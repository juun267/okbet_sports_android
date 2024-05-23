package org.cxct.sportlottery.network.chat.socketResponse.chatMessage

import org.cxct.sportlottery.common.proguards.KeepMembers

/**
 * @author Bill
 * @create 2023/3/14
 * @description
 * 聊天室聊天訊息
 * chatType 1000 房间文字訊息
 * chatType 1004 房间图片消息
 * chatType 2008 紅包
 * */

@KeepMembers
data class ChatMessageResult(
    val content: String?,
    val curTime: Long = 0, //讯息时间
    val iconMiniUrl: String?, //头像缩图
    val iconUrl: String?, //头像
    val messageId: String?,
    val nickName: String?,
    val remark: String?,
    val type: Int = -1, //ChatType 聊天类型
    val userId: Int = -1,
    val userUniKey: String?,
    val userType: String?, //会员角色 0游客、1会员、2管理员、3訪客
    val bgColor: String?,
    val textColor: String?,
    val userLevelCode: String?,
) {
    @Transient
    var chatRedEnvelopeMessageResult: ChatRedEnvelopeMessageResult? = null

}
