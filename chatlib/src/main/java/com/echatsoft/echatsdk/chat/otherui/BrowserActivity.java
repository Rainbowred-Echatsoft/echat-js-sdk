package com.echatsoft.echatsdk.chat.otherui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;


import com.echatsoft.echatsdk.chat.utils.Constants;
import com.github.echat.chat.R;

import org.json.JSONException;
import org.json.JSONObject;


public class BrowserActivity extends WebViewActivity {

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //多进程启动下 替换白屏
        setTheme(R.style.EChatTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_browser_echat);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 显示返回按钮并隐藏图标
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        String url = getIntent().getStringExtra(Constants.EXTRA_BROWER_URL);
        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }

        initWebView();
        //加载网页
        loadUrl(url);
    }

    private void initWebView() {
        //初始化Webview设置
        WebSettings settings = getWebView().getSettings();
        //开启JavaScript支持
        settings.setJavaScriptEnabled(true);
        //默认设置为true，即允许在 File 域下执行任意 JavaScript 代码
        settings.setAllowFileAccess(true);
        //Disable zoom
        settings.setSupportZoom(false);
        //提高渲染优先级
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //建议缓存策略为，判断是否有网络，有的话，使用LOAD_DEFAULT, 无网络时，使用LOAD_CACHE_ELSE_NETWORK
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 设置缓存模式
        // 开启DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 开启database storage API功能
        settings.setDatabaseEnabled(true);
        String cacheDirPath = getCacheDir().getAbsolutePath();
        // 设置数据库缓存路径
        settings.setDatabasePath(cacheDirPath);
        // 设置Application caches缓存目录
        settings.setAppCachePath(cacheDirPath);
        // 开启Application Cache功能
        settings.setAppCacheEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 10);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(false);
        }

        try {
            getWebView().addJavascriptInterface(new EChatWebviewJavascriptBridge(), "EchatJsBridge");
        } catch (Exception e) {
        }
    }

    class EChatWebviewJavascriptBridge {

        @JavascriptInterface
        public void callEchatNative(String input) {
            try {
                final JSONObject object = new JSONObject(input);
                String functionName;
                functionName = object.optString("functionName");

                if ("closeUrlView".equals(functionName)) {
                    finish();
                }

            } catch (JSONException e) {

            }
        }
    }


    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(WebView view, String url) {

    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        if (!TextUtils.isEmpty(title)) {
            toolbar.setTitle(title);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("http")) {
            view.loadUrl(url);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Uri url = request.getUrl();
        if (url != null) {
            return this.shouldOverrideUrlLoading(view, url.toString());
        }
        return false;
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {

    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
