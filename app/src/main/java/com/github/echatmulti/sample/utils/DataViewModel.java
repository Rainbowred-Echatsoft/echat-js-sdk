package com.github.echatmulti.sample.utils;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.text.TextUtils;

import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.echat.chat.utils.EChatUtils;

import java.util.HashMap;
import java.util.Map;

import static com.github.echatmulti.sample.utils.Constants.APPID;
import static com.github.echatmulti.sample.utils.Constants.APPID_DEFAULT;
import static com.github.echatmulti.sample.utils.Constants.COMPANY_ID;
import static com.github.echatmulti.sample.utils.Constants.DEVICE_TOKEN_FUN;
import static com.github.echatmulti.sample.utils.Constants.ECHATTAG1;
import static com.github.echatmulti.sample.utils.Constants.ECHATTAG2;
import static com.github.echatmulti.sample.utils.Constants.ENCODINGKEY;
import static com.github.echatmulti.sample.utils.Constants.ENCODINGKEY_DEFAULT;
import static com.github.echatmulti.sample.utils.Constants.METADATA_ONLY_UID;
import static com.github.echatmulti.sample.utils.Constants.ROUTEENTRANCEID;
import static com.github.echatmulti.sample.utils.Constants.TOKEN;
import static com.github.echatmulti.sample.utils.Constants.TOKEN_DEFAULT;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-24
 * @describe
 */
public class DataViewModel extends ViewModel {
    public MutableLiveData<String> appid = new MutableLiveData<>();
    public MutableLiveData<String> token = new MutableLiveData<>();
    public MutableLiveData<String> companyId = new MutableLiveData<>();
    public MutableLiveData<String> deviceToken = new MutableLiveData<>();
    public MutableLiveData<String> metaDataOnlyUid = new MutableLiveData<>();
    public MutableLiveData<String> echatTag1 = new MutableLiveData<>();
    public MutableLiveData<String> echatTag2 = new MutableLiveData<>();
    public MutableLiveData<String> routeEntranceId = new MutableLiveData<>();
    public MutableLiveData<String> encodingKey = new MutableLiveData<>();
    public MutableLiveData<Integer> unReadCount = new MutableLiveData<>();
    public MutableLiveData<Integer> unReadRemoteCount = new MutableLiveData<>();

    public MutableLiveData<Integer> whoOpenChat = new MutableLiveData<>();

    public void loadData() {
        appid.setValue(SPUtils.getInstance().getString(APPID, APPID_DEFAULT));
        token.setValue(SPUtils.getInstance().getString(TOKEN, TOKEN_DEFAULT));
        encodingKey.setValue(SPUtils.getInstance().getString(ENCODINGKEY, ENCODINGKEY_DEFAULT));
        companyId.setValue(SPUtils.getInstance().getString(COMPANY_ID, "12170"));
        deviceToken.setValue(SPUtils.getInstance().getString(DEVICE_TOKEN_FUN));
        metaDataOnlyUid.setValue(SPUtils.getInstance().getString(METADATA_ONLY_UID, null));
        deviceToken.setValue(SPUtils.getInstance().getString(DEVICE_TOKEN_FUN));
        echatTag1.setValue(SPUtils.getInstance().getString(ECHATTAG1, "售前咨询"));
        echatTag2.setValue(SPUtils.getInstance().getString(ECHATTAG2, "售后服务"));
        routeEntranceId.setValue(SPUtils.getInstance().getString(ROUTEENTRANCEID, "209"));
        if (TextUtils.isEmpty(metaDataOnlyUid.getValue())) {
            makeNewMetadata();
        }

        loadUnreadCount();
    }

    public void loadUnreadCount() {
        final int count = EChatUtils.getLocalUnreadCount();
        final int remoteCount = EChatUtils.getRemoteUnreadCount();
        unReadRemoteCount.setValue(remoteCount);
        unReadCount.setValue(count);
    }

    public void saveUnreadCount() {
        EChatUtils.setLocalUnreadCount(unReadCount.getValue());
        EChatUtils.setRemoteUnreadCount(unReadCount.getValue());
    }

    /**
     * 请求一洽服务器 获得未读消息数
     */
    public void getUnreadCountFromNetwork(Context context) {
        if (TextUtils.isEmpty(metaDataOnlyUid.getValue())) {
            return;
        }
        EChatUtils.getUnreadCount(context,
                companyId.getValue(),
                EncodeUtils.urlEncode(metaDataOnlyUid.getValue(), "UTF-8"),
                null,
                null,
                new EChatUtils.GetUnreadCountCallback() {
                    @Override
                    public void onAPIChange(int count, String content, Long tm) {
                        LogUtils.iTag("UNREAD", String.format("unread count :%d", count));
                        LogUtils.iTag("UNREAD", String.format("last content:%s, timestamp : %d", content, tm));
                        //获得最新远程服务器未读消息数 和 时间戳
                        EChatUtils.sendLastChatInformation(context, content, tm);
                        unReadRemoteCount.setValue(count);
                        saveData();
                        EChatUtils.sendRemoteUnreadCount(context, count, tm);
                    }

                    @Override
                    public void fail(int errcode, String msg) {
                        LogUtils.eTag("UNREAD", errcode, msg);
                    }
                }
        );
    }

    public void makeNewMetadata() {
        Map<String, Object> metaDataMap = new HashMap<>();
        metaDataMap.put("uid", String.valueOf(deviceToken.getValue().hashCode()));
        final String metaData = EChatUtils.create2MetaData(metaDataMap, encodingKey.getValue(), appid.getValue());
        metaDataOnlyUid.setValue(metaData);
        SPUtils.getInstance().put(METADATA_ONLY_UID, metaData);
    }

    public void saveData() {
        SPUtils.getInstance().put(APPID, appid.getValue());
        SPUtils.getInstance().put(TOKEN, token.getValue());
        SPUtils.getInstance().put(COMPANY_ID, companyId.getValue());
        SPUtils.getInstance().put(METADATA_ONLY_UID, metaDataOnlyUid.getValue());
        SPUtils.getInstance().put(ENCODINGKEY, encodingKey.getValue());
        SPUtils.getInstance().put(ROUTEENTRANCEID, routeEntranceId.getValue());
        SPUtils.getInstance().put(ECHATTAG1, echatTag1.getValue());
        SPUtils.getInstance().put(ECHATTAG2, echatTag2.getValue());
        saveUnreadCount();
    }

    public void resetData() {
        appid.setValue(APPID_DEFAULT);
        token.setValue(TOKEN_DEFAULT);
        encodingKey.setValue(ENCODINGKEY_DEFAULT);
        companyId.setValue("12170");
        echatTag1.setValue("售前咨询");
        echatTag2.setValue("售后服务");
        routeEntranceId.setValue("209");
        makeNewMetadata();
        saveData();
    }
}
