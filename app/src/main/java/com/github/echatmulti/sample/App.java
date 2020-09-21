package com.github.echatmulti.sample;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.BottomSheetDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.echat.chat.EChatCore;
import com.github.echat.chat.utils.Constants;
import com.github.echat.chat.utils.EChatUtils;
import com.github.echatmulti.sample.utils.RemoteNotificationUtils;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.xiaomi.MiPushRegistar;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.echatmulti.sample.utils.Constants.ACTION_DEVICE_TOKEN;
import static com.github.echatmulti.sample.utils.Constants.APPID;
import static com.github.echatmulti.sample.utils.Constants.APPID_DEFAULT;
import static com.github.echatmulti.sample.utils.Constants.DEVICE_TOKEN_FUN;
import static com.github.echatmulti.sample.utils.Constants.METADATA_ONLY_UID;
import static com.github.echatmulti.sample.utils.Constants.TOKEN;
import static com.github.echatmulti.sample.utils.Constants.TOKEN_DEFAULT;

public class App extends Application {

    private final static String TAG = "EChatMulti_N";

    public static Handler handler;

    // TODO: 2019-11-22 handler go to get
    private class Runhandler extends Handler {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        initLogutils();

        //二维码
        ZXingLibrary.initDisplayOpinion(this);

        //初始化通知
        RemoteNotificationUtils.initNotificationChannel(this);
        initUpush();

