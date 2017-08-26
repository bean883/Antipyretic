package cn.bingod.antipyretic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.bingod.antipyretic.library.Antipyretic;

@Table("/web")
public class WebActivity extends AppCompatActivity {

    @Param
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Antipyretic.bind(this);

        WebView webView = (WebView) findViewById(R.id.web);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!Antipyretic.loadUrl(url, WebActivity.this)) view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(url);
    }
}
