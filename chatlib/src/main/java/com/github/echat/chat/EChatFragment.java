package com.github.echat.chat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.echat.jzvd.JZVideoPlayer;
import com.echat.jzvd.JZVideoPlayerStandard;
import com.github.echat.chat.otherui.BrowserActivity;
import com.github.echat.chat.otherui.CameraActivity;
import com.github.echat.chat.otherui.WebviewBottomDialogActivity;
import com.github.echat.chat.utils.Constants;
import com.github.echat.chat.utils.FragmentUtils;
import com.github.echat.chat.utils.GifSizeFilter;
import com.github.echat.chat.utils.GlideImageEngine;
import com.github.echat.chat.utils.JZMediaIjkplayer;
import com.github.echat.chat.utils.RequestUtils;
import com.github.echat.chat.utils.UrlUtils;
import com.maning.imagebrowserlibrary.MNImageBrowser;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.DOWNLOAD_SERVICE;
import static com.github.echat.chat.utils.Constants.ACTION_NEW_MSG;
import static com.github.echat.chat.utils.Constants.ACTION_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.CHAT_LAST_CHAT_TIME;
import static com.github.echat.chat.utils.Constants.CHAT_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.COMPANY_ID;
import static com.github.echat.chat.utils.Constants.METADATA;
import static com.github.echat.chat.utils.Constants.PLATFORM_SIGN;
import static com.github.echat.chat.utils.Constants.PUSH_INFO;
import static com.github.echat.chat.utils.Constants.SP_LAST_CHAT_TIME;
import static com.github.echat.chat.utils.Constants.TYPE;
import static com.github.echat.chat.utils.Constants.TYPE_CHAT;
import static com.github.echat.chat.utils.Constants.TYPE_MSGBOX;
import static com.github.echat.chat.utils.Constants.ECHATTAG;
import static com.github.echat.chat.utils.Constants.VISEVT;


/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-20
 * @describe
 */
public class EChatFragment extends Fragment implements Toolbar.OnMenuItemClickListener, FragmentUtils.OnBackClickListener {
    private static final String TAG = "EChatFragment";
    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    private Handler mHandler = new Handler();
    private WeakReference<Activity> mActivityWeakReference;
    protected View mContentView;
    private Toolbar toolbar;
    private MenuItem endChat;
    private WebView mWebView;
    private boolean uploading;//处理上次异常关闭
    private DownloadCompleteReceiver downloadCompleteReceiver;

    public static EChatFragment newInstance(Bundle args) {
        EChatFragment fragment = new EChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivityWeakReference = new WeakReference<>((Activity) context);
    }

