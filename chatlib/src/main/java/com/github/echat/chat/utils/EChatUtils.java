package com.github.echat.chat.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.TextView;

import com.github.echat.chat.utils.aes.AesUtils;

import org.dom4j.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-20
 * @describe
 */
public class EChatUtils {

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
            if (!TextUtils.isEmpty(metaData)) put("metaData", metaData);
            if (!TextUtils.isEmpty(visitorId)) put("visitorId", visitorId);
            if (!TextUtils.isEmpty(encryptVId)) put("encryptVId", encryptVId);
        }}, visEvtJSON, new RequestUtils.ReqCallBack<String>() {
            @Override
            public void onReqSuccess(String result) {
                try {
                    JSONObject resutlObj = new JSONObject(result);
                    if (resutlObj.optInt("errcode") == 0) {
                        if (callback != null) callback.onStatus(true);
                    }
                } catch (JSONException e) {
                    if (callback != null) callback.onStatus(false);
                }
            }

            @Override
            public void onReqFailed(String errorMsg) {
                if (callback != null) callback.onStatus(true);
            }
        });
    }

    public interface SendVisEvtCallback {
        void onStatus(boolean flag);
    }


}
