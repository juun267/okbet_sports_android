package org.cxct.sportlottery.network.chat.socketResponse.chatMessage

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatSilenceRoomResult(
    @Json(name = "roomId")
    val roomId: Long,
    @Json(name = "isSpeak")
    val isSpeak: String?,//聊天室是否可以聊天（1：可以聊天；0：不可以聊天）
)
