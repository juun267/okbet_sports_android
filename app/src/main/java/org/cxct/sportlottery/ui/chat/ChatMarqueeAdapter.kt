package org.cxct.sportlottery.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import org.cxct.sportlottery.databinding.ItemChatMarqueeBinding
import org.cxct.sportlottery.ui.maintab.publicity.MarqueeAdapter

/**
 * @author kevin
 * @create 2023/3/15
 * @description
 */
class ChatMarqueeAdapter : MarqueeAdapter() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DetailViewHolder {
        val layoutView =
            ItemChatMarqueeBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return MarqueeViewHolder(layoutView)
    }

    inner class MarqueeViewHolder(val binding: ItemChatMarqueeBinding) :
        DetailViewHolder(binding.root) {
        override var detail: TextView = binding.tvMarquee
    }
}