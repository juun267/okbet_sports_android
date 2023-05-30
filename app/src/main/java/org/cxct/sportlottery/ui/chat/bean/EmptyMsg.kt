package org.cxct.sportlottery.ui.chat.bean

import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatRoomMsg
import org.cxct.sportlottery.ui.chat.adapter.ChatMessageListAdapter3

// 占位用，不显示任何东西
class EmptyMsg(override val itemType: Int = 0): ChatRoomMsg<String, BaseViewHolder>(content = "") {

    override fun createViewHolder(adapter: ChatMessageListAdapter3, parent: ViewGroup): BaseViewHolder {
       return BaseViewHolder(View(parent.context))
    }

    override fun bindVH(adapter: ChatMessageListAdapter3, holder: BaseViewHolder) {}

}