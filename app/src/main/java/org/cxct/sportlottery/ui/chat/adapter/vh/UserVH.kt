package org.cxct.sportlottery.ui.chat.adapter.vh

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.UserVipType.setLevelTagIcon
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemChatMessageUserBinding as ICMUB
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.ui.chat.adapter.ChatMessageListAdapter3
import org.cxct.sportlottery.ui.chat.bean.UserMessageStyle
import org.cxct.sportlottery.util.TimeUtil

class UserVH(parent: ViewGroup,
             private val binding: ICMUB = ICMUB.inflate(LayoutInflater.from(parent.context), parent, false)
): BaseViewHolder(binding.root) {

    fun bind(adapter: ChatMessageListAdapter3, data: ChatMessageResult) = data.run {
        binding.tvName.text = nickName
        binding.ivVipLevel.setLevelTagIcon(data.userLevelCode)
        val checkedContent = adapter.checkContent(content, userType)
        val stringBuilder = SpannableStringBuilder()
        val context = binding.tvName.context
        val checkTag = SpannableString(adapter.checkTag(stringBuilder, context, checkedContent))
        binding.tvMessage.mixFontText = checkTag
        binding.tvMessage.setTextColor(ContextCompat.getColor(context, UserMessageStyle.getTextColor(userType)))
        //region 處理圖片和文字訊息以及排版
        adapter.setupPictureAndTextMessage(
            this@run,
            binding.ivChatImage,
            binding.tvMessage,
            binding.messageBorder,
        )

        binding.paddingView.isGone = binding.ivChatImage.isVisible && binding.tvMessage.isVisible
        //endregion
        binding.tvTime.text = TimeUtil.timeFormat(curTime, TimeUtil.HM_FORMAT)

        if (UserMessageStyle.isAdmin(userType)) {
            binding.ivAvatar.apply {
//                load(iconUrl, R.drawable.ic_chat_admin)
                load(iconUrl, R.drawable.ic_person_avatar)
                borderColor = ContextCompat.getColor(context,R.color.color_025BE8)
                borderWidth = 2f
            }
            binding.ivHeadAdmin.isVisible = true
            binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_admin_circle)
            binding.messageBorder.backgroundTintList = null
        } else {
            binding.ivAvatar.borderWidth = 0f
            binding.ivHeadAdmin.isVisible = false
            binding.ivAvatar.load(iconUrl, R.drawable.ic_person_avatar)

            //0游客、1会员、2管理员(特殊處理)、3訪客
            binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_user_custom_circle_border)
//            binding.messageBorder.backgroundTintList =
//                ColorStateList.valueOf(ContextCompat.getColor(context, UserMessageStyle.getBorderColor(userType)))
        }

        //點擊用戶暱稱
        binding.tvName.setOnClickListener {
            var userIdString = userId?.toString()
            var nickNameString = nickName.orEmpty()
            if (userIdString.isNotEmpty() && nickNameString.isNotEmpty()) {
                userIdString = "[@:$userIdString]" //包裝過的格式 "[@:userId]"
                nickNameString = "@$nickNameString" //輸入匡要顯示 "@nickName"
                adapter.onUserAvatarClick(Pair(userIdString, nickNameString))
            }
        }
    }

}