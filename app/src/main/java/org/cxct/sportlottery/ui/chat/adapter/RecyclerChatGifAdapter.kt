package org.cxct.sportlottery.ui.chat.adapter

import android.util.Log
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemChatGifBinding
import org.cxct.sportlottery.net.chat.data.ChatSticker
import org.cxct.sportlottery.repository.sConfigData

class RecyclerChatGifAdapter: BindingAdapter<ChatSticker, ItemChatGifBinding>()  {


    override fun onBinding(position: Int, binding: ItemChatGifBinding, item: ChatSticker) {
        val host= sConfigData?.resServerHost
        binding.ivGif.load("${host}${item.url}")
    }
}