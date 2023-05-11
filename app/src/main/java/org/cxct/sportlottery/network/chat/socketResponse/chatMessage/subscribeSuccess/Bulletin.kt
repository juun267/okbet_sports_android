package org.cxct.sportlottery.network.chat.socketResponse.chatMessage.subscribeSuccess


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class Bulletin(
    @Json(name = "content")
    val content: String,
)