package org.cxct.sportlottery.ui.chat.adapter.vh

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.databinding.ItemChatMessageWinRedEnvelopeBinding
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatReceiveContent
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatWinRedEnvelopeResult
import org.cxct.sportlottery.util.TextUtil

class WinRedEnvelopeVH (parent: ViewGroup,
                        private val binding: ItemChatMessageWinRedEnvelopeBinding = ItemChatMessageWinRedEnvelopeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {

    fun bind(data: ChatReceiveContent<*>) {
        data.getThisContent<ChatWinRedEnvelopeResult>()?.apply {
            binding.tvName.text = if (!nickName.isNullOrEmpty()) nickName else userName
            binding.tvMoney.text = TextUtil.format(money.toString()) //應正確顯示到小數第二位
            binding.tvCurrency.text = currency
        }
    }
}