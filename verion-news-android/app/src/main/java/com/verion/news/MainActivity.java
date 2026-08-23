package com.verion.news;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;

public class MainActivity extends Activity {
    private WebView webView;
    private ProgressBar progress;
    private InterstitialAd interstitialAd;
    private int articleClicks = 0;
    private static final String HOME = "https://verionnewss.blogspot.com/";
    private static final String BANNER_ID = "ca-app-pub-4901980834448866/6731694725";
    private static final String INTERSTITIAL_ID = "ca-app-pub-4901980834448866/3267976911";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(7,17,31));
        getWindow().setNavigationBarColor(Color.rgb(7,17,31));

        MobileAds.initialize(this, status -> {});
        loadInterstitial();

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        FrameLayout webContainer = new FrameLayout(this);
        webView = new WebView(this);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(227,34,46)));
        webContainer.addView(webView, new FrameLayout.LayoutParams(-1,-1));
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(-1,6); pp.gravity=Gravity.TOP;
        webContainer.addView(progress,pp);
        column.addView(webContainer,new LinearLayout.LayoutParams(-1,0,1f));

        AdView banner = new AdView(this);
        banner.setAdSize(AdSize.BANNER);
        banner.setAdUnitId(BANNER_ID);
        column.addView(banner,new LinearLayout.LayoutParams(-1,-2));
        setContentView(column);
        banner.loadAd(new AdRequest.Builder().build());

        WebSettings s=webView.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true); s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setBuiltInZoomControls(false); s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setUserAgentString(s.getUserAgentString()+" VERIONNEWS-Android/1.4");

        webView.setWebChromeClient(new android.webkit.WebChromeClient(){
            @Override public void onProgressChanged(WebView view,int p){ progress.setProgress(p); progress.setVisibility(p>=100?View.GONE:View.VISIBLE); }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
                Uri u=request.getUrl(); String host=u.getHost()==null?"":u.getHost();
                if(host.contains("verionnewss.blogspot.com")||host.contains("blogspot.com")||host.contains("blogger.com")){
                    String path=u.getPath()==null?"":u.getPath();
                    if(path.matches(".*/\\d{4}/\\d{2}/.*")){
                        articleClicks++;
                        if(articleClicks%3==0 && interstitialAd!=null){
                            final String target=u.toString();
                            interstitialAd.show(MainActivity.this);
                            interstitialAd=null; loadInterstitial();
                            view.loadUrl(target); return true;
                        }
                    }
                    return false;
                }
                try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception ignored){}
                return true;
            }
            @Override public void onReceivedError(WebView view,int code,String desc,String failingUrl){ Toast.makeText(MainActivity.this,"Connection error. Please check your internet.",Toast.LENGTH_SHORT).show(); }
        });
        if(state==null) webView.loadUrl(HOME); else webView.restoreState(state);
    }

    private void loadInterstitial(){
        InterstitialAd.load(this,INTERSTITIAL_ID,new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){
            @Override public void onAdLoaded(InterstitialAd ad){ interstitialAd=ad; }
            @Override public void onAdFailedToLoad(LoadAdError error){ interstitialAd=null; }
        });
    }

    @Override protected void onResume(){ super.onResume(); if(webView!=null&&webView.getUrl()!=null) webView.reload(); }
    @Override protected void onSaveInstanceState(Bundle out){ webView.saveState(out); super.onSaveInstanceState(out); }
    @Override public void onBackPressed(){ if(webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
}