    public EChatFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.iTag(TAG, "onCreate");
        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        LogUtils.iTag(TAG, "onSaveInstanceState: ");
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
        if (mUploadMessageL != null || mUploadMessage != null) {
            outState.putBoolean("Uploading", true);
        }
        LogUtils.i(outState);
    }

    @Override
    public void onResume() {
        LogUtils.iTag(TAG, "onResume");
        super.onResume();
        //恢复Webview活动
        mWebView.resumeTimers();
        /**
         *  通知一洽H5重新开始通信
         */
        callJs(mWebView, "EchatReConnect", null);
    }

    @Override
    public void onPause() {
        super.onPause();

        /**
         * 通知一洽H5停止通信(作用快速通知服务器用户离线 客服消息由远程消息推送)
         * 如果是上传视频
         */
        if (!uploading) {
            callJs(mWebView, "EchatActiveOffline", null);
            mHandler.postDelayed(() -> {
                //暂停Webview活动
                mWebView.pauseTimers();
            }, 100);
        }
        try {
            JZVideoPlayer.releaseAllVideos();
        } catch (NoClassDefFoundError e) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.layout_fr_echat, container, false);
        return mContentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LogUtils.iTag(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        //handle data
        //init base UI
        initBaseUI(savedInstanceState);
        initData(bundle);
    }

    private void initData(Bundle bundle) {

        //先初始化
        lastChatTime = SPUtils.getInstance().getLong(SP_LAST_CHAT_TIME);
        String chatUrl = bundle.getString(Constants.EXTRA_CHAT_URL, "");
        //来自通知点击打开
        if (!TextUtils.isEmpty(chatUrl)) {
            openCompanyId = bundle.getString(Constants.EXTRA_COMPANY_ID, "");
            openUrl = chatUrl;
        }
        //用户点击打开
        else {
            openCompanyId = bundle.getString(COMPANY_ID);
            platformSign = bundle.getString(PLATFORM_SIGN);
            pushInfo = bundle.getString(PUSH_INFO);
            metaData = bundle.getString(METADATA);
            visEvt = bundle.getString(VISEVT);
            echatTag = bundle.getString(ECHATTAG);
            String type = bundle.getString(TYPE);
            LogUtils.i(String.format("visEvt:%s ", visEvt));
            if (TYPE_CHAT.equals(type)) {
                openUrl = CHAT_URL;
            } else if (TYPE_MSGBOX.equals(type)) {
                openUrl = UrlUtils.appendParam(MSGBOX_URL, "echatTitleBar", "0");
            }

            openUrl = UrlUtils.appendParams(openUrl, new HashMap<String, String>() {{
                if (!TextUtils.isEmpty(openCompanyId)) put("companyId", openCompanyId);
                if (!TextUtils.isEmpty(platformSign)) put("platformSign", platformSign);
                if (!TextUtils.isEmpty(pushInfo)) put("pushInfo", pushInfo);
                if (!TextUtils.isEmpty(metaData)) put("metaData", metaData);
                if (!TextUtils.isEmpty(visEvt)) put("visEvt", visEvt);
                if (!TextUtils.isEmpty(echatTag)) put("echatTag", echatTag);
            }});
        }


    }

    private void initBaseUI(Bundle savedInstanceState) {
        //意外销毁 恢复
        if (savedInstanceState != null) {
            uploading = savedInstanceState.getBoolean("Uploading");
        }

        toolbar = mContentView.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_echat);
        endChat = toolbar.getMenu().findItem(R.id.endChat);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setTitle("...");
        toolbar.setNavigationIcon(R.drawable.echat_ic_back_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //返回
                nativeBackClick();
            }
        });

        mWebView = mContentView.findViewById(R.id.webview);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setDownloadListener(downloadListener);

        downloadCompleteReceiver = new DownloadCompleteReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getWActivity().registerReceiver(downloadCompleteReceiver, intentFilter);
    }

    private void init(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        //加载业务
        LogUtils.i(String.format("加载对话(消息盒子)地址:%s", openUrl));
        initChatView(openUrl);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.endChat) {
            callJs(mWebView, "closeChat", null);
        }
        return true;
    }

    /**
     * 按 返回键 的处理
     *
     * @return
     */
    @Override
    public boolean onBackClick() {
        //当前可能存在视频播放 先退回视频播放
        try {
            if (JZVideoPlayer.backPress()) {
                return false;
            }
        } catch (NoClassDefFoundError e) {
        }

        //无视频播放时 调用返回处理方法
        nativeBackClick();
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        LogUtils.iTag(TAG, "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);
        init(savedInstanceState, mContentView);
    }

    @Override
    public void onDestroyView() {
        LogUtils.iTag(TAG, "onDestroyView: ");

        getWActivity().unregisterReceiver(downloadCompleteReceiver);

        /**
         * 安全销毁回收Fragment资源
         */
        if (mContentView != null) {
            ((ViewGroup) mContentView.getParent()).removeView(mContentView);
        }

        /**
         * 安全销毁Webview
         */
        if (mWebView != null) {
            mWebView.clearHistory();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.loadUrl("about:blank");
            mWebView.stopLoading();
            mWebView.setWebChromeClient(null);
            mWebView.setWebViewClient(null);
            mWebView.destroy();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.iTag(TAG, "onDestroy: ");

    }

    /**
     * 权限处理
     *
     * @param shouldRequest
     * @param activity
     */
    public static void showRationaleDialog(final PermissionUtils.OnRationaleListener.ShouldRequest shouldRequest, Activity activity) {
        if (activity == null) return;
        new AlertDialog.Builder(activity)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(R.string.permission_rationale_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shouldRequest.again(true);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shouldRequest.again(false);
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    /**
     * 权限处理
     */
    public void showOpenAppSettingDialog() {
        if (getWActivity() == null) return;
        new android.support.v7.app.AlertDialog.Builder(getWActivity())
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(R.string.permission_denied_forever_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtils.launchAppDetailsSettings();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }


    /*----Webview Base-------*/
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        mWebView.loadUrl(url, additionalHttpHeaders);
    }

    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    private void loadJS(String trigger) {
        LogUtils.iTag("app->js", trigger);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(trigger, null);
        } else {
            mWebView.loadUrl(trigger);
        }
    }

    public void clearHistory() {
        mWebView.clearHistory();
    }

    /**
     * app native告知H5 JS数据
     *
     * @param webView
     * @param functionName
     * @param value
     */
    private void callJs(WebView webView, String functionName, Object value) {
        if (webView == null) return;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("functionName", functionName);
            if (value != null) {
                jsonObject.put("value", value);
            }
            loadJS("javascript:callEchatJs('" + jsonObject.toString() + "')");
        } catch (Exception e) {
        }
    }

    public WebSettings getSettings() {
        return mWebView.getSettings();
    }

    public DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            LogUtils.wTag(TAG, String.format("有个文件要下载%s,%s,%s,%s,%s", url, userAgent, contentDisposition, mimetype, String.valueOf(contentLength)));
            //建议做法
            //1、检查当前网络状态
            //2、如果未联网，请勿调用系统下载，系统下载是一个队列，如果系统认为当前没有网络，会把任务放在队列中，当有网络时，会把队列中的都下载一遍
            //3、这里使用公用目录Download 需要申请写入权限
            if (checkStoragePermission()) {
                downloadBySystem(url, contentDisposition, mimetype);
            } else {
                //申请权限
                PermissionUtils.permission(PermissionConstants.STORAGE)
                        .rationale(new PermissionUtils.OnRationaleListener() {
                            @Override
                            public void rationale(ShouldRequest shouldRequest) {
                                showRationaleDialog(shouldRequest, getWActivity());
                            }
                        }).callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        downloadBySystem(url, contentDisposition, mimetype);
                    }

                    @Override
                    public void onDenied() {
                        showOpenAppSettingDialog();
                    }
                }).request();
            }
        }
    };

    /**
     * 实现文件下载
     *
     * @param url
     * @param contentDisposition
     * @param mimeType
     */
    private void downloadBySystem(String url, String contentDisposition, String mimeType) {
        // 指定下载地址ttu
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner();
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // 允许在计费流量下下载
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(true);
        }
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true);
        // 允许漫游时下载
        request.setAllowedOverRoaming(true);
        // 允许下载的网路类型
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        // 设置下载文件保存的路径和文件名
        String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
        LogUtils.wTag(TAG, String.format("filename:{%s}", fileName));
        // 设置通知栏的标题，如果不设置，默认使用文件名
        request.setTitle("下载" + fileName);
        // 设置通知栏的描述
        //request.setDescription(fileName);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        //另外可选一下方法，自定义下载路径
        //request.setDestinationUri()
        //request.setDestinationInExternalFilesDir()
        final DownloadManager downloadManager = (DownloadManager) getWActivity().getSystemService(DOWNLOAD_SERVICE);
        // 添加一个下载任务
        long downloadId = downloadManager.enqueue(request);
        LogUtils.wTag(TAG, String.format("downloadId:{%s}", downloadId));
    }


    private class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.iTag(TAG, String.format("onReceive. intent:{%s}", intent != null ? intent.toUri(0) : null));

            if (intent != null) {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    LogUtils.iTag(TAG, String.format("downloadId:{%s}", downloadId));
                    openDownloadedAttachment(getWActivity(), downloadId);
                }
            }
        }
    }

    /**
     * Used to open the downloaded attachment.
     *
     * @param context    Content.
     * @param downloadId Id of the downloaded file to open.
     */
    private void openDownloadedAttachment(final Context context, final long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String downloadLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String downloadMimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
            if ((downloadStatus == DownloadManager.STATUS_SUCCESSFUL) && downloadLocalUri != null) {
                openDownloadedAttachment(getWActivity(), Uri.parse(downloadLocalUri), downloadMimeType);
            }
        }
        cursor.close();
    }

    /**
     * Used to open the downloaded attachment.
     * <p/>
     * 1. Fire intent to open download file using external application.
     * <p>
     * 2. Note:
     * 2.a. We can't share fileUri directly to other application (because we will get FileUriExposedException from Android7.0).
     * 2.b. Hence we can only share content uri with other application.
     * 2.c. We must have declared FileProvider in manifest.
     * 2.c. Refer - https://developer.android.com/reference/android/support/v4/content/FileProvider.html
     *
     * @param context            Context.
     * @param attachmentUri      Uri of the downloaded attachment to be opened.
     * @param attachmentMimeType MimeType of the downloaded attachment.
     */
    private void openDownloadedAttachment(final Context context, Uri attachmentUri, final String attachmentMimeType) {
        if (attachmentUri != null) {
            // Get Content Uri.
            if (ContentResolver.SCHEME_FILE.equals(attachmentUri.getScheme())) {
                // FileUri - Convert it to contentUri.
                File file = new File(attachmentUri.getPath());
                attachmentUri = FileProvider.getUriForFile(getWActivity(), context.getPackageName() + ".fileprovider", file);
            }
            Intent openAttachmentIntent = new Intent(Intent.ACTION_VIEW);
            openAttachmentIntent.setDataAndType(attachmentUri, attachmentMimeType);
            openAttachmentIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(openAttachmentIntent);
            } catch (ActivityNotFoundException e) {
                ToastUtils.showLong("打开文件错误");
            }
        }
    }

    public WebViewClient mWebViewClient = new WebViewClient() {

        //>= Android 5.0
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return EChatFragment.this.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return EChatFragment.this.shouldOverrideUrlLoading(view, url);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return shouldInterceptRequest(view, request.getUrl().toString());
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            EChatFragment.this.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            EChatFragment.this.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            LogUtils.e("WebView error: " + errorCode + " + " + description);
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            String channel = "";
            ApplicationInfo appInfo = null;
            Context context = getWActivity();
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

    public WebChromeClient mWebChromeClient = new WebChromeClient() {


        // For Android  >= 3.0
        public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType) {
            EChatFragment.this.openFileChooser(valueCallback, acceptType, null);
        }

        //For Android  >= 4.1
        public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
            EChatFragment.this.openFileChooser(valueCallback, acceptType, capture);
        }

        // For Android >= 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                         FileChooserParams fileChooserParams) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return EChatFragment.this.onShowFileChooser(webView, filePathCallback, fileChooserParams);
            }
            return false;
        }


    };


    public void onPageStarted(WebView view, String url, Bitmap favicon) {

    }

    public void onPageFinished(WebView view, String url) {
        if (uploading) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("functionName", "triggerFile");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            loadUrl("javascript:window.callEchatJs(" +
                    jsonObject.toString() + ")");
        }

        JSONObject mediaOption = new JSONObject();
        JSONObject openOption = new JSONObject();
        try {
            mediaOption.put("video", 1);
            mediaOption.put("image", 1);
            openOption.put("native", 1);
        } catch (JSONException e) {
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callJs(mWebView, "setMediaPlayer", mediaOption);
                callJs(mWebView, "setLinkOpener", openOption);
                mWebView.clearHistory();
            }
        }, 500);
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("tel:")) {
            callPhone(url.replaceAll("tel:", ""));
        }

        if (url.startsWith("mailto:")) {
            openMail(url);
        }

        if (url.startsWith("http")) {
            view.loadUrl(url);
        }
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Uri url = request.getUrl();
        if (url != null) {
            return this.shouldOverrideUrlLoading(view, url.toString());
        }
        return false;
    }

    /**
     * 跳转打电话界面
     *
     * @param number
     */
    private void callPhone(String number) {
        //打开电话窗口需要用户手动点击拨打
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + number));
        startActivity(intent);
    }

    /**
     * 打开邮箱应用 处理邮箱请求
     *
     * @param mailUrl
     */
    private void openMail(String mailUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse(mailUrl));
            startActivity(intent);
        } catch (Exception e) {
            LogUtils.e(TAG + " openMail " + e.getMessage());
        }
    }

    /**
     * 3.0-4.4, 4.4.4 input type=file选择文件时调用 用于上传
     *
     * @param valueCallback
     * @param acceptType
     * @param capture
     */
    public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
        //全局保存回调接口
        mUploadMessage = valueCallback;
        //调用图库 或 拍照
        openCameraOrGallery();
    }

    /**
     * 5.0之后 input type=file选择文件时调用 用于上传
     *
     * @param webView
     * @param filePathCallback
     * @param fileChooserParams
     * @return
     */
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        mUploadMessageL = filePathCallback;
        openCameraOrGallery();
        return true;
    }

    /**
     * 选择打开图库/拍照
     */
    boolean isChoose = false;//避免原生选择界面快速多次点击，造成多次调用
    private static final int REQUEST_CODE_CHOOSE = 17001;//图库等调用

    private BottomSheetDialog showBottomSheetDialog(View root, DialogInterface.OnDismissListener callback) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getWActivity());
        bottomSheetDialog.setContentView(root);
        bottomSheetDialog.setOnDismissListener(callback);
        bottomSheetDialog.show();
        return bottomSheetDialog;
    }


    public void openCameraOrGallery() {
        View root = View.inflate(getWActivity(), R.layout.layout_choose_echat, null);
        final BottomSheetDialog bottomSheetDialog = showBottomSheetDialog(root, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!isChoose) {
                    endToUpload();
                }
            }
        });
        LinearLayout layoutCamera = root.findViewById(R.id.ll_camera);
        LinearLayout layoutPhoto = root.findViewById(R.id.ll_photo);
        layoutCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChoose = true;
                bottomSheetDialog.dismiss();
                openCamera();
            }
        });

        layoutPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChoose = true;
                bottomSheetDialog.dismiss();
                openGallery();
            }
        });
    }

    /**
     * 检测读写存储的权限 (>= 6.0需要申请权限)
     *
     * @return
     */
    private boolean checkStoragePermission() {
        return PermissionUtils.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private boolean checkCameraPermission() {
        return PermissionUtils.isGranted(Manifest.permission.CAMERA);
    }

    /**
     * 打开类微信自定义相机
     */
    private void openCamera() {
        PermissionUtils.permission(PermissionConstants.STORAGE, PermissionConstants.CAMERA, PermissionConstants.MICROPHONE)
                .rationale(new PermissionUtils.OnRationaleListener() {
                    @Override
                    public void rationale(ShouldRequest shouldRequest) {
                        showRationaleDialog(shouldRequest, getWActivity());
                    }
                }).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                uploading = true;
                startActivityForResult(new Intent(getWActivity(), CameraActivity.class), CameraActivity.REQUEST_CODE_CUSTOM_CAMERA);
            }

            @Override
            public void onDenied() {
                showOpenAppSettingDialog();
            }
        }).request();
    }

    /**
     * 打开自定义图库 这里用zhihu开源的Matisse
     */
    private void openGallery() {
        LogUtils.iTag(TAG, "openGalaery: " + getWActivity().getPackageName() + ".fileprovider");
        if (checkStoragePermission() && checkCameraPermission()) {
            uploading = true;
            Matisse.from(this)
                    .choose(MimeType.ofAll(), false)
                    .theme(R.style.Matisse_Custom)
                    .capture(true)
                    .captureStrategy(
                            new CaptureStrategy(true, getWActivity().getPackageName() + ".fileprovider", "test"))
                    .maxSelectable(1)
                    .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    .thumbnailScale(0.85f)
                    .imageEngine(new GlideEngine())
                    .originalEnable(false)
                    .autoHideToolbarOnSingleTap(true)
                    .forResult(REQUEST_CODE_CHOOSE);
        } else {
            //申请权限
            PermissionUtils.permission(PermissionConstants.STORAGE, PermissionConstants.CAMERA)
                    .rationale(new PermissionUtils.OnRationaleListener() {
                        @Override
                        public void rationale(ShouldRequest shouldRequest) {
                            showRationaleDialog(shouldRequest, getWActivity());
                        }
                    }).callback(new PermissionUtils.SimpleCallback() {
                @Override
                public void onGranted() {
                    openGallery();
                }

                @Override
                public void onDenied() {
                    showOpenAppSettingDialog();
                }
            }).request();
        }
    }

    private Activity getWActivity() {
        return mActivityWeakReference.get();
    }

    /**
     * 选择文件/照片回调结束
     * 无论用户是否确定发送/取消发送，都必须调用此函数，将UploadMessage回调使用一遍
     * 否者会出现Webview卡死，无法再次上传等问题
     */
    private void endToUpload() {
        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
            result = null;
        }
        if (mUploadMessageL != null) {
            Uri[] results = result != null ? new Uri[]{result} : null;
            mUploadMessageL.onReceiveValue(results);
            mUploadMessageL = null;
            result = null;
        }
    }

    /**
     * 接管图片浏览
     *
     * @param imageUrls
     * @param current
     */
    private void previewImage(ArrayList<String> imageUrls, int current) {
        MNImageBrowser.with(getWActivity())
                .setCurrentPosition(current)
                .setImageEngine(new GlideImageEngine())
                .setImageList(imageUrls)
                .setCustomProgressViewLayoutID(R.layout.layout_custom_progress_view_echat)
                .setIndicatorHide(false)
                .setFullScreenMode(true)
                .show(mWebView);
    }

    /**
     * 接管视频播放
     *
     * @param videoUrl
     * @param thumbUrl
     */
    private void playVideo(String videoUrl, String thumbUrl) {
        //bilibili ijkplayer默认不提供https
        if (videoUrl.startsWith("https:")) {
            videoUrl = videoUrl.replace("https:", "http:");
        }
        JZVideoPlayerStandard.setMediaInterface(new JZMediaIjkplayer());
        JZVideoPlayerStandard.startFullscreen(getWActivity(), JZVideoPlayerStandard.class, videoUrl, "视频");
    }

    public static Uri getFileContentUri(Context context, String absPath) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                , new String[]{MediaStore.Images.Media._ID}
                , MediaStore.Video.Media.DATA + "=? "
                , new String[]{absPath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));

        } else if (!absPath.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, absPath);
            return context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        uploading = false;
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            /**
             * 处理回调对象为空的时候
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mUploadMessageL == null) return;
            } else {
                if (mUploadMessage == null) return;
            }

            final List<Uri> uris = Matisse.obtainResult(intent);
            if (!uris.isEmpty()) {
                result = uris.get(0);
            }


        } else if (requestCode == CameraActivity.REQUEST_CODE_CUSTOM_CAMERA) {
            String path = null;

            if (resultCode == CameraActivity.RESULT_CODE_PICTURE) {
                path = CameraActivity.obtainPicPathResult(intent);
            }

            if (resultCode == CameraActivity.RESULT_CODE_VIDEO) {
                path = CameraActivity.obtainVideoPathResult(intent);
            }

            if (resultCode == CameraActivity.RESULT_CODE_NO_PERMISSION) {
                ToastUtils.showShort("请检查相机权限");
            }
            if (!TextUtils.isEmpty(path)) {
                result = Uri.fromFile(new File(path));
            }

        }

        endToUpload();
        isChoose = false;
    }


    /*-------------EChat----------------*/
    private String chatStatus = "unKnown";
    private int echatPageStatus;//当前页面状态

    private String openUrl;//最后拼接好的Url
    private String openCompanyId;//外部传入的打开的公司id 可能会和👇进行合并
    private String currentCompanyId;
    private String currentCompanyName;

    private String platformSign;
    private String pushInfo;
    private String metaData;
    private String visEvt;
    private String echatTag;


    private String visitorId;//访客id
    private long lastChatTime;
    // TODO: 2019-06-27 转成本地地址 广播发给主APP
    private Bitmap staffHead;
    private int msgboxUreadMsgCount;


    public final static String CHAT_URL = "https://ps.echatsoft.com/visitor/mobile/chat.html";
    public final static String MSGBOX_URL = "https://ps.echatsoft.com/visitor/mobile/platform/msgbox.html";

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessageL;
    private Uri result;
    private String staffNickName;
    private String staffHeadPath;

    /**
     * 初始化聊天窗口
     */
    private void initChatView(String url) {

        //初始化Webview设置
        WebSettings settings = mWebView.getSettings();
        //开启JavaScript支持
        settings.setJavaScriptEnabled(true);
        //默认设置为true，即允许在 File 域下执行任意 JavaScript 代码
        settings.setAllowFileAccess(true);
        //Disable zoom
        settings.setSupportZoom(false);
        //提高渲染优先级
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //网络正常时使用默认缓存策略
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 开启database storage API功能
        settings.setDatabaseEnabled(true);
        String cacheDirPath = getWActivity().getCacheDir().getAbsolutePath();
        // 设置数据库缓存路径
        settings.setDatabasePath(cacheDirPath);
        // 设置Application caches缓存目录
        settings.setAppCachePath(cacheDirPath);
        // 开启Application Cache功能
        settings.setAppCacheEnabled(true);

        //safety settings
        if (Build.VERSION.SDK_INT < 17) {
            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        }
        mWebView.removeJavascriptInterface("accessibility");
        mWebView.removeJavascriptInterface("accessibilityTraversal");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        //允许iframe与外部域名不一致的时候出现的 请求丢失cookie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        }

        /**
         * 给Webview提供EchatJsBridge的JavaScript对象
         */
        try {
            mWebView.addJavascriptInterface(new EchatJavaBridge(), "EchatJsBridge");
        } catch (Exception e) {
            LogUtils.e(e);
        }

        //加载网页
        loadUrl(url);
    }

    class EchatJavaBridge {

        /**
         * This is not called on the UI thread. Post a runnable to invoke
         * <p/>
         * loadUrl on the UI thread.
         */
        @JavascriptInterface
        public void callEchatNative(String paramString) {
            try {

                Log.i("Call From Js: ", paramString);
                LogUtils.json(TAG, paramString);

                JSONObject jsonObject = new JSONObject(paramString);
                final String functionName = jsonObject.optString("functionName");
                final String value = jsonObject.optString("value");

                /**
                 * 获得H5 当前页面状态
                 */
                if ("echatPageStatus".equals(functionName)) {

                    LogUtils.iTag("echatPageStatus", value);
                    JSONObject valueObject = new JSONObject(value);
                    int event = valueObject.optInt("event");
                    echatPageStatus = event;

                    /**
                     * 1:请求对话，消息盒子点击某个商户或者平台时触发此事件（平台版用户会有此事件）
                     * 2:隐藏对话 消息盒子正在进行的对话隐藏后触发（平台版用户会有此事件）
                     * 3:返回主页面 ，在访客请求离开一洽页面时触发.
                     * 4:进入消息盒子页面（平台版用户会有此事件）
                     * 5:进入浏览页面
                     * 6:单独对话窗口打开
                     * 7:浏览页面对话窗口打开
                     * 8:浏览页面隐藏对话窗口
                     */
                    if (event == 1 || event == 6) {
                        JSONObject eventValue = valueObject.optJSONObject("eventValue");
                        currentCompanyId = eventValue.optString("companyId");
                        currentCompanyName = eventValue.optString("companyName");
                        //设置公司名字
                        updateTitle(currentCompanyName);
                    } else if (event == 2 || event == 4) {
                        setUnreadCountMsgboxTitle();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                endChat.setVisible(false);
                            }
                        });
                    } else if (event == 3) {
                        closeChatView();
                    } else if (event == 5) {

                    }

                }

                if ("sendVisitorId".equals(functionName)) {
                    visitorId = value;
                }

                if ("sendCompanyId".equals(functionName)) {
                    currentCompanyId = value;
                }

                if ("sendWebsocketTime".equals(functionName)) {
                    lastChatTime = jsonObject.optLong("value");
                    SPUtils.getInstance().put(SP_LAST_CHAT_TIME, lastChatTime);
                }


                //平台客户新消息
                if ("platformNewMsg".equals(functionName)) {
                    JSONObject newMsgInfo = new JSONObject(value);
                    //获得未读消息数
                    final int unreadMsgCount = newMsgInfo.optInt("unreadMsgCount");
                    if (msgboxUreadMsgCount != unreadMsgCount || msgboxUreadMsgCount == 0) {
                        msgboxUreadMsgCount = unreadMsgCount;
                        sendUnreadCount(msgboxUreadMsgCount, lastChatTime);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //消息盒子显示 最新未读消息数
                                if (echatPageStatus == 2 || echatPageStatus == 4) {
                                    setUnreadCountMsgboxTitle();
                                }
                            }
                        });
                    }

                    /**
                     * 获得平台新消息，sendNewMessage方法内部屏蔽了当msgContent为空时，不进行推送，只更新消息数
                     */
                    String chatUrl = newMsgInfo.optString("chatUrl");
                    String companyIdString = newMsgInfo.optString("companyId");
                    String companyName = newMsgInfo.optString("companyName");
                    String msgContent = newMsgInfo.optString("msgContent");

                    if (echatPageStatus == 2 || echatPageStatus == 4) {//消息盒子 隐藏的时候 要做通知
                        if (!AppUtils.isAppForeground()) {
                            sendNewMessage(chatUrl, companyIdString, companyName, msgContent, Constants.TYPE_NEW_MSG_FROM_PLATFORM);
                        }
                    } else if (echatPageStatus == 1 || echatPageStatus == 6) {//单独对话收到平台消息 要做通知
                        if (!TextUtils.isEmpty(msgContent) || !TextUtils.isEmpty(companyName)) {
                            sendNewMessage(chatUrl, companyIdString, companyName, msgContent, Constants.TYPE_NEW_MSG_FROM_PLATFORM);
                        }
                    }
                }

                /**
                 * 接管播放视频
                 */
                if ("video".equals(functionName)) {
                    JSONObject video = new JSONObject(value);
                    final String videoUrl = video.optString("url");
                    final String thumbUrl = video.getString("thumbUrl");
                    LogUtils.iTag(TAG, "video value = " + video.toString());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            playVideo(videoUrl, thumbUrl);
                        }
                    });

                }

                /**
                 * 原生浏览图片
                 */
                if ("previewImage".equals(functionName)) {
                    JSONObject previewImage = new JSONObject(value);
                    JSONArray urls = previewImage.optJSONArray("urls");
                    ArrayList<String> imageUrls = new ArrayList<>();
                    int current = previewImage.optInt("current");
                    if (urls.length() > 0) {
                        for (int i = 0; i < urls.length(); i++) {
                            final String s = urls.getJSONObject(i).optString("sourceImg");
                            imageUrls.add(s);
                        }
                    }

                    if (imageUrls.size() > 0) {
                        if (current > 0) {
                            current--;
                        }
                        previewImage(imageUrls, current);
                    }
                }

                /**
                 * 打开链接
                 */
                if ("openLinkV2".equals(functionName)) {

                    final JSONObject valueObj = jsonObject.optJSONObject("value");
                    final String openType = valueObj.optString("openType");
                    final String url = valueObj.optString("url");
                    Intent intent = new Intent();
                    intent.putExtra(Constants.EXTRA_BROWER_URL, url);
                    if ("blank".equals(openType)) {
                        intent.setClass(getWActivity(), BrowserActivity.class);
                    } else if ("inner".equals(openType)) {
                        intent.setClass(getWActivity(), WebviewBottomDialogActivity.class);
                    }
                    startActivity(intent);
                }


                /**
                 * 有新消息 多商户可以不处理
                 * 需要在隐藏Webview的情况 接收到newMsg消息 请把onPause和onResume中的操作注释掉
                 * 注意：因为上传文件需要保持JavaScript状态，进行文件上传的时候请勿暂停webiew
                 */
                if (functionName.equals("newMsg")) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            if (!AppUtils.isAppForeground()) {
                                String tempCurrentUrl = mWebView.getUrl();
                                LogUtils.iTag("NewMsg", String.format("NewMsg companyId:%s, currentUrl:%s", currentCompanyId, tempCurrentUrl));
                                sendNewMessage(tempCurrentUrl, currentCompanyId, currentCompanyName, value, Constants.TYPE_NEW_MSG_FROM_CHAT);
                            }

                        }
                    });
                }

                /**
                 * 访客对话状态变更
                 */
                if (functionName.equals("chatStatus")) {
                    chatStatus = value;
                    mHandler.post(new Runnable() {
                        public void run() {
                            handleMulttMerChantStatus();
                            Log.i("JS Call", "functionName: " + functionName + ",  value: " + value);
                        }
                    });

                }

                /**
                 * 访客评价反馈
                 */
                if (functionName.equals("visitorEvaluate")) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            handleVisitorEvaluate(value);
                            Log.i("JS Call", functionName + "." + value);
                        }
                    });
                }

                /**
                 * 接入对话的客服信息
                 */
                if (functionName.equals("chatStaffInfo")) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            //handleStaffName(value);
                            downloadStaffHead(value);
                            Log.i("JS Call", functionName + "." + value);
                        }
                    });
                }

                /**
                 * 通知APP隐藏对话窗口
                 */
                if (functionName.equals("visitorHide")) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            LogUtils.iTag(BACK_EVENT_TAG, "visitorHide nativeBackClick");
                            nativeBackClick();
                        }
                    });
                }

            } catch (Exception e) {
                LogUtils.eTag("Exception", e.getLocalizedMessage());
                LogUtils.eTag("Exception", e);
            }
        }
    }


    private void sendNewMessage(String chatUrl, String companyIdString, String companyName, String msgContent, int newMsgType) {
        LogUtils.iTag(TAG, "sendNewMessage");
        if (!TextUtils.isEmpty(msgContent) || !TextUtils.isEmpty(companyName)) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.CHAT_NEW_MSG_TYPE, newMsgType);
            bundle.putString(Constants.EXTRA_CHAT_URL, chatUrl);
            bundle.putString(Constants.CHAT_COMPANY_ID, companyIdString);
            bundle.putString(Constants.CHAT_COMPANY_NAME, companyName);
            bundle.putString(Constants.CHAT_MSG_CONTENT, msgContent);
            intent.putExtras(bundle);
            intent.setAction(ACTION_NEW_MSG);
            getWActivity().sendBroadcast(intent, Constants.BroadcastPermission.MESSAGE_RECEIVE_PERMISSION);
        }
    }

    private void sendUnreadCount(int unreadMsgCount, long lastChatTime) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(CHAT_UNREAD_COUNT, unreadMsgCount);
        bundle.putLong(CHAT_LAST_CHAT_TIME, lastChatTime);
        intent.putExtras(bundle);
        intent.setAction(ACTION_UNREAD_COUNT);
        getWActivity().sendBroadcast(intent, Constants.BroadcastPermission.MESSAGE_RECEIVE_PERMISSION);
    }


    /**
     * close current chat activity(fragment)
     */
    private void closeChatView() {
        getWActivity().finish();
    }

    private void setUnreadCountMsgboxTitle() {
        String title = "消息";
        if (msgboxUreadMsgCount != 0) {
            title += String.format("(%d)", msgboxUreadMsgCount);
        }
        String finalTitle = title;
        updateTitle(finalTitle);
    }

    private void updateTitle(String title) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    toolbar.setTitle(title);
                }
            });
        } else {
            toolbar.setTitle(title);
        }
    }

    /**
     * 多商户对话中按钮处理 (关闭按钮+是否回退)
     */
    private void handleMulttMerChantStatus() {
        if (chatStatus.equals("unKnown")) {
            endChat.setVisible(false);
        } else if (chatStatus.equals("waiting")) {
            endChat.setVisible(true);
        } else if (chatStatus.equals("chatting")) {
            endChat.setTitle("结束对话");
            endChat.setVisible(true);
        } else if (chatStatus.equals("leaveDisabled")) {
            endChat.setVisible(false);
        } else if (chatStatus.equals("leaveToService")) {
            endChat.setTitle("结束留言");
            endChat.setVisible(true);
        } else if (chatStatus.equals("leaveToUrl")) {
            endChat.setVisible(false);
        } else if (chatStatus.equals("robot")) {
            endChat.setVisible(true);
        } else if (chatStatus.contains("end")) {
            endChat.setVisible(false);
            String env = chatStatus.split("-")[2];
            String visitor = chatStatus.split("-")[1];
            if ("0".equals(env) && "1".equals(visitor)) {//不需要评价
                LogUtils.iTag(BACK_EVENT_TAG, String.format("pageEvent:%s", echatPageStatus));
                if (echatPageStatus == 1) {
                    LogUtils.iTag(BACK_EVENT_TAG, String.format("echatPageStatus == 1 ,pageEvent:%s", echatPageStatus));
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LogUtils.iTag(BACK_EVENT_TAG, String.format("postDelayed ,pageEvent:%s", echatPageStatus));
                            if (!(echatPageStatus == 2 || echatPageStatus == 4)) {
                                LogUtils.iTag(BACK_EVENT_TAG, String.format("postDelayed ,pageEvent:%s", echatPageStatus));
                                nativeBackClick();
                            }
                        }
                    }, 300);
                } else {
                    if (!(echatPageStatus == 2 || echatPageStatus == 4)) {
                        nativeBackClick();
                    }
                }
            }
        }
    }

    private static final String BACK_EVENT_TAG = "Chat_Back";

    private void handleVisitorEvaluate(String value) {
        String status = value.split("-")[1];
        LogUtils.iTag(BACK_EVENT_TAG, status);
        if (status.equals("2")) {
            if (!(echatPageStatus == 2 || echatPageStatus == 4)) {
                nativeBackClick();
            }
        }
    }

    /**
     * 消息盒子/对话 处理不同页面状态下的返回操作
     */
    private void nativeBackClick() {
        LogUtils.iTag(BACK_EVENT_TAG, String.format("postDelayed ,pageEvent:%s", echatPageStatus));
        if (echatPageStatus == 1) {//1、消息盒子 -> 已打开对话窗口
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                callJs(mWebView, "echatBackEvent", null);
            }
        } else if (echatPageStatus == 6) {//6、直接打开对话 直接打开对话
            //判断当前对话是否跳转出去
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                closeChatView();
                echatPageStatus = 0;
            }

        } else if (echatPageStatus == 2 || echatPageStatus == 4) {//2、隐藏对话窗口 -> 推断是消息盒子 4、进入消息盒子
            closeChatView();
            echatPageStatus = 0;
        } else if (echatPageStatus == 3) {//3、返回主页面 ，在访客请求离开消息盒子页面时触发.
            closeChatView();
            echatPageStatus = 0;
        } else if (echatPageStatus == 5) {//5、进入浏览页面 （用户刘总）
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            }
        } else if (echatPageStatus == 0) {//为正确获得页面状态 直接返回
            closeChatView();
        }
    }

    /**
     * 获得客服昵称
     *
     * @param value
     */
    private void handleStaffName(String value) {
        try {
            JSONObject jsonObject = new JSONObject(value);
            staffNickName = jsonObject.getString("staffNickName");
            if (!TextUtils.isEmpty(staffNickName)) {
                toolbar.setTitle(staffNickName);
            }
        } catch (Exception e) {
        }

    }

    /**
     * 下载客服头像备用
     *
     * @param value
     */
    private void downloadStaffHead(String value) {
        try {
            JSONObject jsonObject = new JSONObject(value);
            final String staffHeadUrl = jsonObject.getString("staffHead");
            staffNickName = jsonObject.getString("staffNickName");
            new Thread(new Runnable() {
                @Override
                public void run() {

                    RequestUtils.getInstance(getWActivity()).downLoadFile(
                            staffHeadUrl,
                            EncryptUtils.encryptMD5ToString(staffHeadUrl),
                            PathUtils.getInternalAppFilesPath(),
                            new RequestUtils.ReqCallBack<File>() {
                                @Override
                                public void onReqSuccess(File result) {
                                    staffHead = ImageUtils.getBitmap(result);
                                }

                                @Override
                                public void onReqFailed(String errorMsg) {

                                }
                            }
                    );
                }
            }).start();
        } catch (Exception e) {
        }
    }


}
