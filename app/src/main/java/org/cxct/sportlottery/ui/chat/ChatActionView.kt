package org.cxct.sportlottery.ui.chat

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewChatActionBinding

/**
 * @author kevin
 * @create 2023/3/13
 * @description
 */
class ChatActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {

    lateinit var binding: ViewChatActionBinding

    init {
        if (!isInEditMode) {
            binding = ViewChatActionBinding.inflate(LayoutInflater.from(context))
            addView(binding.root)
        }
    }

    fun setViewStatus(isEnable: Boolean) {
        setUploadImageStatus(isEnable)
        setInputStatus(isEnable)
    }

    fun setSendStatus(isEnable: Boolean) {
        binding.ivSend.apply {
            setImageResource(
                if (isEnable) R.drawable.ic_chat_send else R.drawable.ic_chat_send_disable
            )
            isEnabled = isEnable
        }
    }

    fun setUploadImageStatus(isEnable: Boolean) {
        binding.ivUploadImage.apply {
            setImageResource(
                if (isEnable) R.drawable.ic_chat_upload_image else R.drawable.ic_chat_upload_image_disable
            )
            isEnabled = isEnable
        }
    }

    fun setInputStatus(isEnable: Boolean) {
        binding.etInput.apply {
            background = ContextCompat.getDrawable(
                context,
                if (isEnable) R.drawable.bg_chat_input else R.drawable.bg_chat_input_disable
            )
            hint = when {
                isEnable -> {
                    context.getString(R.string.chat_enter_chat_content)
                }
                else -> {
                    text?.clear() //禁止發言時應清除已輸入的文字
                    context.getString(R.string.chat_currently_ban_to_speak)
                }
            }
            setHintTextColor(
                ContextCompat.getColor(
                    context,
                    if (isEnable) R.color.color_chat_action_edittext_hint_text else R.color.color_chat_action_edittext_hint_text_disable
                )
            )
            isEnabled = isEnable
        }
    }

    fun setInputMaxLength(maxLength: Int) {
        if (maxLength == 0) return //@Ying: maxLength 0 代表不限制
        binding.etInput.apply {
            filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
        }
    }
}
