package com.github.echat.chat.utils;

public interface Constants {

    String COMPANY_ID = "companyId";
    String PUSH_INFO = "pushInfo";
    String METADATA = "metaData";
    String VISEVT = "visEvt";
    String ECHATTAG = "echatTag";
    String TYPE = "type";
    String TYPE_CHAT = "chat";

    String EXTRA_NOTIFY = "extra_notify";
    String EXTRA_COMPANY_ID = "extra_company_id";
    String EXTRA_URL = "extra_url";

    String SP_LAST_CHAT_TIME = "sp_last_chat_time";

    //用于主app 接受推送数据
    String ACTION_NEW_MSG = "com.echat.chat.action.NEW_MSG";
    String ACTION_UNREAD_COUNT = "com.echat.chat.action.UNREAD_COUNT";
    String EXTRA_CHAT_URL = "chat_url";
    String CHAT_COMPANY_ID = "chat_company_id";
    String CHAT_COMPANY_NAME = "chat_company_name";
    String CHAT_MSG_CONTENT = "chat_msg_content";
    String CHAT_UNREAD_COUNT = "chat_unread_count";
    String CHAT_LAST_CHAT_TIME = "chat_last_chat_time";

    String CHAT_NEW_MSG_TYPE = "chat_new_msg_type";//广播给主APP的消息类型
    int TYPE_NEW_MSG_FROM_CHAT = 17100;//来自对话中的新消息
    String EXTRA_BROWER_URL = "extra_brower_url";

    String SEND_VISEVT_APIURL = "https://eapi.echatsoft.com/pushVisitorEvent";
    String GET_UNREAD_COUNT_APIURL = "https://eapi.echatsoft.com/getVisitorUnReadMsgCount";

    interface BroadcastPermission {
        String MESSAGE_RECEIVE_PERMISSION = "com.echat.chat.RECEIVE_PERMISSION";
        String MESSAGE_SEND_PERMISSION = "com.echat.chat.SEND_PERMISSION";
    }
}
