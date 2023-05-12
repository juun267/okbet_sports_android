package org.cxct.sportlottery.ui.chat.adapter.vh

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.databinding.ItemChatDateBlurBinding
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatReceiveContent

class FloatingDateVH(parent: ViewGroup,
    private val binding: ItemChatDateBlurBinding = ItemChatDateBlurBinding.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {

    fun bind(data: ChatReceiveContent<*>) {
        data.getThisContent<String>().apply {
            binding.tvDate.text = this@apply
        }
    }

}