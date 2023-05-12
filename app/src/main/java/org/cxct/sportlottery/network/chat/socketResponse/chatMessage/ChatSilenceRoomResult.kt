package org.cxct.sportlottery.network.chat.socketResponse.chatMessage

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.ui.chat.ChatMsgReceiveType

@JsonClass(generateAdapter = true)
@KeepMembers
data class ChatSilenceRoomResult(
    @Json(name = "roomId")
    val roomId: Long,
    @Json(name = "isSpeak")
    val isSpeak: String?,//聊天室是否可以聊天（1：可以聊天；0：不可以聊天）
    override val itemType: Int = ChatMsgReceiveType.CHAT_SILENCE_ROOM
): MultiItemEntity
