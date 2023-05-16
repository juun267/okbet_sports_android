package org.cxct.sportlottery.ui.chat.adapter.vh

import android.content.res.ColorStateList
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemChatMessageMeBinding as ICMMB
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.ui.chat.ChatMessageListAdapter3
import org.cxct.sportlottery.ui.chat.bean.UserMessageStyle
import org.cxct.sportlottery.util.TimeUtil

class MeVH (parent: ViewGroup,
            private val binding: ICMMB = ICMMB.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {

    fun bind(adapter: ChatMessageListAdapter3, data: ChatMessageResult) = data.run {
        val checkedContent = adapter.checkContent(content, userType)
        val stringBuilder = SpannableStringBuilder()
        val checkTag =
            SpannableString(adapter.checkTag(stringBuilder, binding.root.context, checkedContent))
        binding.tvMessage.apply {
            setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    UserMessageStyle.getTextColor(userType)
                )
            )
            mixFontText = checkTag
        }
        //region 處理圖片和文字訊息以及排版
        adapter.setupPictureAndTextMessage(
            this@run,
            binding.ivChatImage,
            binding.tvMessage,
            binding.messageBorder
        ) { adapter.onPhotoClick(it) }

        binding.paddingView.isGone =
            binding.ivChatImage.isVisible && binding.tvMessage.isVisible
        //endregion

        binding.tvTime.text =
            if (curTime != null) TimeUtil.timeFormat(
                curTime.toLong(),
                TimeUtil.HM_FORMAT
            ) else ""

        if (UserMessageStyle.isAdmin(userType)) {
            binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_admin_circle_me)

            binding.messageBorder.backgroundTintList = null
        } else {
            //0游客、1会员、2管理员(特殊處理)、3訪客
            binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_me_custom_circle_border)

            binding.messageBorder.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        UserMessageStyle.getBorderColor(userType)
                    )
                )
        }
    }

}
