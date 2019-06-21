package com.echat.echatjsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.echat.chat.EChatActivity;
import com.github.echat.chat.utils.Constants;
import com.github.echatmulti.sample.MainActivity;
import com.github.echatmulti.sample.utils.RemoteNotificationUtils;
import com.umeng.message.UmengNotifyClickActivity;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.android.agoo.common.AgooConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.github.echat.chat.utils.Constants.EXTRA_CHAT_URL;
import static com.github.echat.chat.utils.Constants.EXTRA_COMPANY_ID;
import static com.github.echat.chat.utils.Constants.EXTRA_NOTIFY;
import static com.github.echatmulti.sample.utils.Constants.LASTCHAT;
import static com.github.echatmulti.sample.utils.Constants.UNREAD_COUNT;

/**
 * @Author: xuhaoyang
 * @Email: xuhaoyang3x@gmail.com
 * @program: android-demo
 * @create: 2019-02-15
 * @describe 友盟的厂家推送渠道 所用的Activity
 */
//com.echat.echatjsdemo.MiPushActivity
public class MiPushActivity extends UmengNotifyClickActivity {

    private static String TAG = MiPushActivity.class.getName();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public void onMessage(Intent intent) {
        super.onMessage(intent);
        String body = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
        LogUtils.i(TAG, body);
        //{"display_type":"notification","extra":{"unreadMsgCount":"1","chatCompanyId":"500029","echatUrl":"https://ps.echatsoft.com/visitor/mobile/chat.html?companyId=500029&echatTag=app_platform&pushInfo=Ai6Jvchzk1r6mcD5BqU8oQCcEV_X1MGOdSeM3j0A290x&myData=","echatTimeStamp":"1561978802951"},"msg_id":"uuqi9xn156197880507101","body":{"after_open":"go_app","ticker":"Echat","text":"消息2","title":"一洽@安卓开发"},"random_min":0}
        try {
            JSONObject jsonObject = new JSONObject(body);
            final JSONObject extra = jsonObject.optJSONObject("extra");
            final String chatCompanyId = extra.optString("chatCompanyId");
            final String echatUrl = extra.optString("echatUrl");
            final String unreadMsgCount = extra.optString("unreadMsgCount");
            final String echatTimeStampString = extra.optString("echatTimeStamp");

            long echatTimeStamp = Long.valueOf(echatTimeStampString);
            final long lastChatTime = SPUtils.getInstance().getLong(LASTCHAT, 0l);
            //判断当前消息是否应该推送[lastChatTime上次已知最后一次进入对话窗口/接受到远程消息时间戳]
            //用于排除远程消息/厂商消息延后到达 避免不必要的通知
            if (lastChatTime < echatTimeStamp) {
                SPUtils.getInstance().put(UNREAD_COUNT, Integer.valueOf(unreadMsgCount));
                Intent mainIntent = new Intent(this, MainActivity.class);
                Intent result = new Intent(this, EChatActivity.class);
                result.putExtra(EXTRA_NOTIFY, true);
                result.putExtra(EXTRA_COMPANY_ID, chatCompanyId);
                result.putExtra(EXTRA_CHAT_URL, echatUrl);
                result.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Intent[] intents = new Intent[]{mainIntent, result};
                startActivities(intents);
            } else {
                Intent main = getPackageManager().
                        getLaunchIntentForPackage(getPackageName());
                main.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(main);
            }
            finish();
        } catch (JSONException e) {
        }
    }

}
