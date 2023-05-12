package org.cxct.sportlottery.ui.chat.adapter

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatReceiveContent

class ChatMessageAdapter: BaseMultiItemQuickAdapter<ChatReceiveContent<*>, BaseViewHolder>() {
    override fun convert(holder: BaseViewHolder, item: ChatReceiveContent<*>) {

    }
}