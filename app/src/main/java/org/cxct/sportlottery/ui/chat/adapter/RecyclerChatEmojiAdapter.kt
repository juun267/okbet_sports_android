package org.cxct.sportlottery.ui.chat.adapter

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemChatEmojiTypeBinding

class RecyclerChatEmojiAdapter : BindingAdapter<String, ItemChatEmojiTypeBinding>()  {
    override fun onBinding(position: Int, binding: ItemChatEmojiTypeBinding, item: String) {
        binding.tvEmoji.text=item
    }
}