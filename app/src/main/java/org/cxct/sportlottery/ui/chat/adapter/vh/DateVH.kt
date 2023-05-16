package org.cxct.sportlottery.ui.chat.adapter.vh

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.databinding.ItemChatDateBinding as ICDB
import org.cxct.sportlottery.ui.chat.bean.ChatDateMsg

class DateVH(parent: ViewGroup,
             private val binding: ICDB = ICDB.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {
    fun bind(data: ChatDateMsg) {
        binding.tvDate.text = data.content
    }
}