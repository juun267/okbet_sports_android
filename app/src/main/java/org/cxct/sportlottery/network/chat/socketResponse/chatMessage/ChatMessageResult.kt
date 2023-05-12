package org.cxct.sportlottery.network.chat.socketResponse.chatMessage


import com.chad.library.adapter.base.entity.MultiItemEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.ui.chat.ChatMsgReceiveType

/**
 * @author Bill
 * @create 2023/3/14
 * @description
 * 聊天室聊天訊息
 * chatType 1000 房间文字訊息
 * chatType 1004 房间图片消息
 * chatType 2008 紅包
 * */

@JsonClass(generateAdapter = true)
@KeepMembers
data class ChatMessageResult(
    @Json(name = "content")
    val content: String?,
    @Json(name = "curTime")
    val curTime: String?, //讯息时间
    @Json(name = "iconMiniUrl")
    val iconMiniUrl: String?, //头像缩图
    @Json(name = "iconUrl")
    val iconUrl: String?, //头像
    @Json(name = "messageId")
    val messageId: String?,
    @Json(name = "nickName")
    val nickName: String?,
    @Json(name = "remark")
    val remark: String?,
    @Json(name = "type")
    val type: Int?, //ChatType 聊天类型
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "userUniKey")
    val userUniKey: String?,
    @Json(name = "userType")
    val userType: String?, //会员角色 0游客、1会员、2管理员、3訪客
    @Json(name = "bgColor")
    val bgColor: String?,
    @Json(name = "textColor")
    val textColor: String?,
    override val itemType: Int = ChatMsgReceiveType.CHAT_MSG,
): MultiItemEntity {
    var chatRedEnvelopeMessageResult: ChatRedEnvelopeMessageResult? = null


}
