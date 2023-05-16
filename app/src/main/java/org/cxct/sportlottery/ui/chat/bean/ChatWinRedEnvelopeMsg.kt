package org.cxct.sportlottery.ui.chat.bean

import android.view.ViewGroup
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatReceiveContent
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatRoomMsg
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatWinRedEnvelopeResult
import org.cxct.sportlottery.ui.chat.ChatMessageListAdapter3
import org.cxct.sportlottery.ui.chat.adapter.vh.WinRedEnvelopeVH

// 聊天列表红包
class ChatWinRedEnvelopeMsg(data: ChatReceiveContent<ChatWinRedEnvelopeResult>):
    ChatRoomMsg<ChatWinRedEnvelopeResult, WinRedEnvelopeVH>(data.content, data.seq, data.time, data.type) {

    override val itemType: Int = WIN_RED_ENVELOPE

    override fun createViewHolder(adapter: ChatMessageListAdapter3, parent: ViewGroup) = WinRedEnvelopeVH(parent)

    override fun bindVH(adapter: ChatMessageListAdapter3, holder: WinRedEnvelopeVH) {
        content?.let { holder.bind(it) }
    }
}
