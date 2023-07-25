package org.cxct.sportlottery.ui.chat.adapter

import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemChatEmojiColumnBinding
import org.cxct.sportlottery.net.chat.data.ChatSticker
import org.cxct.sportlottery.net.chat.data.ChatStickerRow
import org.cxct.sportlottery.ui.chat.bean.EmojiColumnBean

class RecyclerChatColumnAdapter  : BindingAdapter<ChatStickerRow, ItemChatEmojiColumnBinding>() {
    override fun onBinding(position: Int, binding: ItemChatEmojiColumnBinding, item: ChatStickerRow) {
        //名称
        binding.tvName.text=item.typeName
        //选中
        if(item.select){
            binding.tvName.setBackgroundResource(R.drawable.bg_chat_emoji_column)
            binding.tvName.setTextColor(ContextCompat.getColor(context,R.color.color_025BE8))
        }else{
            //未选中
            binding.tvName.setBackgroundResource(R.drawable.bg_white_radius_8)
            binding.tvName.setTextColor(ContextCompat.getColor(context,R.color.color_6C7BA8_6C7BA8))
        }
    }
}