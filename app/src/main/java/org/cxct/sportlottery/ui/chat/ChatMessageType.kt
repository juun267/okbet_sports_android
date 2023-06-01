package org.cxct.sportlottery.ui.chat

object ChatMessageType {
    //一般訊息
    const val TEXT_IMAGE_VOICE = 1 //訊息、圖片、語音
    const val PLAN_MSG = 12 //計畫消息

    //系統消息
    const val SPEAKING_BANNED = 4 //禁言
    const val CANCEL_SPEAKING_BANNED = 5 //解禁言
    const val PLAN_ADMIN_MSG = 7//計劃管理員消息
    const val CHAT_TIME = 8 //聊天時段

    //跟注
    const val BET_FOLLOWED = 6 //跟注
    const val BET_FOLLOWED_RESULT = 14 //跟注開獎結果 //讚 + 打賞

    //紅包
    const val RED_PACKET_SYSTEM = 10 //紅包
    const val RED_PACKET_RESULT = 11 //搶紅包結果

    //中獎
    const val WIN_INFO = 18

    //跟注機器人
    const val ROBOT_BET = 19

    //投注記錄
    const val BET_RECORD = 20


    //不需更新chatMessageAdapter的消息
    const val USER_ENTER_CHAT = 2 //欢迎XXX进入聊天室
    const val USER_LEAVE_CHAT = 3 //XXX離開聊天室
    const val CANCEL_SYSTEM_MSG = 9 //系統消息撤銷
    const val SYSTEM_PRIVATE_MSG = 301 //系統給個人的訊息
    const val CLOSE_CHAT = 13 //聊天室關閉
}