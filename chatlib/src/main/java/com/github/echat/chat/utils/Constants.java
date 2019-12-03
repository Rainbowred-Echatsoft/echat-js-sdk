package com.github.echat.chat.utils;

public interface Constants {

    String COMPANY_ID = "companyId";
    String PUSH_INFO = "pushInfo";
    String METADATA = "metaData";
    String VISEVT = "visEvt";
    String ECHATTAG = "echatTag";
    String ROUTEENTRANCEID = "routeEntranceId";
    String TYPE = "type";
    String TYPE_CHAT = "chat";

    String EXTRA_NOTIFY = "extra_notify";
    String EXTRA_COMPANY_ID = "extra_company_id";
    String EXTRA_URL = "extra_url";

    //用于主app 接受推送数据
    String ACTION_NEW_MSG = "com.echat.chat.action.NEW_MSG";
    String ACTION_LOCAL_UNREAD_COUNT = "com.echat.chat.action.LOCAL_UNREAD_COUNT";
    String ACTION_REMOTE_UNREAD_COUNT = "com.echat.chat.action.REMOTE_UNREAD_COUNT";

    String EXTRA_VIDEO_URL = "extra_video_url";
    String EXTRA_VIDEO_FILE_NAME = "extra_video_file_name";
    String ACTION_DOWNLOAD_VIDEO = "com.echat.chat.action.DOWNLOAD_VIDEO";

    String EXTRA_CHAT_URL = "chat_url";
    String CHAT_COMPANY_ID = "chat_company_id";
    String CHAT_COMPANY_NAME = "chat_company_name";
    String CHAT_MSG_CONTENT = "chat_msg_content";
    String CHAT_LOCAL_UNREAD_COUNT = "chat_unread_count";
    String CHAT_REMOTE_UNREAD_COUNT = "chat_remote_unread_count";


    String CHAT_NEW_MSG_TYPE = "chat_new_msg_type";//广播给主APP的消息类型
    int TYPE_NEW_MSG_FROM_CHAT = 17100;//来自对话中的新消息
    String EXTRA_BROWER_URL = "extra_brower_url";

    String API_HOST = "https://eapi.echatsoft.com";
    //    String API_HOST = "https://tapi.echatsoft.com";
    String SEND_VISEVT_APIURL = API_HOST + "/pushVisitorEvent";
    String GET_UNREAD_COUNT_APIURL = API_HOST + "/getVisitorUnReadMsgCount/1.1";
//    String GET_UNREAD_COUNT_APIURL = API_HOST + "/getVisitorUnReadMsgCount";

    String ACTION_UPDATE_LAST_CONTENT = "com.github.echatmulti.action.last_content";//EChatutils sendLastChatInformation
    String NOTIFICATION_LAST_CONTENT = "notification_last_content";//broadcast or sp
    String NOTIFICATION_LAST_CHAT_TIME = "notification_last_chat_time";//broadcast or sp
    String CHAT_URL = "https://es.echatsoft.com/visitor/mobile/chat.html";
//    String CHAT_URL = "https://ts.echatsoft.com/visitor/mobile/chat.html";

}
