package com.github.echatmulti.sample.utils;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.text.TextUtils;

import com.blankj.utilcode.util.CacheDoubleUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.echat.chat.utils.EChatUtils;
import com.github.echat.chat.utils.RequestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.github.echat.chat.utils.RequestUtils.TYPE_GET;
import static com.github.echatmulti.sample.utils.Constants.APPID;
import static com.github.echatmulti.sample.utils.Constants.APPID_DEFAULT;
import static com.github.echatmulti.sample.utils.Constants.BUS1_ID;
import static com.github.echatmulti.sample.utils.Constants.BUS2_ID;
import static com.github.echatmulti.sample.utils.Constants.DEVICE_TOKEN_FUN;
import static com.github.echatmulti.sample.utils.Constants.ENCODINGKEY;
import static com.github.echatmulti.sample.utils.Constants.ENCODINGKEY_DEFAULT;
import static com.github.echatmulti.sample.utils.Constants.METADATA_ONLY_UID;
import static com.github.echatmulti.sample.utils.Constants.PLATFORM_ID;
import static com.github.echatmulti.sample.utils.Constants.PLATFORM_SIGN;
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
    public MutableLiveData<String> platformId = new MutableLiveData<>();
    public MutableLiveData<String> bus1Id = new MutableLiveData<>();
    public MutableLiveData<String> bus2Id = new MutableLiveData<>();
    public MutableLiveData<String> platformSgin = new MutableLiveData<>();//平台的校验码 每个商户都得单独算
    public MutableLiveData<String> deviceToken = new MutableLiveData<>();
    public MutableLiveData<String> metaDataOnlyUid = new MutableLiveData<>();
    public MutableLiveData<String> encodingKey = new MutableLiveData<>();
    public MutableLiveData<Integer> unReadCount = new MutableLiveData<>();


    public void loadData() {
        appid.setValue(SPUtils.getInstance().getString(APPID, APPID_DEFAULT));
        token.setValue(SPUtils.getInstance().getString(TOKEN, TOKEN_DEFAULT));
        encodingKey.setValue(SPUtils.getInstance().getString(ENCODINGKEY, ENCODINGKEY_DEFAULT));
        platformId.setValue(SPUtils.getInstance().getString(PLATFORM_ID, "500029"));
        bus1Id.setValue(SPUtils.getInstance().getString(BUS1_ID, "500030"));
        bus2Id.setValue(SPUtils.getInstance().getString(BUS2_ID, "500047"));
        deviceToken.setValue(SPUtils.getInstance().getString(DEVICE_TOKEN_FUN));
        platformSgin.setValue(SPUtils.getInstance().getString(PLATFORM_SIGN, null));
        metaDataOnlyUid.setValue(SPUtils.getInstance().getString(METADATA_ONLY_UID, null));

        if (TextUtils.isEmpty(metaDataOnlyUid.getValue())) {
            makeNewMetadata();
        }

        if (TextUtils.isEmpty(platformSgin.getValue())) {
            makePlatformSgin();
        }

        loadUnreadCount();

    }

    public void loadUnreadCount() {

        final int count = SPUtils.getInstance().getInt(Constants.UNREAD_COUNT);
        LogUtils.w("加载的未读消息数据是：" + count);
        unReadCount.postValue(count);
        // TODO: 2019-07-01 remove it
        //JSONObject localCount = CacheDoubleUtils.getInstance().getJSONObject("localCount", new JSONObject());
        //unReadCount.postValue(localCount.optInt("all"));
    }

    public void saveUnreadCount() {
        SPUtils.getInstance().put(Constants.UNREAD_COUNT, unReadCount.getValue());

        // TODO: 2019-07-01 remove it
        /*try {
            JSONObject localCount = CacheDoubleUtils.getInstance().getJSONObject("localCount", new JSONObject());
            localCount.put("all", unReadCount.getValue());
            CacheDoubleUtils.getInstance().put("localCount", localCount);
        } catch (JSONException e) {
        }*/
    }

    /**
     * 请求一洽服务器 获得未读消息数
     */
    public void getUnreadCountFromNetwork(Context context) {
        if (TextUtils.isEmpty(metaDataOnlyUid.getValue())) {
            return;
        }
        RequestUtils.getInstance(context).requestAsyn(
                "http://p.echatsoft.com/chatapi/getVisitorUnReadMsgCount",
                TYPE_GET,
                new HashMap<String, String>() {{
                    put("companyId", platformId.getValue());
                    try {
                        put("metaData", URLEncoder.encode(metaDataOnlyUid.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                    }
                    put("platformSign", platformSgin.getValue());
                }},
                new RequestUtils.ReqCallBack<String>() {
                    @Override
                    public void onReqSuccess(String result) {
                        try {
                            JSONObject object = new JSONObject(result);
                            final int unreadMsgCount = object.optInt("unreadMsgCount");
                            unReadCount.postValue(unreadMsgCount);
                        } catch (JSONException e) {
                        }
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                    }
                }
        );
    }


    public void makePlatformSgin() {
        final String sha1 = EChatUtils.getSHA1(token.getValue(), appid.getValue(), platformId.getValue());
        platformSgin.postValue(sha1);
        SPUtils.getInstance().put(PLATFORM_SIGN, sha1);
    }

    public void makeNewMetadata() {
        Map<String, Object> metaDataMap = new HashMap<>();
        metaDataMap.put("uid", String.valueOf(deviceToken.getValue().hashCode()));
        final String metaData = EChatUtils.create2MetaData(metaDataMap, encodingKey.getValue(), appid.getValue());
        metaDataOnlyUid.postValue(metaData);
        SPUtils.getInstance().put(METADATA_ONLY_UID, metaData);
    }

    public void saveData() {
        SPUtils.getInstance().put(APPID, appid.getValue());
        SPUtils.getInstance().put(TOKEN, token.getValue());
        SPUtils.getInstance().put(PLATFORM_ID, platformId.getValue());
        SPUtils.getInstance().put(BUS1_ID, bus1Id.getValue());
        SPUtils.getInstance().put(BUS2_ID, bus2Id.getValue());
        SPUtils.getInstance().put(PLATFORM_SIGN, platformSgin.getValue());
        SPUtils.getInstance().put(METADATA_ONLY_UID, metaDataOnlyUid.getValue());
        SPUtils.getInstance().put(ENCODINGKEY, encodingKey.getValue());
        saveUnreadCount();
    }
}
