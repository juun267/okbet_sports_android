package org.cxct.sportlottery.ui.chat.adapter

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemChatEmojiTypeBinding
import org.cxct.sportlottery.net.chat.data.ChatSticker

class RecyclerChatEmojiAdapter : BindingAdapter<ChatSticker, ItemChatEmojiTypeBinding>()  {
    override fun onBinding(position: Int, binding: ItemChatEmojiTypeBinding, item: ChatSticker) {
        binding.tvEmoji.text=item.url
    }
}