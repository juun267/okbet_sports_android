package org.cxct.sportlottery.ui.chat

enum class ChatMessageType(val type: Int) {
    //一般訊息
    TEXT_IMAGE_VOICE(1), //訊息、圖片、語音
    PLAN_MSG(12), //計畫消息

    //系統消息
    SPEAKING_BANNED(4), //禁言
    CANCEL_SPEAKING_BANNED(5), //解禁言
    PLAN_ADMIN_MSG(7),//計劃管理員消息
    CHAT_TIME(8), //聊天時段

    //跟注
    BET_FOLLOWED(6), //跟注
    BET_FOLLOWED_RESULT(14), //跟注開獎結果 //讚 + 打賞

    //紅包
    RED_PACKET_SYSTEM(10), //紅包
    RED_PACKET_RESULT(11), //搶紅包結果

    //中獎
    WIN_INFO(18),

    //跟注機器人
    ROBOT_BET(19),

    //投注記錄
    BET_RECORD(20),


    //不需更新chatMessageAdapter的消息
    USER_ENTER_CHAT(2), //欢迎XXX进入聊天室
    USER_LEAVE_CHAT(3), //XXX離開聊天室
    CANCEL_SYSTEM_MSG(9), //系統消息撤銷
    SYSTEM_PRIVATE_MSG(301), //系統給個人的訊息
    CLOSE_CHAT(13), //聊天室關閉
}