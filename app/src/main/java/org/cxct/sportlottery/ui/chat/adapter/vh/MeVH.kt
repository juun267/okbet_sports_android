package org.cxct.sportlottery.ui.chat.adapter.vh

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
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

class MeVH (parent: ViewGroup,
            private val context: Context = parent.context,
            private val binding: ICMMB = ICMMB.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {

    private val defaultMsgBg by lazy {
        val drawable = ContextCompat.getDrawable(context, R.drawable.bg_chat_pop_me_custom_circle_border)
        drawable!!.setTint(context.getColor(R.color.color_025BE8))
        drawable
    }

    fun bind(adapter: ChatMessageListAdapter3, data: ChatMessageResult) = data.run {
        val checkedContent = adapter.checkContent(content, userType)
        val stringBuilder = SpannableStringBuilder()
        val context = binding.root.context
        val tagColor = if (adapter.isAdmin) R.color.color_025BE8 else R.color.color_E23434
        val checkTag = SpannableString(adapter.checkTag(stringBuilder, context, checkedContent, tagColor))

        binding.tvMessage.mixFontText = checkTag

        //region 處理圖片和文字訊息以及排版
        adapter.setupPictureAndTextMessage(
            this@run,
            binding.ivChatImage,
            binding.tvMessage,
            binding.messageBorder
        )

        binding.tvTime.text = msgTime
        binding.paddingView.isGone = binding.ivChatImage.isVisible && binding.tvMessage.isVisible

        if (UserMessageStyle.isAdmin(userType)) {
            binding.tvMessage.setTextColor(ContextCompat.getColor(context, UserMessageStyle.getTextColor(userType)))
            binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_admin_circle_me)
            binding.messageBorder.backgroundTintList = null
        } else {
            binding.tvMessage.setTextColor(Color.WHITE)
            //0游客、1会员、2管理员(特殊處理)、3訪客
            binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_me_custom_circle_border)
            binding.messageBorder.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.color_025BE8))
        }
    }

}
