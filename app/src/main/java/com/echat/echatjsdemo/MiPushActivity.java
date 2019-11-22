package com.echat.echatjsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.echat.chat.EChatActivity;
import com.github.echat.chat.utils.EChatUtils;
import com.github.echatmulti.sample.MainActivity;
import com.umeng.message.UmengNotifyClickActivity;

import org.android.agoo.common.AgooConstants;
import org.json.JSONException;
import org.json.JSONObject;

import static com.github.echat.chat.utils.Constants.EXTRA_CHAT_URL;
import static com.github.echat.chat.utils.Constants.EXTRA_COMPANY_ID;
import static com.github.echat.chat.utils.Constants.EXTRA_NOTIFY;

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
        //{"display_type":"notification","extra":{"unreadMsgCount":"2","chatCompanyId":"12170","echatUrl":"https://es.echatsoft.com/visitor/mobile/chat.html?companyId=12170&echatTag=app_android&pushInfo=AudpTaEVZ0rrjHfTcnysqRgp_fYaXfyIjpr0F5MeObZL&myData=","echatTimeStamp":"1574414943725"},
        // "msg_id":"uuzcgxq157441494568501","body":{"after_open":"go_app","ticker":"Echat","text":"222222","title":"XHY@Android"},"random_min":0}
        try {
            JSONObject jsonObject = new JSONObject(body);
            final JSONObject extra = jsonObject.optJSONObject("extra");
            final String chatCompanyId = extra.optString("chatCompanyId");
            final String echatUrl = extra.optString("echatUrl");
            final String unreadMsgCount = extra.optString("unreadMsgCount");
            final String echatTimeStampString = extra.optString("echatTimeStamp");

            final JSONObject bodyObj = jsonObject.optJSONObject("body");
            final String msgContent = bodyObj.optString("text");


            final long echatTimeStamp = Long.valueOf(echatTimeStampString);
            final long lastChatTime = EChatUtils.getLastChatTime();
            //判断当前消息是否应该推送[lastChatTime上次已知最后一次进入对话窗口/接受到远程消息时间戳]
            //用于排除远程消息/厂商消息延后到达 避免不必要的通知
            if (lastChatTime < echatTimeStamp) {
                //更新最后一次对话时间戳
                EChatUtils.sendLastChatInformation(this, msgContent, echatTimeStamp);
                //走到这一步 实际上是打开到对话窗口 无需计数
                EChatUtils.setRemoteUnreadCount(0);

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
