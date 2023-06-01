package org.cxct.sportlottery.ui.chat.bean

import android.view.ViewGroup
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatReceiveContent
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatRoomMsg
import org.cxct.sportlottery.ui.chat.adapter.ChatMessageListAdapter3
import org.cxct.sportlottery.ui.chat.adapter.vh.SystemVH


// 聊天列表系统发的消息
class ChatSystemMsg(data: ChatReceiveContent<ChatMessageResult>)
    : ChatRoomMsg<ChatMessageResult, SystemVH>(data.content, data.seq, data.time, data.type) {

    override val itemType: Int = SYSTEM

    override fun createViewHolder(adapter: ChatMessageListAdapter3, parent: ViewGroup): SystemVH {
        return SystemVH(parent)
    }

    override fun bindVH(adapter: ChatMessageListAdapter3, holder: SystemVH) {
        content?.let { holder.bind(it) }
    }

}


