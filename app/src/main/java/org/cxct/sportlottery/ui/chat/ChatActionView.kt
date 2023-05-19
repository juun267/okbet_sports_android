package org.cxct.sportlottery.ui.chat

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R

/**
 * @author kevin
 * @create 2023/3/13
 * @description
 */
class ChatActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {

    val ivSend: ImageView
    val ivUploadImage: ImageView
    val etInput: EditText

    init {
        LayoutInflater.from(context).inflate(R.layout.view_chat_action, this, true)
        ivSend = findViewById(R.id.ivSend)
        ivUploadImage = findViewById(R.id.ivUploadImage)
        etInput = findViewById(R.id.etInput)
    }

    fun setViewStatus(isEnable: Boolean) {
        setUploadImageStatus(isEnable)
        setInputStatus(isEnable)
    }

    fun setSendStatus(isEnable: Boolean) {
        ivSend.isEnabled = isEnable
        ivSend.setImageResource(if (isEnable) R.drawable.ic_chat_send else R.drawable.ic_chat_send_disable)
    }

    fun setUploadImageStatus(isEnable: Boolean) {
        ivUploadImage.isEnabled = isEnable
        ivUploadImage.setImageResource(if (isEnable) R.drawable.ic_chat_upload_image else R.drawable.ic_chat_upload_image_disable)
    }

    fun setInputStatus(isEnable: Boolean) = etInput.run  {
        background = ContextCompat.getDrawable(
            context,
            if (isEnable) R.drawable.bg_chat_input else R.drawable.bg_chat_input_disable
        )
        hint = if(isEnable) {
                context.getString(R.string.chat_enter_chat_content)
            }  else {
                text?.clear() //禁止發言時應清除已輸入的文字
                context.getString(R.string.chat_currently_ban_to_speak)
            }
        setHintTextColor(
            ContextCompat.getColor(
                context,
                if (isEnable) R.color.color_chat_action_edittext_hint_text else R.color.color_chat_action_edittext_hint_text_disable
            )
        )
        isEnabled = isEnable
    }

    fun setInputMaxLength(maxLength: Int) {
        if (maxLength == 0) return //@Ying: maxLength 0 代表不限制
        etInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
    }
}
