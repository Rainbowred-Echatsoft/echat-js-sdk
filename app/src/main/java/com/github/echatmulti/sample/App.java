package com.github.echatmulti.sample;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.WebView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.echat.chat.utils.Constants;
import com.github.echatmulti.sample.utils.RemoteNotificationUtils;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.xiaomi.MiPushRegistar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.echat.chat.utils.Constants.ACTION_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.CHAT_LAST_CHAT_TIME;
import static com.github.echat.chat.utils.Constants.CHAT_UNREAD_COUNT;
import static com.github.echatmulti.sample.utils.Constants.ACTION_DEVICE_TOKEN;
import static com.github.echatmulti.sample.utils.Constants.DEVICE_TOKEN_FUN;
import static com.github.echatmulti.sample.utils.Constants.LASTCHAT;
import static com.github.echatmulti.sample.utils.Constants.UNREAD_COUNT;

public class App extends Application {

    private final static String TAG = "EChatMulti_N";

    public static Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        initLogutils();

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
                final String unreadMsgCount = uMessage.extra.get("unreadMsgCount");
                final String echatTimeStampString = uMessage.extra.get("echatTimeStamp");

                long echatTimeStamp = Long.valueOf(echatTimeStampString);
                final long lastChatTime = SPUtils.getInstance().getLong(LASTCHAT, 0l);
                //判断当前消息是否应该推送[lastChatTime上次已知最后一次进入对话窗口/接受到远程消息时间戳]
                //用于排除远程消息/厂商消息延后到达 避免不必要的通知
                if (lastChatTime < echatTimeStamp) {

                    //通知一下UI 更新通知数
                    SPUtils.getInstance().put(UNREAD_COUNT, Integer.valueOf(unreadMsgCount));
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt(CHAT_UNREAD_COUNT, Integer.valueOf(unreadMsgCount));
                    bundle.putLong(CHAT_LAST_CHAT_TIME, echatTimeStamp);
                    intent.putExtras(bundle);
                    intent.setAction(ACTION_UNREAD_COUNT);
                    sendBroadcast(intent, Constants.BroadcastPermission.MESSAGE_RECEIVE_PERMISSION);

                    notification
                            .setNotificationId(Integer.parseInt(chatCompanyId))
                            .setCount(0)
                            .showNotification(uMessage.title, uMessage.text, new HashMap<String, String>() {{
                                put(Constants.EXTRA_CHAT_URL, echatUrl);
                                put(Constants.EXTRA_COMPANY_ID, chatCompanyId);
                            }});
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
