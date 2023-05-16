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
import org.cxct.sportlottery.databinding.ItemChatMessageUserBinding as ICMUB
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.ui.chat.ChatMessageListAdapter3
import org.cxct.sportlottery.ui.chat.bean.UserMessageStyle
import org.cxct.sportlottery.util.TimeUtil

class UserVH(parent: ViewGroup,
             private val binding: ICMUB = ICMUB.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {

    fun bind(adapter: ChatMessageListAdapter3, data: ChatMessageResult) = data.run {
        binding.tvName.text = nickName
        val checkedContent = adapter.checkContent(content, userType)
        val stringBuilder = SpannableStringBuilder()
        val checkTag =
            SpannableString(adapter.checkTag(stringBuilder, binding.root.context, checkedContent))
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
        adapter.setupPictureAndTextMessage(
            this@run,
            binding.ivChatImage,
            binding.tvMessage,
            binding.messageBorder,
        ) { adapter.onPhotoClick(it) }

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
                adapter.onUserAvatarClick(Pair(userIdString, nickNameString))
            }
        }
    }

}