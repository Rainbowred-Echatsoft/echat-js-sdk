package com.echatsoft.echatsdk.chat.otherui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.github.echat.chat.BuildConfig;
import com.github.echat.chat.R;


/**
 * @Author: xuhaoyang
 * @Email: xuhaoyang3x@gmail.com
 * @program: BottomSheets
 * @create: 2018-12-19
 * @describe
 */
public class EChatWebviewBottomSheetDialog extends EChatBottomSheetDialog {

    private ProgressBar mProgress;
    private LinearLayout llToolbarClose;
    private ImageView ivToolbarNavigation;
    private TextView tvToolbarTitle;
    private EChatCustomWebview mWebview;
    private boolean isCanBack = true;
    private boolean isShowNavBack = true;
    private FrameLayout mVideoContainer;
    private WebChromeClient.CustomViewCallback mCallBack;

    public EChatWebviewBottomSheetDialog(@NonNull Context context) {
        super(context);
        init(context);
    }

    public EChatWebviewBottomSheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
        init(context);
    }

    protected EChatWebviewBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_webview_echat, null);

        //获得主要的主体颜色
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;

        //设置控件
        final RelativeLayout toolbar = view.findViewById(R.id.toolbarLayout);
        ivToolbarNavigation = view.findViewById(R.id.ivToolbarNavigation);
        tvToolbarTitle = view.findViewById(R.id.tvToolbarTitle);
        llToolbarClose = view.findViewById(R.id.llToolbarClose);
        mVideoContainer = view.findViewById(R.id.videoContainer);
        mWebview = view.findViewById(R.id.webview);
        mProgress = view.findViewById(android.R.id.progress);

        //设置toolbar颜色 圆角
        float[] outerRadius = {10, 10, 10, 10, 0, 0, 0, 0};
        RectF inset = new RectF(0, 0, 0, 0);
        float[] innerRadius = {0, 0, 0, 0, 0, 0, 0, 0};//内矩形 圆角半径
        RoundRectShape roundRectShape = new RoundRectShape(outerRadius, inset, innerRadius);
        ShapeDrawable drawable = new ShapeDrawable(roundRectShape);
        drawable.getPaint().setColor(colorPrimary);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            toolbar.setBackground(drawable);
        } else {
            toolbar.setBackgroundDrawable(drawable);
        }

        if (!isShowNavBack) ivToolbarNavigation.setVisibility(View.GONE);

        //事件
        llToolbarClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ivToolbarNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWebview.canGoBack()) {
                    mWebview.goBack();
                } else {
                    dismiss();
                }
            }
        });

        //设置ContentView
        setContentView(view);

        //初始化Webview
        mWebview.setMoveCallbak(new EChatCustomWebview.MoveCallbak() {
            @Override
            public boolean isIntercept() {
                return isExpanded();
            }
        });
        mWebview.setWebViewClient(mWebViewClient);
        mWebview.setWebChromeClient(mWebChromeClient);
        final WebSettings settings = mWebview.getSettings();
        //开启JavaScript支持
        settings.setJavaScriptEnabled(true);
        //默认设置为true，即允许在 File 域下执行任意 JavaScript 代码
        settings.setAllowFileAccess(true);
        //Disable zoom
        settings.setSupportZoom(true);
        //提高渲染优先级
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 开启DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 开启database storage API功能
        settings.setDatabaseEnabled(true);
        // 开启Application Cache功能
        settings.setAppCacheEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 10);
        //设置脚本是否允许自动打开弹窗
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        //设置WebView是否支持多屏窗口
        settings.setSupportMultipleWindows(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {//这个版本之后 被默认禁止
            settings.setAllowUniversalAccessFromFileURLs(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

    }


    private WebViewClient mWebViewClient = new WebViewClient() {

        //>= Android 5.0
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri url = request.getUrl();
            if (url != null) {
                if (url.toString().startsWith("http")) {
                    view.loadUrl(url.toString());
                }
            }
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("http")) {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (mProgress != null) {
                mProgress.setVisibility(View.GONE);
            }
        }
    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {


        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (!TextUtils.isEmpty(title)) {
                setTitle(title);
            }
        }

        @Override
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

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            LogUtils.i("onShowCustomView");
            mWebview.setVisibility(View.GONE);
            mVideoContainer.setVisibility(View.VISIBLE);
            mVideoContainer.addView(view);
            mCallBack = callback;
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            LogUtils.i("onHideCustomView");
            if (mCallBack != null) {
                mCallBack.onCustomViewHidden();
                mCallBack = null;
            }
            mWebview.setVisibility(View.VISIBLE);
            mVideoContainer.removeAllViews();
            mVideoContainer.setVisibility(View.GONE);
            super.onHideCustomView();
        }
    };

    
    private void exceJSFunction(String funName, String content) {
        String trigger = "javascript:" + funName + "(" + content + ")";
        loadJS(trigger);
    }

    private void loadJS(String trigger) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebview.evaluateJavascript(trigger, null);
        } else {
            mWebview.loadUrl(trigger);
        }
    }

    public void loadUrl(String url) {
        if (mWebview == null) return;
        mWebview.loadUrl(url);
    }

    public void setCanBack(boolean canBack) {
        isCanBack = canBack;
    }

    public void setShowBackNav(boolean show) {
        isShowNavBack = show;
        ivToolbarNavigation.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setTitle(@StringRes final int resId) {
        setTitle(getContext().getResources().getText(resId).toString());
    }

    public void setTitle(String title) {
        if (tvToolbarTitle != null) {
            tvToolbarTitle.setText(title);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWebview != null) {
            mWebview.clearHistory();
            ((ViewGroup) mWebview.getParent()).removeView(mWebview);
            mWebview.loadUrl("about:blank");
            mWebview.stopLoading();
            mWebview.setWebChromeClient(null);
            mWebview.setWebViewClient(null);
            mWebview.destroy();
        }
    }

    @Override
    public void show() {
        super.show();
        mWebview.bindBottomSheetDialog(getContainer());
    }
}
