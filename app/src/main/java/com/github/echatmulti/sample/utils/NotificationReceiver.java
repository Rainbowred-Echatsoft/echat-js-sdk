package com.github.echatmulti.sample.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.echat.chat.utils.Constants;

import java.util.HashMap;
import java.util.List;

import static com.github.echat.chat.utils.Constants.CHAT_LAST_CHAT_TIME;
import static com.github.echat.chat.utils.Constants.CHAT_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.EXTRA_CHAT_URL;
import static com.github.echat.chat.utils.Constants.EXTRA_COMPANY_ID;
import static com.github.echatmulti.sample.utils.Constants.LASTCHAT;
import static com.github.echatmulti.sample.utils.Constants.UNREAD_COUNT;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-26
 * @describe
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";
    private List<Intent> intents;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("进入报告广播", "此时的测试状态是：");
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        /**
         * 接受chatlib 发来的本地消息(什么是本地消息 根据业务需求定义 可参考chatlib的逻辑)
         */
        if (Constants.ACTION_NEW_MSG.equals(action)) {
            LogUtils.iTag("chatLib", bundle);
            String companyIdString = bundle.getString(Constants.CHAT_COMPANY_ID, "");//公司ID
            String companyName = bundle.getString(Constants.CHAT_COMPANY_NAME, "");//用作通知标题
            String chatUrl = bundle.getString(EXTRA_CHAT_URL, "");//可作为点开通知，直接打开的地址
            String msgContent = bundle.getString(Constants.CHAT_MSG_CONTENT, "");//客服/系统发送的消息内容
            int unreadMsgCount = bundle.getInt(CHAT_UNREAD_COUNT);//这个用户的所有未读消息
            int msgType = bundle.getInt(Constants.CHAT_NEW_MSG_TYPE);//是对话新消息/平台新消息
            //来自对话的本地消息用
            if (msgType == Constants.TYPE_NEW_MSG_FROM_CHAT) {
                //默认 平台多商户版 不启用该功能 则可忽略
            }
            //来自平台的消息
            else if (msgType == Constants.TYPE_NEW_MSG_FROM_PLATFORM) {
                RemoteNotificationUtils.cancel(context, Integer.parseInt(companyIdString));
                RemoteNotificationUtils.getInstance(context)
                        .setCount(unreadMsgCount)
                        .setNotificationId(Integer.parseInt(companyIdString))
                        .showNotification(companyName, msgContent, null, null, new HashMap<String, String>() {{
                            put(EXTRA_COMPANY_ID, companyIdString);
                            put(EXTRA_CHAT_URL, chatUrl);
                        }});
            }
        }
        //接受未读消息数变更
        else if (Constants.ACTION_UNREAD_COUNT.equals(action)) {

            int notificationCount = bundle.getInt(CHAT_UNREAD_COUNT);
            //用于远程推送如果有时间戳，可根据这个时间戳，排除推送延迟的消息
            long lastChatTime = bundle.getLong(CHAT_LAST_CHAT_TIME);
            SPUtils.getInstance().put(LASTCHAT, lastChatTime);
            SPUtils.getInstance().put(UNREAD_COUNT, notificationCount);
        }
    }
}
