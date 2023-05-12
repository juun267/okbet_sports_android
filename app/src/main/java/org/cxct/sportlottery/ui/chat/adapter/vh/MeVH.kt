package org.cxct.sportlottery.ui.chat.adapter.vh

import android.content.res.ColorStateList
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemChatMessageMeBinding
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatReceiveContent
import org.cxct.sportlottery.ui.chat.ChatMessageListAdapter2
import org.cxct.sportlottery.ui.chat.bean.UserMessageStyle
import org.cxct.sportlottery.util.TimeUtil

class MeVH (parent: ViewGroup,
            private val onPhotoClick: (String) -> Unit,
            private val binding: ItemChatMessageMeBinding = ItemChatMessageMeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
): RecyclerView.ViewHolder(binding.root) {

    fun bind(data: ChatReceiveContent<*>) {
        data.getThisContent<ChatMessageResult>()?.apply {
            val checkedContent = ChatMessageListAdapter2.checkContent(content, userType)
            val stringBuilder = SpannableStringBuilder()
            val checkTag =
                SpannableString(ChatMessageListAdapter2.checkTag(stringBuilder, binding.root.context, checkedContent))
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
            ChatMessageListAdapter2.setupPictureAndTextMessage(
                this@apply,
                binding.ivChatImage,
                binding.tvMessage,
                binding.messageBorder
            ) { onPhotoClick(it) }

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
}
