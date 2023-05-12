package org.cxct.sportlottery.ui.chat.bean

import org.cxct.sportlottery.R

data class UserMessageStyle private constructor(
    val code: String,
    val borderColor: Int,
    val fillColor: Int,
    val textColor: Int,
) {
    
    companion object {

        private val GUEST = UserMessageStyle(
            "0",
            R.color.color_chat_message_border_guest,
            R.color.color_chat_message_fill_guest,
            R.color.color_chat_message_text_guest
        )

        private val MEMBER = UserMessageStyle(
            "1",
            R.color.color_chat_message_border_member,
            R.color.color_chat_message_fill_member,
            R.color.color_chat_message_text_member
        )

        private val ADMIN = UserMessageStyle(
            "2",
            R.color.color_chat_message_border_admin,
            R.color.color_chat_message_fill_admin,
            R.color.color_chat_message_text_admin
        )

        private val VISITOR = UserMessageStyle(
            "3",
            R.color.color_chat_message_border_guest,
            R.color.color_chat_message_fill_guest,
            R.color.color_chat_message_text_guest
        )

        private fun getStyle(userType: String?): UserMessageStyle = when (userType) {
            GUEST.code -> GUEST
            ADMIN.code -> ADMIN
            MEMBER.code -> MEMBER
            VISITOR.code -> VISITOR
            else -> VISITOR
        }

        fun getBorderColor(userType: String?): Int = getStyle(userType)?.borderColor
        fun getFillColor(userType: String?): Int = getStyle(userType)?.fillColor
        fun getTextColor(userType: String?): Int = getStyle(userType)?.textColor
        fun isAdmin(userType: String?) = ADMIN.code == userType

    }
}