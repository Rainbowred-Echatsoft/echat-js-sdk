package com.github.echat.chat.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.echat.chat.utils.aes.AesUtils;

import org.dom4j.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.github.echat.chat.utils.Constants.ACTION_LOCAL_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.ACTION_REMOTE_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.ACTION_UPDATE_LAST_CONTENT;
import static com.github.echat.chat.utils.Constants.CHAT_REMOTE_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.CHAT_LOCAL_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.GET_UNREAD_COUNT_APIURL;
import static com.github.echat.chat.utils.Constants.NOTIFICATION_LAST_CHAT_TIME;
import static com.github.echat.chat.utils.Constants.NOTIFICATION_LAST_CONTENT;
import static com.github.echat.chat.utils.RequestUtils.TYPE_GET;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-20
 * @describe
 */
public class EChatUtils {
    private static final String TAG = "EChatUtils";

    public static String getSHA1(String token, String appid, String companyId) {
        try {
            String[] array = new String[]{token, appid, companyId};
            StringBuffer sb = new StringBuffer();
            Arrays.sort(array);
            for (String s : array) {
                sb.append(s);
            }
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(sb.toString().getBytes());
            byte[] digest = md.digest();

            StringBuffer hexstr = new StringBuffer();
            String shaHex;
            for (int i = 0; i < digest.length; i++) {
                shaHex = Integer.toHexString(digest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexstr.append(0);
                }
                hexstr.append(shaHex);
            }
            return hexstr.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 加密生成metaData
     *
     * @param metaData
     * @param encodingKey
     * @param appId
     * @return
     */
    public static String create2MetaData(Map<String, Object> metaData,
                                         String encodingKey, String appId) {
        try {
            Document document = XMLUtils.map2xml(metaData, "xml");
            String xml = XMLUtils.formatXml(document);
            AesUtils aes = new AesUtils(encodingKey, appId);
            return aes.encrypt(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送图文消息
     *
     * @param companyId  公司ID 必须
     * @param metaData   metaData/visitorId/encryptVId 三选一
     * @param visitorId  metaData/visitorId/encryptVId 三选一
     * @param encryptVId metaData/visitorId/encryptVId 三选一
     * @param visEvtJSON 图文消息JSON 必须
     * @param callback
     */
    public static void sendVisEvt(@NonNull Context context, @NonNull String companyId, String metaData, String visitorId, String encryptVId, @NonNull String visEvtJSON, SendVisEvtCallback callback) {
        if (TextUtils.isEmpty(companyId) || context == null || TextUtils.isEmpty(visEvtJSON))
            return;
        if ((TextUtils.isEmpty(visitorId) && TextUtils.isEmpty(metaData) && TextUtils.isEmpty(encryptVId)))
            return;
        RequestUtils.getInstance(context).requestPostJsonByAsyn(Constants.SEND_VISEVT_APIURL, new HashMap<String, String>() {{
            put("companyId", companyId);
            if (!TextUtils.isEmpty(metaData)) put("metaData", metaData);
            if (!TextUtils.isEmpty(visitorId)) put("visitorId", visitorId);
            if (!TextUtils.isEmpty(encryptVId)) put("encryptVId", encryptVId);
        }}, visEvtJSON, new RequestUtils.ReqCallBack<String>() {
            @Override
            public void onReqSuccess(String result) {
                try {
                    JSONObject resutlObj = new JSONObject(result);
                    if (resutlObj.optInt("errcode") == 0) {
                        if (callback != null)
                            callback.onStatus(true, resutlObj.optString("errmsg"));
                    } else {
                        if (callback != null)
                            callback.onStatus(false, resutlObj.optString("errmsg"));
                    }
                } catch (JSONException e) {
                    if (callback != null) callback.onStatus(false, e.getLocalizedMessage());
                }
            }

            @Override
            public void onReqFailed(String errorMsg) {
                if (callback != null) callback.onStatus(false, errorMsg);
            }

        });
    }

    public interface SendVisEvtCallback {
        void onStatus(boolean flag, String msg);
    }


    /**
     * 获得未读消息数
     *
     * @param companyId  公司ID 必须
     * @param metaData   metaData/(visitorId/encryptVId) 二选一
     * @param visitorId  metaData/(visitorId/encryptVId) 二选一
     * @param encryptVId metaData/(visitorId/encryptVId) 二选一
     * @param callback
     */
    public static void getUnreadCount(@NonNull Context context,
                                      @NonNull String companyId,
                                      String metaData,
                                      String visitorId,
                                      String encryptVId,
                                      GetUnreadCountCallback callback) {
        if (TextUtils.isEmpty(companyId) || context == null)
            return;
        if ((TextUtils.isEmpty(visitorId) && TextUtils.isEmpty(metaData) && TextUtils.isEmpty(encryptVId)))
            return;
        RequestUtils.getInstance(context).requestAsyn(
                GET_UNREAD_COUNT_APIURL,
                TYPE_GET,
                new HashMap<String, String>() {{
                    put("companyId", companyId);
                    if (!TextUtils.isEmpty(metaData)) put("metaData", metaData);
                    if (!TextUtils.isEmpty(visitorId)) put("visitorId", visitorId);
                    if (!TextUtils.isEmpty(encryptVId)) put("encryptVId", encryptVId);
                }},
                new RequestUtils.ReqCallBack<String>() {
                    @Override
                    public void onReqSuccess(String result) {
                        LogUtils.iTag("EChatutils", result);
                        try {
                            JSONObject resutlObj = new JSONObject(result);
                            if (resutlObj.optInt("errcode") == 0) {
                                final int unreadMsgCount = resutlObj.optInt("unreadMsgCount");
                                final String lastMsgContent = resutlObj.optString("lastMsgContent");
                                final Long tm = resutlObj.optLong("tm");

                                if (callback != null)
                                    callback.onAPIChange(unreadMsgCount, lastMsgContent, tm);
                            } else {
                                if (callback != null)
                                    callback.fail(resutlObj.optInt("errcode"), resutlObj.optString("errmsg"));
                            }
                        } catch (JSONException e) {
                            if (callback != null) callback.fail(-1, e.getLocalizedMessage());
                        }
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                        if (callback != null) callback.fail(-1, errorMsg);
                    }
                }
        );
    }

    /**
     * 设置对话最后一条消息和时间错
     *
     * @param content
     * @param lastChatTime
     */
    public static void setLastChatInformation(String content, long lastChatTime) {
        if (!TextUtils.isEmpty(content)) {
            SPUtils.getInstance().put(Constants.NOTIFICATION_LAST_CONTENT, content);
        }
        SPUtils.getInstance().put(Constants.NOTIFICATION_LAST_CHAT_TIME, lastChatTime);
    }

    /**
     * 访客发送的消息 和 时间戳
     * 用于展示对话最后一条消息(ps 当访客发送时 则是当前这则对话最后一条消息)
     *
     * @param context
     * @param content      消息内容
     * @param lastChatTime 时间戳
     */
    public static void sendLastChatInformation(Context context, String content, long lastChatTime) {
        LogUtils.iTag(TAG, String.format("访客发送的消息：%s 和 时间戳：%d", content, lastChatTime));
        EChatUtils.setLastChatInformation(content, lastChatTime);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(NOTIFICATION_LAST_CONTENT, content);
        bundle.putLong(NOTIFICATION_LAST_CHAT_TIME, lastChatTime);
        intent.putExtras(bundle);
        intent.setAction(ACTION_UPDATE_LAST_CONTENT);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    /**
     * 处理远程消息 通知UI更新
     *
     * @param context
     * @param count
     * @param echatTimeStamp
     */
    public static void sendRemoteUnreadCount(Context context, int count, long echatTimeStamp) {
        setRemoteUnreadCount(count);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(CHAT_REMOTE_UNREAD_COUNT, count);
        bundle.putLong(NOTIFICATION_LAST_CHAT_TIME, echatTimeStamp);
        intent.putExtras(bundle);
        intent.setAction(ACTION_REMOTE_UNREAD_COUNT);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    public static void sendLocalUnreadCount(Context context, int localUnreadCount, long lastChatTime) {
        LogUtils.iTag(TAG, String.format("本地消息数：%d 和 时间戳：%d", localUnreadCount, lastChatTime));
        setLocalUnreadCount(localUnreadCount);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(CHAT_LOCAL_UNREAD_COUNT, localUnreadCount);
        bundle.putLong(NOTIFICATION_LAST_CHAT_TIME, lastChatTime);
        intent.putExtras(bundle);
        intent.setAction(ACTION_LOCAL_UNREAD_COUNT);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    public static Long getLastChatTime() {
        return SPUtils.getInstance().getLong(Constants.NOTIFICATION_LAST_CHAT_TIME);
    }

    public static String getLastChatMsgContent() {
        return SPUtils.getInstance().getString(Constants.NOTIFICATION_LAST_CONTENT);
    }

    public static void setLocalUnreadCount(int count) {
        SPUtils.getInstance().put(CHAT_LOCAL_UNREAD_COUNT, count);
    }

    public static int getLocalUnreadCount() {
        return SPUtils.getInstance().getInt(CHAT_LOCAL_UNREAD_COUNT);
    }

    public static void setRemoteUnreadCount(int count) {
        SPUtils.getInstance().put(Constants.CHAT_REMOTE_UNREAD_COUNT, count);
    }

    public static int getRemoteUnreadCount() {
        return SPUtils.getInstance().getInt(Constants.CHAT_REMOTE_UNREAD_COUNT);
    }


    public interface GetUnreadCountCallback {
        void onAPIChange(int count, String lastMsgContent, Long tm);

        void fail(int errcode, String msg);
    }


}