        //解决9.0 不同进程的webview冲突解决方式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (getPackageName().equals(getProcessName(this))) {
                WebView.setDataDirectorySuffix("core");
            } else if ((getPackageName() + ":webview").equals(getProcessName(this))) {
                WebView.setDataDirectorySuffix("second");
            }
        }

        interceptOpenLink();

    }

    /**
     * 处理一洽打开特定地址 调用原生发送图文消息
     */
    private void interceptOpenLink() {
        //拦截模块内的openLink
        EChatCore.getInstance().setCallback(new EChatCore.Callback() {
            @Override
            public boolean openLink(Context context, String url, String type) {
                final Uri uri = Uri.parse(url);
                LogUtils.i(uri);
                final String host = uri.getHost();
                final String visitorId = uri.getQueryParameter("visitorId");
                final String companyId = uri.getQueryParameter("companyId");

                if ("echatdemo".equals(host)) {//拦截
                    View root = View.inflate(context, R.layout.dialog_layout_order, null);

                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                    bottomSheetDialog.setContentView(root);
                    bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    bottomSheetDialog.show();
                    final TextView priceTv = root.findViewById(R.id.pricetv);
                    final TextView logisticsTv = root.findViewById(R.id.logisticsTv);
                    final ImageView ivToolbarNavigation = (ImageView) root.findViewById(com.github.echat.chat.R.id.ivToolbarNavigation);
                    final LinearLayout llToolbarClose = (LinearLayout) root.findViewById(com.github.echat.chat.R.id.llToolbarClose);
                    priceTv.setText(Html.fromHtml("<font color='#000'>实付：</font><font color='#ff3366'> ¥199.60</font>"));
                    logisticsTv.setText(Html.fromHtml("<font color='#000'>物流：</font>买家已收货"));
                    //事件
                    llToolbarClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                        }
                    });
                    ivToolbarNavigation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                        }
                    });
                    root.findViewById(R.id.btn_send_visevt).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            JSONObject object = new JSONObject();
                            final String appid = SPUtils.getInstance().getString(APPID, APPID_DEFAULT);
                            final String token = SPUtils.getInstance().getString(TOKEN, TOKEN_DEFAULT);
                            final String metaData = SPUtils.getInstance().getString(METADATA_ONLY_UID, null);
                            try {
                                object.putOpt("eventId", "D97483381");
                                object.putOpt("title", "订单号：D97483381");
                                object.putOpt("content", "<div style=\\'color:#666;line-height:20px\\'>BADDIARY-2016秋季新款韩版高低摆连衣裙腰带套装</div><div style=\\'color:#666;line-height:20px\\'>金额：<span style=\\'color:red\\'>¥199.60</span></div><div style=\\'color:#666;line-height:20px\\'>物流：<span style=\\'color:#ccc\\'>买家已收货</span></div>");
                                object.putOpt("imageUrl", "https://demo.echatsoft.com/web/html/demoMall/url/visitorUrl/myorder/images/1.jpg");
                                object.putOpt("urlForVisitor", "http('https://demo.echatsoft.com/web/html/demoMall/url/staffUrl/myorder/order.asp?eventId=D97483381','inner')");
                                object.putOpt("urlForStaff", "http('https://demo.echatsoft.com/web/html/demoMall/url/staffUrl/myorder/order.asp?eventId=D97483381','inner')");
                                object.putOpt("memo", "下单时间：2018/12/03-10:30");
                                EChatUtils.sendVisEvt(context, companyId, URLEncoder.encode(metaData, "UTF-8"), null, null, object.toString(), new EChatUtils.SendVisEvtCallback() {
                                    @Override
                                    public void onStatus(boolean flag, String message) {
                                        if (flag) {
                                            bottomSheetDialog.dismiss();
                                        } else {
                                            ToastUtils.showShort("发送失败: " + message);
                                        }
                                    }
                                });
                            } catch (Exception e) {
                            }
                        }
                    });

                    return true;
                }
                return false;
            }
        });
    }


    private void initLogutils() {

        String logfile = PathUtils.getExternalAppCachePath() + File.separator + "EChat";
        LogUtils.iTag(TAG, "initLog: 写入目录 " + logfile);
        LogUtils.Config config = LogUtils.getConfig()
                .setLogSwitch(true)// 设置log总开关，包括输出到控制台和文件，默认开
                .setConsoleSwitch(BuildConfig.DEBUG)// 设置是否输出到控制台开关，默认开
                .setFilePrefix(TAG)
                // 当全局标签不为空时，我们输出的log全部为该tag，
                // 为空时，如果传入的tag为空那就显示类名，否则显示tag
                .setLogHeadSwitch(true)// 设置log头信息开关，默认为开
                .setLog2FileSwitch(true)// 打印log时是否存到文件的开关，默认关
                .setSingleTagSwitch(false)// 一条日志仅输出一条，默认开，为美化 AS 3.1 的 Logcat
                .setDir(logfile)// 当自定义路径为空时，写入应用的/cache/log/目录中
                .setBorderSwitch(false)// 输出日志是否带边框开关，默认开
                .setStackDeep(1)//log 栈深度，默认为 1
                .setConsoleFilter(LogUtils.V)// log的控制台过滤器，和logcat过滤器同理，默认Verbose
                .setFileFilter(LogUtils.V)// log文件过滤器，和logcat过滤器同理，默认Verbose
                // 新增 ArrayList 格式化器，默认已支持 Array, Throwable, Bundle, Intent 的格式化输出
                .addFormatter(new LogUtils.IFormatter<ArrayList>() {
                    @Override
                    public String format(ArrayList arrayList) {
                        return "LogUtils Formatter ArrayList { " + arrayList.toString() + " }";
                    }
                });
        LogUtils.d(config.toString());
    }


    private void initUpush() {

        MiPushRegistar.register(this, BuildConfig.XIAOMI_PUSH_ID, BuildConfig.XIAOMI_PUSH_KEY);

        HuaWeiRegister.register(this);
        //设置LOG开关，默认为false
        UMConfigure.setLogEnabled(false);

        //初始化组件化基础库, 统计SDK/推送SDK/分享SDK都必须调用此初始化接口
        UMConfigure.init(this, BuildConfig.UMENG_PUSH_APPKEY, "EChatStore", UMConfigure.DEVICE_TYPE_PHONE,
                BuildConfig.UMENG_MESSAGE_SECRET);

        PushAgent mPushAgent = PushAgent.getInstance(this);
        //sdk开启通知声音
        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);
        // 通知声音由服务端控制
        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SERVER);

        UmengMessageHandler messageHandler = new UmengMessageHandler() {

            /**
             * 通知的回调方法（通知送达时会回调）
             */
            @Override
            public void dealWithNotificationMessage(Context context, UMessage uMessage) {
                //super.dealWithNotificationMessage(context, uMessage);
                //调用super，会展示通知，不调用super，则不展示通知。

                final RemoteNotificationUtils notification = RemoteNotificationUtils.getInstance(context);
                final String chatCompanyId = uMessage.extra.get("chatCompanyId");
                final String echatUrl = uMessage.extra.get("echatUrl");
                final String remoteUnreadMsgCount = uMessage.extra.get("unreadMsgCount");
                final String echatTimeStampString = uMessage.extra.get("echatTimeStamp");

                long echatTimeStamp = Long.valueOf(echatTimeStampString);
                final long lastChatTime = EChatUtils.getLastChatTime();
                //判断当前消息是否应该推送[lastChatTime 意指 上次已知最后一次进入对话窗口/访客发送消息时间戳/接受到远程消息时间戳/获取访客未读消息条数接口获得时间戳]
                //用于排除远程消息/厂商消息延后到达 避免不必要的通知
                //避免 因远程推送延迟到达 错误更新对话最后一条消息
                if (lastChatTime < echatTimeStamp) {

                    LogUtils.iTag(TAG, "收到远程推送: 把content：" + uMessage.text + ", 存储到本地 用于消息列表显示");
                    if (!TextUtils.isEmpty(uMessage.text)) {
                        EChatUtils.sendLastChatInformation(getApplicationContext(), uMessage.text, echatTimeStamp);
                    } else {
                        EChatUtils.setLastChatInformation(null, echatTimeStamp);
                    }
                    //通知一下UI 更新通知数并保存本地
                    EChatUtils.sendRemoteUnreadCount(getApplicationContext(), Integer.valueOf(remoteUnreadMsgCount), echatTimeStamp);

                    final int localUnreadCount = EChatUtils.getLocalUnreadCount();
                    notification
                            .setNotificationId(Integer.parseInt(chatCompanyId))
                            .setCount(localUnreadCount + Integer.valueOf(remoteUnreadMsgCount))
                            .showNotification(uMessage.title, uMessage.text, new HashMap<String, String>() {{
                                put(Constants.EXTRA_CHAT_URL, echatUrl);
                                put(Constants.EXTRA_COMPANY_ID, chatCompanyId);
                            }});

                    getRemoteUnread(context, chatCompanyId, localUnreadCount, uMessage.title, echatUrl);
                }

                LogUtils.d(TAG, "这是友盟消息推送 回调：" + uMessage.getRaw().toString());
                LogUtils.d(TAG, "UMessage custom : " + uMessage.extra);
            }

            @Override
            public void dealWithCustomMessage(Context context, UMessage uMessage) {
                /**
                 * 自定义消息的回调方法
                 */
                LogUtils.d(TAG, uMessage);
            }

            /**
             * 自定义通知栏样式的回调方法
             */
            @Override
            public Notification getNotification(Context context, UMessage msg) {
                switch (msg.builder_id) {
                    default:
                        return super.getNotification(context, msg);
                }
            }
        };

        mPushAgent.setMessageHandler(messageHandler);

        //注册推送服务 每次调用register都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                LogUtils.d(TAG, "UM device token: " + deviceToken);
                SPUtils.getInstance().put(DEVICE_TOKEN_FUN, deviceToken);

                Intent intent = new Intent(ACTION_DEVICE_TOKEN);
                intent.putExtra(DEVICE_TOKEN_FUN, deviceToken);
                sendBroadcast(intent);
            }

            @Override
            public void onFailure(String s, String s1) {
                LogUtils.d(TAG, "UM register failed: " + s + " " + s1);
            }
        });
    }

    private volatile boolean isRequest = false;

    private void getRemoteUnread(Context context, String companyId, int localCount, String title, String echatUrl) {
        if (!isRequest) {
            isRequest = true;
            final String metaData = SPUtils.getInstance().getString(METADATA_ONLY_UID, null);
            EChatUtils.getUnreadCount(this,
                    companyId,
                    metaData,
                    null,
                    null,
                    new EChatUtils.GetUnreadCountCallback() {
                        @Override
                        public void onAPIChange(int count, String content, Long tm) {
                            LogUtils.iTag("UNREAD", String.format("unread count :%d ，last content:%s, timestamp : %d", count, content, tm), "请求完毕");
                            isRequest = false;
                            long notificationTm = EChatUtils.getLastChatTime();
                            if (notificationTm < tm) {
                                //更新时间戳
                                EChatUtils.sendLastChatInformation(getBaseContext(), content, tm);
                                EChatUtils.sendRemoteUnreadCount(getBaseContext(), count, tm);
                                //修改新通知

                                final RemoteNotificationUtils notification = RemoteNotificationUtils.getInstance(context);
                                notification
                                        .setNotificationId(Integer.parseInt(companyId))
                                        .setCount(localCount + count)
                                        .showNotification(title, content, new HashMap<String, String>() {{
                                            put(Constants.EXTRA_CHAT_URL, echatUrl);
                                            put(Constants.EXTRA_COMPANY_ID, companyId);
                                        }});
                            }
                        }

                        @Override
                        public void fail(int errocde, String msg) {
                            LogUtils.eTag("UNREAD", errocde, msg, "请求完毕");
                            isRequest = false;
                        }
                    }
            );
        }
    }

    /**
     * 获得进程名字
     *
     * @return
     */
    public static String getProcessName(Context context) {
        int count = 0;
        do {
            String processName = getProcessNameImpl(context);
            if (!TextUtils.isEmpty(processName)) {
                return processName;
            }
        } while (count++ < 3);

        return null;
    }

    public static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Exception e) {
            LogUtils.e("getProcessName read is fail. exception=" + e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                LogUtils.e("getProcessName close is fail. exception=" + e);
            }
        }
        return null;
    }

    private static String getProcessNameImpl(Context context) {
        // get by ams
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
        if (processes != null) {
            int pid = android.os.Process.myPid();
            for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
                if (processInfo.pid == pid && !TextUtils.isEmpty(processInfo.processName)) {
                    return processInfo.processName;
                }
            }
        }

        // get from kernel
        String ret = getProcessName(android.os.Process.myPid());
        if (!TextUtils.isEmpty(ret) && ret.contains(context.getPackageName())) {
            return ret;
        }

        return null;
    }

}
