package org.cxct.sportlottery.network.chat.socketResponse.chatMessage.subscribeSuccess


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Bulletin(
    @Json(name = "content")
    val content: String,
)