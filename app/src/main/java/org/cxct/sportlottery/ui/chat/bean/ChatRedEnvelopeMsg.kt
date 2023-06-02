package org.cxct.sportlottery.ui.chat.bean

import android.view.ViewGroup
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.*
import org.cxct.sportlottery.ui.chat.adapter.ChatMessageListAdapter3
import org.cxct.sportlottery.ui.chat.adapter.vh.MessageRedEnvelopeVH

sealed class ChatRedEnvelopeMsg<T>(data: ChatReceiveContent<T>)
    : ChatRoomMsg<T, MessageRedEnvelopeVH>(data.content, data.seq, data.time, data.type) {

    override val itemType: Int = RED_ENVELOPE

    override fun createViewHolder(adapter: ChatMessageListAdapter3, parent: ViewGroup) = MessageRedEnvelopeVH(parent)

    // 聊天列表红包 (type = 1001)
    class ChatRedEnvelopeMsg1001(data: ChatReceiveContent<ChatRedEnvelopeResult>): ChatRedEnvelopeMsg<ChatRedEnvelopeResult>(data) {
        override fun bindVH(adapter: ChatMessageListAdapter3, holder: MessageRedEnvelopeVH) {
            holder.bindMsg1001(adapter, this)
        }
    }

    // 聊天列表红包 (type = 2005)
    class ChatRedEnvelopeMsg2005(data: ChatReceiveContent<ChatPersonalRedEnvelopeResult>): ChatRedEnvelopeMsg<ChatPersonalRedEnvelopeResult>(data) {
        override fun bindVH(adapter: ChatMessageListAdapter3, holder: MessageRedEnvelopeVH) {
            holder.bindMsg2005(adapter, this)
        }
    }

    // 聊天列表红包 (type = 2008)
    class ChatRedEnvelopeMsg2008(data: ChatReceiveContent<ChatMessageResult>): ChatRedEnvelopeMsg<ChatMessageResult>(data) {
        override fun bindVH(adapter: ChatMessageListAdapter3, holder: MessageRedEnvelopeVH) {
            holder.bindMsg2008(adapter, this)
        }
    }

}

