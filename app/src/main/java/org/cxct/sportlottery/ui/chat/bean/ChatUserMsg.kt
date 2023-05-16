package org.cxct.sportlottery.ui.chat.bean

import android.view.ViewGroup
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatRoomMsg
import org.cxct.sportlottery.ui.chat.ChatMessageListAdapter3
import org.cxct.sportlottery.ui.chat.adapter.vh.UserVH

// 聊天列表其他用户发的消息
class ChatUserMsg(data: ChatMessageResult)
    : ChatRoomMsg<ChatMessageResult, UserVH>(content = data, time = data.curTime, type = data.type) {
    override val itemType: Int = MESSAGE_USER
    override fun createViewHolder(adapter: ChatMessageListAdapter3, parent: ViewGroup) = UserVH(parent)

    override fun bindVH(adapter: ChatMessageListAdapter3, holder: UserVH) {
        content?.let { holder.bind(adapter, it) }
    }

}


