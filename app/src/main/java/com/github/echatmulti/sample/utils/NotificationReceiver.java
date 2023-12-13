package com.github.echatmulti.sample.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.echatsoft.echatsdk.chat.utils.Constants;
import com.echatsoft.echatsdk.chat.utils.EChatUtils;

import java.util.HashMap;
import java.util.List;

import static com.echatsoft.echatsdk.chat.utils.Constants.ACTION_REMOTE_UNREAD_COUNT;
import static com.echatsoft.echatsdk.chat.utils.Constants.CHAT_LOCAL_UNREAD_COUNT;
import static com.echatsoft.echatsdk.chat.utils.Constants.EXTRA_CHAT_URL;
import static com.echatsoft.echatsdk.chat.utils.Constants.EXTRA_COMPANY_ID;
import static com.echatsoft.echatsdk.chat.utils.Constants.NOTIFICATION_LAST_CHAT_TIME;

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
            int unreadMsgCount = bundle.getInt(CHAT_LOCAL_UNREAD_COUNT);//这个用户的所有未读消息数
            int msgType = bundle.getInt(Constants.CHAT_NEW_MSG_TYPE);//是对话新消息/平台新消息
            //来自对话的本地消息用
            if (msgType == Constants.TYPE_NEW_MSG_FROM_CHAT) {
                //默认 平台多商户版 不启用该功能 则可忽略
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
        else if (Constants.ACTION_LOCAL_UNREAD_COUNT.equals(action)) {
            final int notificationCount = bundle.getInt(CHAT_LOCAL_UNREAD_COUNT);
            final long lastChatTime = bundle.getLong(NOTIFICATION_LAST_CHAT_TIME);
            //用于远程推送如果有时间戳，可根据这个时间戳，排除推送延迟的消息

            EChatUtils.setRemoteUnreadCount(0);//本地连接上了 不存在远程未读
        }

        /**
         * 远程推送的数据
         */
        else if (ACTION_REMOTE_UNREAD_COUNT.equals(action)){

        }
    }
}
