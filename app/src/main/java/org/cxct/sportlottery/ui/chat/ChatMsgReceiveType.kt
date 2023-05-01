package org.cxct.sportlottery.ui.chat

/**
 * @author Bill
 * @create 2023/3/14
 * @description 聊天室返回訊息列表
 */
object ChatMsgReceiveType {
    const val CHAT_MSG= 1000//房间聊天訊息

    const val CHAT_SEND_RED_ENVELOPE= 1001//发送红包

    const val CHAT_USER_ENTER= 1002//用户进入房间

    const val CHAT_USER_LEAVE= 1003//用户离开房间

    const val CHAT_SEND_PIC= 1004//用户发送图片讯息

    const val CHAT_SEND_PIC_AND_TEXT= 1010//用户发送图片+讯息的消息

    const val CHAT_WIN_RED_ENVELOPE_ROOM_NOTIFY= 1005//推送中奖红包金额

    const val CHAT_SILENCE_ROOM= 1006//推送平台聊天室是否禁言

    const val CHAT_WIN_RED_ENVELOPE_RAIN_NOTIFY= 1007//推送来自红包雨中奖红包通知

    const val CHAT_MSG_REMOVE= 1008//删除消息

    const val CHAT_UPDATE_USER_LEVEL_CONFIG= 1009//推送用户层级设定修改

    const val CHAT_SILENCE= 2002//房间用户禁言

    const val CHAT_RELIEVE_SILENCE= 2003//房间用户解除禁言

    const val CHAT_KICK_OUT= 2004//踢出房间

    const val CHAT_SEND_PERSONAL_RED_ENVELOPE= 2005//发送用户个人红包

    const val CHAT_USER_PROMPT= 2006//发送用户系统提示讯息

    const val CHAT_MSG_RED_ENVELOPE= 2008//房间用户红包消息

    const val CHAT_UPDATE_MEMBER= 2009//推送会员用户层级变更

    const val CHAT_ERROR= 9999//异常信息

}