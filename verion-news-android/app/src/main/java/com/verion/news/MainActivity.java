package com.verion.news;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private WebView webView;
    private ProgressBar progress;
    private InterstitialAd interstitialAd;
    private int articleClicks = 0;
    private long lastInterstitialShownAt = 0L;

    private static final String HOME = "https://verionnewss.blogspot.com/";
    private static final String TRUSTED_HOST = "verionnewss.blogspot.com";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 1401;
    private static final long INTERSTITIAL_COOLDOWN_MS = 120_000L;
    private static final int INTERSTITIAL_CLICK_INTERVAL = 5;

    private static final String BANNER_ID = "ca-app-pub-4901980834448866/6731694725";
    private static final String INTERSTITIAL_ID = "ca-app-pub-4901980834448866/3267976911";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(7, 17, 31));
        getWindow().setNavigationBarColor(Color.rgb(7, 17, 31));

        NewsCheckWorker.createChannel(this);
        requestNotificationPermissionIfNeeded();
        scheduleNewsChecks();
        MobileAds.initialize(this, status -> {});
        loadInterstitial();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(7, 17, 31));
        applySafeAreaInsets(root);

        FrameLayout webContainer = new FrameLayout(this);
        webContainer.setBackgroundColor(Color.WHITE);
        webView = new WebView(this);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(227, 34, 46)));

        webContainer.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 6);
        pp.gravity = Gravity.TOP;
        webContainer.addView(progress, pp);
        root.addView(webContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        FrameLayout bannerHost = new FrameLayout(this);
        bannerHost.setBackgroundColor(Color.WHITE);
        root.addView(bannerHost, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        AdView banner = new AdView(this);
        banner.setAdUnitId(BANNER_ID);
        FrameLayout.LayoutParams bannerParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bannerParams.gravity = Gravity.CENTER;
        bannerHost.addView(banner, bannerParams);
        setContentView(root);

        bannerHost.post(() -> {
            float density = getResources().getDisplayMetrics().density;
            int widthPx = bannerHost.getWidth();
            if (widthPx <= 0) widthPx = getResources().getDisplayMetrics().widthPixels;
            int adWidthDp = Math.max(320, (int) (widthPx / density));
            banner.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidthDp));
            banner.loadAd(new AdRequest.Builder().build());
        });

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(false);
        s.setUseWideViewPort(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setTextZoom(100);
        s.setUserAgentString(s.getUserAgentString() + " VERIONNEWS-Android/1.7.0");

        webView.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int p) {
                progress.setProgress(p);
                progress.setVisibility(p >= 100 ? View.GONE : View.VISIBLE);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                String scheme = u.getScheme() == null ? "" : u.getScheme().toLowerCase();
                String host = u.getHost() == null ? "" : u.getHost().toLowerCase();

                if (("https".equals(scheme) || "http".equals(scheme)) && TRUSTED_HOST.equals(host)) {
                    String path = u.getPath() == null ? "" : u.getPath();
                    if (path.matches(".*/\\d{4}/\\d{2}/.*")) {
                        articleClicks++;
                        long now = System.currentTimeMillis();
                        boolean clickThreshold = articleClicks % INTERSTITIAL_CLICK_INTERVAL == 0;
                        boolean cooldownPassed = now - lastInterstitialShownAt >= INTERSTITIAL_COOLDOWN_MS;
                        if (clickThreshold && cooldownPassed && interstitialAd != null) {
                            interstitialAd.show(MainActivity.this);
                            interstitialAd = null;
                            lastInterstitialShownAt = now;
                            loadInterstitial();
                        }
                    }
                    return false;
                }

                if ("http".equals(scheme) || "https".equals(scheme)) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, u)); }
                    catch (Exception ignored) {}
                    return true;
                }

                return true;
            }

            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                applyMobilePageFixes(view);
            }

            @Override public void onReceivedError(WebView view, int code, String desc, String failingUrl) {
                Toast.makeText(MainActivity.this,
                        "Connection error. Please check your internet.", Toast.LENGTH_SHORT).show();
            }
        });

        String notificationUrl = getIntent().getStringExtra("article_url");
        if (state == null) {
            webView.loadUrl(isTrustedArticleUrl(notificationUrl) ? notificationUrl : HOME);
        } else {
            webView.restoreState(state);
        }
    }

    private boolean isTrustedArticleUrl(String value) {
        if (value == null || value.isEmpty()) return false;
        try {
            Uri uri = Uri.parse(value);
            String host = uri.getHost();
            return host != null && TRUSTED_HOST.equalsIgnoreCase(host);
        } catch (Exception e) {
            return false;
        }
    }

    private void applyMobilePageFixes(WebView view) {
        String js = "(function(){" +
                "var m=document.querySelector('meta[name=viewport]');" +
                "if(!m){m=document.createElement('meta');m.name='viewport';document.head.appendChild(m);}" +
                "m.setAttribute('content','width=device-width, initial-scale=1.0, maximum-scale=1.0, viewport-fit=cover');" +
                "var old=document.getElementById('verion-mobile-fixes');if(old)old.remove();" +
                "var st=document.createElement('style');st.id='verion-mobile-fixes';" +
                "st.textContent='html,body{width:100%!important;max-width:100%!important;overflow-x:hidden!important;margin:0!important;padding:0!important;}*{box-sizing:border-box!important;}body>*{max-width:100%!important;}.content-wrapper,.main-wrapper,.main-inner,.content-outer,.content-inner,.post-outer-container,.post-outer,.post,.widget,.section,.centered,.page_body,.main-container,.container,.wrapper{width:100%!important;max-width:100%!important;margin-left:0!important;margin-right:0!important;}.content-inner,.main-inner,.post-outer-container,.post-outer,.post{padding-left:10px!important;padding-right:10px!important;}img,video,iframe,canvas,svg,table{max-width:100%!important;height:auto;}[data-verion-nowrap=\\\"1\\\"]{white-space:nowrap!important;word-break:normal!important;overflow-wrap:normal!important;}';" +
                "document.head.appendChild(st);" +
                "var oldBtn=document.getElementById('verion-app-back');if(oldBtn)oldBtn.remove();" +
                "Array.prototype.forEach.call(document.querySelectorAll('a,span,div,button,strong,b'),function(el){var t=(el.textContent||'').replace(/\\s+/g,' ').trim().toUpperCase();if(t==='NATIONAL')el.setAttribute('data-verion-nowrap','1');});" +
                "document.documentElement.scrollLeft=0;document.body.scrollLeft=0;})();";
        view.evaluateJavascript(js, null);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST);
        }
    }

    private void scheduleNewsChecks() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest periodic = new PeriodicWorkRequest.Builder(
                NewsCheckWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "verion_news_background_check",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodic);

        OneTimeWorkRequest initial = new OneTimeWorkRequest.Builder(NewsCheckWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(this).enqueueUniqueWork(
                "verion_news_initial_check",
                ExistingWorkPolicy.REPLACE,
                initial);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String url = intent.getStringExtra("article_url");
        if (webView != null && isTrustedArticleUrl(url)) webView.loadUrl(url);
    }

    private void applySafeAreaInsets(View root) {
        root.setOnApplyWindowInsetsListener((v, windowInsets) -> {
            int top, bottom;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Insets bars = windowInsets.getInsets(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                top = Math.max(0, bars.top);
                bottom = Math.max(0, bars.bottom);
            } else {
                top = Math.max(0, windowInsets.getSystemWindowInsetTop());
                bottom = Math.max(0, windowInsets.getSystemWindowInsetBottom());
            }
            v.setPadding(0, top, 0, bottom);
            return windowInsets;
        });
        root.requestApplyInsets();
    }

    private void loadInterstitial() {
        InterstitialAd.load(this, INTERSTITIAL_ID, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override public void onAdLoaded(InterstitialAd ad) { interstitialAd = ad; }
                    @Override public void onAdFailedToLoad(LoadAdError error) { interstitialAd = null; }
                });
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        if (webView != null) webView.saveState(out);
        super.onSaveInstanceState(out);
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
