package org.cxct.sportlottery.ui.chat.bean

import android.view.ViewGroup
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatRoomMsg
import org.cxct.sportlottery.ui.chat.ChatMessageListAdapter3
import org.cxct.sportlottery.ui.chat.adapter.vh.MeVH

// 聊天列表我自己发的消息
class ChatMineMsg(data: ChatMessageResult):
    ChatRoomMsg<ChatMessageResult, MeVH>(content = data, time = data.curTime, type = data.type) {

    override val itemType: Int = MESSAGE_ME

    override fun createViewHolder(adapter: ChatMessageListAdapter3, parent: ViewGroup): MeVH {
        return MeVH(parent)
    }

    override fun bindVH(adapter: ChatMessageListAdapter3, holder: MeVH) {
        this.content?.let { holder.bind(adapter, it) }
    }

}
