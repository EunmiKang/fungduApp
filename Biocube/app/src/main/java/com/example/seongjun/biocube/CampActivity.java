package com.example.seongjun.biocube;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Seongjun on 2018. 2. 17..
 */

public class CampActivity extends Activity {

    WebView mWebView;
    WebSettings mWebSettings;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camp);


        mWebView = (WebView)findViewById(R.id.web_camp);
        mWebView.setWebViewClient(new WebViewClient());
        mWebSettings = mWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);

        mWebView.loadUrl("http://biocube.strikingly.com/");
    }
}
