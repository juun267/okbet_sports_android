package org.cxct.sportlottery.network.chat.socketResponse.chatMessage

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @author Bill
 * @create 2023/3/14
 * @description
 * 个人讯息通知
 * chatType 2002 房间用户禁言
 * chatType 2003 房间用户解除禁言
 * chatType 2004 踢出房间
 * */
@JsonClass(generateAdapter = true)
data class ChatPersonalMsgResult(
    @Json(name = "userId")
    val userId: Long,//	用户ID
)
