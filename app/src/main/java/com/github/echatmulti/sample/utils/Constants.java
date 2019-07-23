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
    String TOKEN_DEFAULT = "vqS36X2P";
    String ENCODINGKEY_DEFAULT = "je7ZjRnLc6wR4gT3BCN6aqDjGUqmnmTXL33V8zBDNLA";
    String APPID_DEFAULT = "B8E32C77DEC45BCB3429AF70C8FD7A8B";
    String PLATFORM_SIGN = "platformSign";
    String METADATA_ONLY_UID = "onlyUidMetaData";
    String ENCODINGKEY = "encodingKey";

    String LASTCHAT = "lastChat";
    String UNREAD_COUNT = "chat_unread_count";
    String REMOTE_UNREAD_COUNT = "chat_remote_unread_count";


    String COMPANY_ID = "company_id";

    //第一次初始化(获得DeviceToken后就不算第一次启动)
    String FIRST_BOOT = "first_boot";
    String DEVICETOKEN = "DEVICETOKEN";


    String ACTION_DEVICE_TOKEN = "com.github.echatmulti.action.devicetoken";
    String DEVICE_TOKEN_FUN = "deviceToken";//broadcast or sp


    int HANDLER_WHAT_BACK = 10001;//处理后退
    int HANDLER_WHAT_DATA_HANDLE = 10002;//处理数据

    int RESULT_CODE_SETTINGS_QR = 17001;
}
