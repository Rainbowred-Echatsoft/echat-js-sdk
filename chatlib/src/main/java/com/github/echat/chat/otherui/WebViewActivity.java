package com.github.echat.chat.otherui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


import com.blankj.utilcode.util.LogUtils;
import com.github.echatmulti.sample.R;

import java.util.Map;


/**
 * Created by Echat_Android on 2018/1/11.
 */

public abstract class WebViewActivity extends AppCompatActivity {

    private final static String TAG = WebViewActivity.class.getSimpleName();

    private ProgressBar mProgress;
    protected ProgressDialog mProgressDialog;
    protected AlertDialog.Builder mDialog;
    private AlertDialog alertDialog;
    private WebView mWebView;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mProgress = findViewById(android.R.id.progress);
        mWebView = findViewById(R.id.webview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //临时关闭硬件加速
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWebView.getSettings().setSafeBrowsingEnabled(false);
        }

        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebView mWebView = getWebView();
        if (mWebView != null) {
            mWebView.loadUrl("about:blank");
            mWebView.clearHistory();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.stopLoading();
            mWebView.setWebChromeClient(null);
            mWebView.setWebViewClient(null);
            mWebView.destroy();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //重启硬件加速
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
    }

    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        mWebView.loadUrl(url, additionalHttpHeaders);
    }

    public void loadUrl(String url) {
        if (isMainThread()){
            mWebView.loadUrl(url);
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(url);
                }
            });
        }

    }

    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    private void loadJS(String trigger) {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mWebView.evaluateJavascript(trigger, null);
                } else {
                    mWebView.loadUrl(trigger);
                }
            }
        });
    }

    public void exceJSFunction(String funName, String content) {
        String trigger = "javascript:" + funName + "(" + content + ")";
        loadJS(trigger);
    }

    /**
     * 告知JavaScript 功能回调消息
     *
     * @param value
     */
    public void loadJSFun(String value) {
        String msgString = getJavaScriptJson(value);
        if (!TextUtils.isEmpty(msgString)) {
            exceJSFunction("callEchatJs", msgString);
        }
    }

    public static String getJavaScriptJson(String msg) {
        StringBuffer msgString = new StringBuffer(msg);
        char[] chars = msgString.toString().toCharArray();
        msgString.setLength(0);
        msgString.append("\"");
        for (int i = 0; i < chars.length; i++) {
            if (i - 1 >= 0 && '\"' == (chars[i]) && '\\' == chars[i - 1]) {
                msgString.append(chars[i]);
                continue;
            }
            if ('\"' == (chars[i])) {
                msgString.append("\\");
            }
            msgString.append(chars[i]);
        }
        msgString.append("\"");
        return msgString.toString();
    }


    public abstract void onPageStarted(WebView view, String url, Bitmap favicon);

    public abstract void onPageFinished(WebView view, String url);

    public abstract void onReceivedError(WebView view, int errorCode,
                                         String description, String failingUrl);

    public void onProgressChanged(WebView view, int newProgress) {
        if (mProgress != null) {
            mProgress.setMax(100);
            mProgress.setProgress(newProgress);
            // 如果进度大于或者等于100，则隐藏进度条
            if (newProgress >= 100) {
                mProgress.setVisibility(View.GONE);
            }
        }
    }

    public abstract void onReceivedTitle(WebView view, String title);

    @SuppressLint("NewApi")
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return shouldOverrideUrlLoading(view, request.getUrl().toString());
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    /**
     * >= Android 3.0
     * <  Android 5.0
     * 上传文件时调用
     *
     * @param valueCallback
     * @param acceptType
     * @param capture
     */
    public abstract void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture);

    /**
     * >= Android 5.0
     * 上传文件时调用
     *
     * @param webView
     * @param filePathCallback
     * @param fileChooserParams
     * @return
     */
    public abstract boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                              WebChromeClient.FileChooserParams fileChooserParams);

    public WebView getWebView() {
        return mWebView;
    }

    public void setJavaScriptEnabled(boolean b) {
        getWebView().getSettings().setJavaScriptEnabled(b);
    }

    public WebSettings getSettings() {
        return mWebView.getSettings();
    }

    @Keep
    public WebViewClient mWebViewClient = new WebViewClient() {

        //>= Android 5.0
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return WebViewActivity.this.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return WebViewActivity.this.shouldOverrideUrlLoading(view, url);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return shouldInterceptRequest(view, request.getUrl().toString());
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            WebViewActivity.this.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            WebViewActivity.this.onPageFinished(view, url);
        }

        @Keep
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            LogUtils.e("WebView error: " + errorCode + " + " + description);
            WebViewActivity.this.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Keep
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            String channel = "";
            ApplicationInfo appInfo = null;
            Context context = WebViewActivity.this;
            try {
                appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                channel = appInfo.metaData.getString("TD_CHANNEL_ID");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(channel) && channel.equals("play.google.com")) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                String message = context.getString(R.string.ssl_error);
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = context.getString(R.string.ssl_error_not_trust);
                        break;
                    case SslError.SSL_EXPIRED:
                        message = context.getString(R.string.ssl_error_expired);
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = context.getString(R.string.ssl_error_mismatch);
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = context.getString(R.string.ssl_error_not_valid);
                        break;
                }
                message += context.getString(R.string.ssl_error_continue_open);

                builder.setTitle(R.string.ssl_error);
                builder.setMessage(message);
                builder.setPositiveButton(R.string.continue_open, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                handler.proceed();
            }
        }
    };

    @Keep
    public WebChromeClient mWebChromeClient = new WebChromeClient() {
        public void onProgressChanged(WebView view, int newProgress) {
            WebViewActivity.this.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            WebViewActivity.this.onReceivedTitle(view, title);
        }

        // For Android  >= 3.0
        public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType) {
            WebViewActivity.this.openFileChooser(valueCallback, acceptType, null);
        }

        //For Android  >= 4.1
        public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
            WebViewActivity.this.openFileChooser(valueCallback, acceptType, capture);
        }

        // For Android >= 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                         FileChooserParams fileChooserParams) {
            return WebViewActivity.this.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            LogUtils.i(cm.message() + " -- From line "
                    + cm.lineNumber() + " of "
                    + cm.sourceId());
            return true;
        }

    };

    /**
     * 显示加载框
     *
     * @param resId
     */
    protected void showProgressDialog(int resId) {
        showProgressDialog(getString(resId));
    }

    /**
     * 显示加载框
     *
     * @param message
     */
    protected void showProgressDialog(String message) {
        showProgressDialog(message, false);
    }

    protected void showProgressDialog(String message, boolean isCancelable) {
        if (!isInvalidContext()) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
            }
            mProgressDialog.setCancelable(isCancelable);
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }

    /**
     * 关闭加载框
     */
    protected void dismissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    if (!isInvalidContext()) {
                        mProgressDialog.dismiss();
                        mProgressDialog.cancel();
                        mProgressDialog = null;
                    }
                }
            }
        });

    }

    private boolean isInvalidContext() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return (isDestroyed() || isFinishing());
        } else {
            return isFinishing();
        }
    }

    /**
     * 判断加载框是否显示
     *
     * @return
     */
    protected boolean isShowProgressDialog() {
        if (mProgressDialog == null) {
            return false;
        }
        return mProgressDialog.isShowing();
    }


    /**
     * 显示AlertDialog
     *
     * @param titleResId
     * @param contentResId
     * @param btnName
     * @param callback
     */
    protected void showAlertDialog(int titleResId, int contentResId, String[] btnName, AlertDiaglogCallback callback) {
        showAlertDialog(getString(titleResId), getString(contentResId), btnName, callback);
    }

    /**
     * 显示AlertDialog
     *
     * @param title
     * @param content
     * @param btnName
     * @param callback
     */
    protected void showAlertDialog(String title, String content, String[] btnName, final AlertDiaglogCallback callback) {
        if (mDialog == null) {
            mDialog = new AlertDialog.Builder(this);
        }

        if (btnName == null || btnName.length <= 2) {
            btnName = new String[]{"取消", "确定"};
        }

        alertDialog = mDialog.setTitle(title).setMessage(content).setNegativeButton(btnName[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //取消
                dialog.cancel();
                callback.cancel();
            }
        }).setPositiveButton(btnName[1], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //确定
                dialog.dismiss();
                callback.confirm();
            }
        }).create();
        alertDialog.setCancelable(false);
        if (!isInvalidContext()) {
            alertDialog.show();
        }
    }

    /**
     * AlertDialog 是否以显示
     *
     * @return
     */
    protected boolean isShowAlertDialog() {
        if (mDialog == null) {
            return false;
        }
        alertDialog = mDialog.create();
        return alertDialog.isShowing();
    }

    public boolean dismissAlertDialog() {
        if (mDialog == null) {
            return false;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    if (!isInvalidContext()) {
                        alertDialog.dismiss();
                        alertDialog.cancel();
                        alertDialog = null;
                    }
                }
            }
        });
        return true;
    }

    protected interface AlertDiaglogCallback {
        void cancel();

        void confirm();
    }
}
