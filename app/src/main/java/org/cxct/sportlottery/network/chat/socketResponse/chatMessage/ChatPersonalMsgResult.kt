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
 * 个人讯息通知
 * chatType 2002 房间用户禁言
 * chatType 2003 房间用户解除禁言
 * chatType 2004 踢出房间
 * */
@JsonClass(generateAdapter = true)
@KeepMembers
data class ChatPersonalMsgResult(
    @Json(name = "userId")
    val userId: Long, //	用户ID
    override val itemType: Int = ChatMsgReceiveType.CHAT_SILENCE,
): MultiItemEntity
