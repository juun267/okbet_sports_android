package org.cxct.sportlottery.ui.chat.adapter.vh

import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemChatMessageMeBinding as ICMMB
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.ui.chat.adapter.ChatMessageListAdapter3
import org.cxct.sportlottery.util.TimeUtil

class MeVH (parent: ViewGroup,
            private val context: Context = parent.context,
            private val binding: ICMMB = ICMMB.inflate(LayoutInflater.from(context), parent, false)
): BaseViewHolder(binding.root) {


    fun bind(adapter: ChatMessageListAdapter3, data: ChatMessageResult) = binding.run {
        val checkedContent = adapter.checkContent(data.content, data.userType)
        val stringBuilder = SpannableStringBuilder()
        val tagColor = /*if (adapter.isAdmin) R.color.color_025BE8 else */R.color.color_FFF500
        val checkTag = SpannableString(adapter.checkTag(stringBuilder, context, checkedContent, tagColor))

        tvMessage.mixFontText = checkTag

        //region 處理圖片和文字訊息以及排版
        adapter.setupPictureAndTextMessage(
            data,
            ivChatImage,
            tvMessage,
            messageBorder
        )

        tvTime.text = TimeUtil.timeFormat(data.curTime, TimeUtil.HM_FORMAT)
        paddingView.isGone = ivChatImage.isVisible && tvMessage.isVisible

//        if (UserMessageStyle.isAdmin(userType)) {
//            binding.tvMessage.setTextColor(ContextCompat.getColor(context, UserMessageStyle.getTextColor(userType)))
//            binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_admin_circle_me)
//            binding.messageBorder.backgroundTintList = null
//        } else {
//            binding.tvMessage.setTextColor(Color.WHITE)
        //0游客、1会员、2管理员(特殊處理)、3訪客
        messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_me_custom_circle_border)
        messageBorder.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.color_025BE8))
//        }
    }

}
