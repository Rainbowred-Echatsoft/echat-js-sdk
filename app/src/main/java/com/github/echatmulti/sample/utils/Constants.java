package com.github.echatmulti.sample.utils;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-21
 * @describe
 */
public interface Constants {

    String TOKEN = "token";
    String APPID = "appid";
    String TOKEN_DEFAULT = "N7s6ETKs";
    String ENCODINGKEY_DEFAULT = "gergw9I8r7LeNVgFfJq44sHbTbW8zhAHUptUW3hMVLR";
    String APPID_DEFAULT = "FC80AC211AB3F2A5C8BB8129F3C7E787";
    String PLATFORM_SIGN = "platformSign";
    String METADATA_ONLY_UID = "onlyUidMetaData";
    String ENCODINGKEY = "encodingKey";

    String LASTCHAT = "lastChat";
    String UNREAD_COUNT = "chat_unread_count";


    String PLATFORM_ID = "platform_id";
    String BUS1_ID = "bus1_id";
    String BUS2_ID = "bus2_id";

    //第一次初始化(获得DeviceToken后就不算第一次启动)
    String FIRST_BOOT = "first_boot";
    String DEVICETOKEN = "DEVICETOKEN";


    String ACTION_DEVICE_TOKEN = "com.github.echatmulti.action.devicetoken";
    String DEVICE_TOKEN_FUN = "deviceToken";//broadcast or sp


    int HANDLER_WHAT_BACK = 10001;//处理后退
    int HANDLER_WHAT_DATA_HANDLE = 10002;//处理数据

}
