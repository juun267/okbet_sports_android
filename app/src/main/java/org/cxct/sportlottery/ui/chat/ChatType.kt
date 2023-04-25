package org.cxct.sportlottery.ui.chat

/**
 * @author Bill
 * @create 2023/3/14
 * @description 聊天室傳送訊息Type
 */
enum class ChatType(val code: Int?) {
    CHAT_SEND_TEXT_MSG(1000),//房间文字訊息

    CHAT_SEND_PIC_MSG(1004),//房间图片消息

    CHAT_SEND_PIC_AND_TEXT_MSG(1010),//发送图片+讯息的消息

}