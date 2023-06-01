package org.cxct.sportlottery.ui.chat.adapter.vh

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.databinding.ItemChatDateBlurBinding as ICDBB
import org.cxct.sportlottery.ui.chat.bean.ChatFloatingDateMsg

class FloatingDateVH(parent: ViewGroup,
                     private val binding: ICDBB = ICDBB.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {

    fun bind(data: ChatFloatingDateMsg) {
        binding.tvDate.text = data.date
    }

}