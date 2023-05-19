package org.cxct.sportlottery.network.chat.socketResponse.chatMessage.subscribeSuccess


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult

@JsonClass(generateAdapter = true)
@KeepMembers
data class SubscribeSuccessResult(
    @Json(name = "bulletinList")
    val bulletinList: List<Bulletin>?,
    @Json(name = "messageList")
    val messageList: List<ChatMessageResult>,
)