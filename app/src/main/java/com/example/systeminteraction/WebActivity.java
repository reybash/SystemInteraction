// WebActivity.java
package com.example.systeminteraction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class WebActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        // Находим WebView по идентификатору
        WebView webView = findViewById(R.id.webView);

        // Включаем поддержку JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Устанавливаем WebViewClient для обработки ссылок внутри WebView
        webView.setWebViewClient(new WebViewClient());

        // Загружаем веб-страницу
        webView.loadUrl(getString(R.string.url));
    }
}
