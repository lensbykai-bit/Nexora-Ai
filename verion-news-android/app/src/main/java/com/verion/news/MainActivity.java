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
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
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
    private static final String HOME = "https://verionnewss.blogspot.com/";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 1401;

    // VERION NEWS production AdMob units.
    private static final String BANNER_ID = "ca-app-pub-4901980834448866/6731694725";
    private static final String INTERSTITIAL_ID = "ca-app-pub-4901980834448866/3267976911";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(7,17,31));
        getWindow().setNavigationBarColor(Color.rgb(7,17,31));

        NewsCheckWorker.createChannel(this);
        requestNotificationPermissionIfNeeded();
        scheduleNewsChecks();

        MobileAds.initialize(this, status -> {});
        loadInterstitial();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(7,17,31));
        applySafeAreaInsets(root);

        FrameLayout webContainer = new FrameLayout(this);
        webContainer.setBackgroundColor(Color.WHITE);
        webView = new WebView(this);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(227,34,46)));
        webContainer.addView(webView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 6);
        pp.gravity = Gravity.TOP;
        webContainer.addView(progress, pp);
        root.addView(webContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        FrameLayout bannerHost = new FrameLayout(this);
        bannerHost.setBackgroundColor(Color.WHITE);
        root.addView(bannerHost, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        AdView banner = new AdView(this);
        banner.setAdUnitId(BANNER_ID);
        FrameLayout.LayoutParams bannerParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bannerParams.gravity = Gravity.CENTER;
        bannerHost.addView(banner, bannerParams);
        setContentView(root);

        bannerHost.post(() -> {
            float density = getResources().getDisplayMetrics().density;
            int widthPx = bannerHost.getWidth() - bannerHost.getPaddingLeft() - bannerHost.getPaddingRight();
            if (widthPx <= 0) widthPx = getResources().getDisplayMetrics().widthPixels;
            int adWidthDp = Math.max(320, (int) (widthPx / density));
            banner.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidthDp));
            banner.loadAd(new AdRequest.Builder().build());
        });

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setUserAgentString(s.getUserAgentString() + " VERIONNEWS-Android/1.6");

        webView.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int p) {
                progress.setProgress(p);
                progress.setVisibility(p >= 100 ? View.GONE : View.VISIBLE);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                String host = u.getHost() == null ? "" : u.getHost();
                if (host.contains("verionnewss.blogspot.com") || host.contains("blogspot.com") || host.contains("blogger.com")) {
                    String path = u.getPath() == null ? "" : u.getPath();
                    if (path.matches(".*/\\d{4}/\\d{2}/.*")) {
                        articleClicks++;
                        if (articleClicks % 3 == 0 && interstitialAd != null) {
                            String target = u.toString();
                            interstitialAd.show(MainActivity.this);
                            interstitialAd = null;
                            loadInterstitial();
                            view.loadUrl(target);
                            return true;
                        }
                    }
                    return false;
                }
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                return true;
            }
            @Override public void onReceivedError(WebView view, int code, String desc, String failingUrl) {
                Toast.makeText(MainActivity.this, "Connection error. Please check your internet.", Toast.LENGTH_SHORT).show();
            }
        });

        String notificationUrl = getIntent().getStringExtra("article_url");
        if (state == null) {
            webView.loadUrl(notificationUrl != null && !notificationUrl.isEmpty() ? notificationUrl : HOME);
        } else {
            webView.restoreState(state);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
        }
    }

    private void scheduleNewsChecks() {
        PeriodicWorkRequest periodic = new PeriodicWorkRequest.Builder(NewsCheckWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "verion_news_background_check",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodic);

        OneTimeWorkRequest initial = new OneTimeWorkRequest.Builder(NewsCheckWorker.class).build();
        WorkManager.getInstance(this).enqueueUniqueWork(
                "verion_news_initial_check",
                ExistingWorkPolicy.REPLACE,
                initial);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String url = intent.getStringExtra("article_url");
        if (webView != null && url != null && !url.isEmpty()) webView.loadUrl(url);
    }

    private void applySafeAreaInsets(View root) {
        root.setOnApplyWindowInsetsListener((v, windowInsets) -> {
            int left, top, right, bottom;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Insets bars = windowInsets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                left = bars.left; top = bars.top; right = bars.right; bottom = bars.bottom;
            } else {
                left = windowInsets.getSystemWindowInsetLeft();
                top = windowInsets.getSystemWindowInsetTop();
                right = windowInsets.getSystemWindowInsetRight();
                bottom = windowInsets.getSystemWindowInsetBottom();
            }
            v.setPadding(left, top, right, bottom);
            return windowInsets;
        });
        root.requestApplyInsets();
    }

    private void loadInterstitial() {
        InterstitialAd.load(this, INTERSTITIAL_ID, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override public void onAdLoaded(InterstitialAd ad) { interstitialAd = ad; }
            @Override public void onAdFailedToLoad(LoadAdError error) { interstitialAd = null; }
        });
    }

    @Override protected void onResume() { super.onResume(); if (webView != null && webView.getUrl() != null) webView.reload(); }
    @Override protected void onSaveInstanceState(Bundle out) { webView.saveState(out); super.onSaveInstanceState(out); }
    @Override public void onBackPressed() { if (webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
}
