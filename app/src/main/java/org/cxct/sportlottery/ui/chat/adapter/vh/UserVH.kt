package org.cxct.sportlottery.ui.chat.adapter.vh

import android.content.res.ColorStateList
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemChatMessageUserBinding
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatReceiveContent
import org.cxct.sportlottery.ui.chat.ChatMessageListAdapter2
import org.cxct.sportlottery.ui.chat.bean.UserMessageStyle
import org.cxct.sportlottery.util.TimeUtil

class UserVH(parent: ViewGroup,
             private val onPhotoClick: (String) -> Unit,
             private val onUserAvatarClick: (tagUserPair: Pair<String, String>) -> Unit,
             private val binding: ItemChatMessageUserBinding = ItemChatMessageUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {


    fun bind(data: ChatReceiveContent<*>) {
        data.getThisContent<ChatMessageResult>()?.apply {
            binding.tvName.text = nickName
            val checkedContent = ChatMessageListAdapter2.checkContent(content, userType)
            val stringBuilder = SpannableStringBuilder()
            val checkTag =
                SpannableString(ChatMessageListAdapter2.checkTag(stringBuilder, binding.root.context, checkedContent))
            binding.tvMessage.apply {
                mixFontText = checkTag
                setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        UserMessageStyle.getTextColor(userType)
                    )
                )
            }
            //region 處理圖片和文字訊息以及排版
            ChatMessageListAdapter2.setupPictureAndTextMessage(
                this@apply,
                binding.ivChatImage,
                binding.tvMessage,
                binding.messageBorder,
            ) { onPhotoClick(it) }

            binding.paddingView.isGone = binding.ivChatImage.isVisible && binding.tvMessage.isVisible
            //endregion
            binding.tvTime.text =
                if (curTime != null) TimeUtil.timeFormat(
                    curTime.toLong(),
                    TimeUtil.HM_FORMAT
                ) else ""

            if (UserMessageStyle.isAdmin(userType)) {
                binding.ivAvatar.setImageResource(R.drawable.ic_chat_admin)

                binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_admin_circle)

                binding.messageBorder.backgroundTintList = null
            } else {
                Glide.with(binding.root.context)
                    .load(iconUrl)
                    .placeholder(
                        ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.ic_person_avatar
                        )
                    )
                    .error(
                        ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.ic_person_avatar
                        )
                    )
                    .into(binding.ivAvatar)

                //0游客、1会员、2管理员(特殊處理)、3訪客
                binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_user_custom_circle_border)

                binding.messageBorder.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            binding.root.context,
                            UserMessageStyle.getBorderColor(userType)
                        )
                    )
            }

            //點擊用戶暱稱
            binding.tvName.setOnClickListener {
                var userIdString = userId?.toString() ?: ""
                var nickNameString = nickName.orEmpty()
                if (userIdString.isNotEmpty() && nickNameString.isNotEmpty()) {
                    userIdString = "[@:$userIdString]" //包裝過的格式 "[@:userId]"
                    nickNameString = "@$nickNameString" //輸入匡要顯示 "@nickName"
                    onUserAvatarClick(Pair(userIdString, nickNameString))
                }
            }
        }
    }
}