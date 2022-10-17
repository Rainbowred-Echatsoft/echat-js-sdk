package com.echatsoft.echatsdk.push;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.heytap.msp.push.HeytapPushManager;
import com.umeng.commonsdk.UMConfigure;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.mezu.MeizuRegister;
import org.android.agoo.oppo.OppoRegister;
import org.android.agoo.vivo.VivoRegister;
import org.android.agoo.xiaomi.MiPushRegistar;

/**
 * User: xuhaoyang
 * mail: xuhaoyang3x@gmail.com
 * Date: 2021/6/6
 * Description:
 * Umeng doc: https://developer.umeng.com/docs/67966/detail/173238
 */
public class PushManager {

    public static final String TAG    = "PushManager";
    public static final String CHANEL = "umeng";

    public static String  UMENG_APPKEY;// 友盟申请的App key
    public static String  UMENG_MESSAGE_SECRET;// 友盟申请的UmengMessageSecret
    public static String  UMENG_APP_MASTER_SECRET;//友盟申请的APP_MASTER_SECRET 后台加密消息的密码
    public static String  XIAOMI_ID;//小米后台APP对应的xiaomi id
    public static String  XIAOMI_KEY;//小米后台APP对应的xiaomi key
    public static String  MEI_ZU_ID;//魅族后台APP对应的app id
    public static String  MEI_ZU_KEY;//魅族后台APP对应的app key
    public static String  OPPO_KEY; //OPPO后台APP对应的app key
    public static String  OPPO_SECRET;//OPPO后台APP对应的app secret
    public static boolean DEBUG;//umeng debug开关

    //注意要合规
    public static void preInit(Context context) {
        UMConfigure.preInit(context, UMENG_APPKEY, CHANEL);
    }

    /**
     * @param application
     */
    public static void init(Application application) {
        Log.i(TAG, "remote push init");
        if (TextUtils.isEmpty(UMENG_APPKEY) ||
                TextUtils.isEmpty(UMENG_MESSAGE_SECRET)) {
            Log.e(TAG, "remote push error, not to init ");
            return;
        }

        UMConfigure.setLogEnabled(DEBUG);
        UMConfigure.init(
                application,
                UMENG_APPKEY,
                CHANEL,
                UMConfigure.DEVICE_TYPE_PHONE,
                UMENG_MESSAGE_SECRET
        );

        registerDeviceChannel(application);
    }

    public static void requestNotificationPermission(Context context) {
        if (!isNotificationEnabled(context)) {
            Log.i(TAG, "no notification permission");
            if (RomUtils.isOppo()) {
                requestOppoNotificationPermission(context);
            } else {
                openPushMenu(context);
            }
        }
    }

    public static void requestOppoNotificationPermission(Context context) {
        Log.i(TAG, "oppo handle");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            HeytapPushManager.requestNotificationPermission();
        } else {
            openPushMenu(context);
        }
    }

    public static void openPushMenu(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
            context.startActivity(intent);
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "openPushMenu", e);
            }
        } else {
            Intent localIntent = new Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(localIntent);
        }
    }

    public static boolean isNotificationEnabled(Context context) {
        return NotificationManagerCompat.from(context.getApplicationContext()).areNotificationsEnabled();
    }

    public static void registerDeviceChannel(Application application) {

        if (RomUtils.isHuawei()) {
            Log.i(TAG, "remote push HUAWEI init");
            //未使用 请注释
            HuaWeiRegister.register(application);
        }

        if (RomUtils.isXiaomi() &&
                !TextUtils.isEmpty(XIAOMI_ID) &&
                !TextUtils.isEmpty(XIAOMI_KEY)) {
            Log.i(TAG, "remote push XIAOMI init");
            //check MiPushBroadcastReceiver
            //未使用 请注释
            MiPushRegistar.register(application, XIAOMI_ID, XIAOMI_KEY);
        }

        if (RomUtils.isMeizu()) {
            //未使用 请注释
            MeizuRegister.register(application, MEI_ZU_ID, MEI_ZU_KEY);
        }

        //vivo，注意vivo通道的初始化参数在minifest中配置
        if (RomUtils.isVivo()) {
            //未使用 请注释
            VivoRegister.register(application);
        }

        if (RomUtils.isOppo()) {
            //未使用 请注释
            OppoRegister.register(application, OPPO_KEY, OPPO_SECRET);
        }
    }
}
