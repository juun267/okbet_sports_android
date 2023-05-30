package org.cxct.sportlottery.ui.chat.bean

import android.view.ViewGroup
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatRoomMsg
import org.cxct.sportlottery.ui.chat.adapter.ChatMessageListAdapter3
import org.cxct.sportlottery.ui.chat.adapter.vh.FloatingDateVH

// 聊天列表悬浮日期
class ChatFloatingDateMsg(val date: String,
                          override val itemType: Int = FLOATING_DATE_TIP): ChatRoomMsg<String, FloatingDateVH>(content = date) {

    override fun createViewHolder(adapter: ChatMessageListAdapter3, parent: ViewGroup) = FloatingDateVH(parent)

    override fun bindVH(adapter: ChatMessageListAdapter3, holder: FloatingDateVH) = holder.bind(this)

}