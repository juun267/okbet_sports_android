package org.cxct.sportlottery.ui.chat.bean

import android.view.ViewGroup
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatRoomMsg
import org.cxct.sportlottery.ui.chat.adapter.ChatMessageListAdapter3
import org.cxct.sportlottery.ui.chat.adapter.vh.DateVH

// 聊天列表展示的日期
class ChatDateMsg(val data: String,
                  override val itemType: Int = DATE_TIP): ChatRoomMsg<String, DateVH>(content = data) {

    override fun createViewHolder(adapter: ChatMessageListAdapter3, parent: ViewGroup) = DateVH.ofContext(parent.context)

    override fun bindVH(adapter: ChatMessageListAdapter3, holder: DateVH) {
        holder.bind(this)
    }

}