package com.github.echatmulti.sample.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.blankj.utilcode.util.ImageUtils;
import com.github.echat.chat.EChatActivity;
import com.github.echatmulti.sample.MainActivity;
import com.github.echatmulti.sample.R;

import java.util.Map;

import static com.github.echat.chat.utils.Constants.EXTRA_CHAT_URL;
import static com.github.echat.chat.utils.Constants.EXTRA_COMPANY_ID;
import static com.github.echat.chat.utils.Constants.EXTRA_NOTIFY;

/**
 * @Author: xuhaoyang
 * @Email: xuhaoyang3x@gmail.com
 * @program: android-demo
 * @create: 2019-02-13
 * @describe
 */
public class RemoteNotificationUtils extends ContextWrapper {


    public final static String DEFAULT_CHANNEL = "EChatDefaultChannel";
    public final static String group_primary = "echat_group";
    public final static String DEFAULT_CHANNEL_DESCRIPTION = "EChat默认通知渠道";
    public final static String FOREGROUND_CHANNEL = "EChatForegroundChannel";
    public final static String FOREGROUND_CHANNEL_DESCRIPTION = "EChat前台";
    private NotificationManager manager;
    private int NOTIFICATION_ID = 0;
    private static int count = 0;

    private static RemoteNotificationUtils utils;

    private RemoteNotificationUtils(Context base) {
        super(base);
    }

    public static RemoteNotificationUtils getInstance(Context context) {
        if (utils == null) {
            synchronized (RemoteNotificationUtils.class) {
                if (utils == null) {
                    utils = new RemoteNotificationUtils(context);
                }
            }
        }
        return utils;
    }

    public void showNotification(String title, String content, Long time) {
        showNotification(title, content, null, null, null);
    }

    public void showNotification(String title, String content, Map<String, String> params) {
        showNotification(title, content, null, null, params);
    }

    public void showNotification(String title, String content, Long time, Bitmap staffHead, Map<String, String> params) {
        Intent mainIntent = new Intent(this, MainActivity.class);
        Intent result = new Intent(this, EChatActivity.class);
        result.putExtra(EXTRA_NOTIFY, true);
        if (params != null) {
            result.putExtra(EXTRA_COMPANY_ID, params.get(EXTRA_COMPANY_ID));
            result.putExtra(EXTRA_CHAT_URL, params.get(EXTRA_CHAT_URL));
        }
        result.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Intent[] intents = new Intent[]{mainIntent, result};
        PendingIntent pendingIntent = PendingIntent.getActivities(this, (int) (Math.random() * 100), intents, PendingIntent.FLAG_UPDATE_CURRENT);


        if (TextUtils.isEmpty(title)) {
            title = "客服";
        }

        if (count > 1) {
            content = "[你有" + count + "条消息]" + content;
        }

        NotificationCompat.Builder builder = getNofity(title, content, time)
                .setContentIntent(pendingIntent);

        if (staffHead != null) {
            builder.setLargeIcon(staffHead);
        }

        builder.setNumber(count);
        final Notification notification = builder.build();

        getManager().notify(NOTIFICATION_ID, notification);
    }

    public RemoteNotificationUtils setCount(int count) {
        RemoteNotificationUtils.count = count;
        return this;
    }

    public RemoteNotificationUtils setNotificationId(int notificationId) {
        NOTIFICATION_ID = notificationId;
        return this;
    }

    private NotificationCompat.Builder getNofity(String title, String content, Long time) {
        return new NotificationCompat.Builder(getApplicationContext(), DEFAULT_CHANNEL)
//                .setTicker(getString(R.string.android_auto_update_notify_ticker))
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(getSmallIcon())
                .setLargeIcon(getLargeIcon())
                .setWhen(time == null ? System.currentTimeMillis() : time)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);
    }

    public NotificationCompat.Builder getForegroundNotificationBuilder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelId(FOREGROUND_CHANNEL, FOREGROUND_CHANNEL_DESCRIPTION, NotificationManagerCompat.IMPORTANCE_NONE);
        }
        return new NotificationCompat.Builder(getApplicationContext(), FOREGROUND_CHANNEL);
    }

    public static void initNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannelGroup eChat_group = new NotificationChannelGroup(group_primary, "EChat Group");
            manager.createNotificationChannelGroup(eChat_group);
            NotificationChannel mChannel = new NotificationChannel(DEFAULT_CHANNEL,
                    DEFAULT_CHANNEL_DESCRIPTION,
                    NotificationManager.IMPORTANCE_HIGH);
            mChannel.setLightColor(Color.GREEN);
            mChannel.setGroup(group_primary);
            mChannel.enableLights(true);
            //锁屏的时候是否展示通知
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            manager.createNotificationChannel(mChannel);
        }
    }


    /**
     * 创建通知渠道 兼容Android 8.0
     *
     * @param channelId
     * @param channelName
     * @param importance
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public void createChannelId(String channelId, String channelName, int importance) {
        createNotificationGroup();

        NotificationChannel mChannel = new NotificationChannel(channelId,
                channelName,
                importance);
        mChannel.setLightColor(Color.GREEN);
        mChannel.setGroup(group_primary);
        mChannel.enableLights(true);
        //锁屏的时候是否展示通知
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(mChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationGroup() {
        NotificationChannelGroup ncp1 = new NotificationChannelGroup(group_primary, "EChat Group");
        getManager().createNotificationChannelGroup(ncp1);
    }

    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    private int getSmallIcon() {
        //设置 nofication 的图标 直接读取小米推送配置的图标
        int icon = getResources().getIdentifier("mipush_small_notification", "drawable", getPackageName());
        if (icon == 0) {
            icon = getApplicationInfo().icon;
        }

        return icon;
    }

    private Bitmap getLargeIcon() {
        int bigIcon = getResources().getIdentifier("mipush_notification", "drawable", getPackageName());
        if (bigIcon != 0) {
            return BitmapFactory.decodeResource(getResources(), bigIcon);
        } else {
            return ImageUtils.getBitmap(R.drawable.ic_logo);
        }
    }

    public void cancel() {
        getManager().cancel(NOTIFICATION_ID);
    }

    public void cancel(int notification_id) {
        getManager().cancel(notification_id);
    }

    public static void cancelAll(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
    }

    public static void cancel(Context context, int companyId) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(companyId);
    }
}