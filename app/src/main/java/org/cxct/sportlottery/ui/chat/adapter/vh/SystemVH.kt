package org.cxct.sportlottery.ui.chat.adapter.vh

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemChatMessageSystemBinding as ICMSB
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.util.TimeUtil

class SystemVH(parent: ViewGroup,
               private val binding: ICMSB = ICMSB.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {


    fun bind(data: ChatMessageResult) = data.run{
        binding.tvName.text = binding.root.context.getString(R.string.system_notify)
        binding.tvMessage.mixFontText = content
        binding.tvTime.text =
            if (curTime != null) TimeUtil.timeFormat(
                curTime,
                TimeUtil.HM_FORMAT
            ) else ""

        if (!bgColor.isNullOrEmpty()) {
            binding.tvMessage.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(bgColor))
        }
        if (!textColor.isNullOrEmpty())
            binding.tvMessage.setTextColor(Color.parseColor(textColor))
    }

}