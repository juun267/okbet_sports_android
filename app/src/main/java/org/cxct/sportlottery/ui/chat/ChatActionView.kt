package org.cxct.sportlottery.ui.chat

import android.content.Context
import android.content.Intent
import android.text.InputFilter
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.drake.spannable.addSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.ColorSpan
import com.drake.spannable.span.HighlightSpan
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.view.onClick

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
    private val linearEdit:LinearLayout
    private val ivEmoji:ImageView
    init {
        LayoutInflater.from(context).inflate(R.layout.view_chat_action, this, true)
        ivSend = findViewById(R.id.ivSend)
        ivUploadImage = findViewById(R.id.ivUploadImage)
        etInput = findViewById(R.id.etInput)
        linearEdit= findViewById(R.id.linearEdit)
        ivEmoji= findViewById(R.id.ivEmoji)
        setLoginStatus()
    }

    private fun setLoginStatus() {
        if (LoginRepository.isLogined()) {
            return
        }

        etInput.gone()
        val textView = findViewById<TextView>(R.id.tvToLoging)
        textView.visible()
        textView.movementMethod = LinkMovementMethod()
        textView.text = "${resources.getString(R.string.N987)}, "
            .setSpan(ColorSpan(resources.getColor(R.color.color_A7B2C4)))
            .addSpan(resources.getString(R.string.N988), listOf(UnderlineSpan(), HighlightSpan(resources.getColor(R.color.color_025BE8)) {
                context.startActivity(Intent(context, LoginOKActivity::class.java))
            }))
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
        linearEdit.background = ContextCompat.getDrawable(
            context,
            if (isEnable) R.drawable.bg_chat_input else R.drawable.bg_chat_input_disable
        )
        ivEmoji.visibility=if(isEnable){
            View.VISIBLE
        }else{
            View.GONE
        }
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


    private var expandEmoji=false
    fun setOnEmojiClick(){
        ivEmoji.onClick {
            if(expandEmoji){
                ivEmoji.setImageResource(R.drawable.ic_chat_emoji_normal)
                expandEmoji=false
            }else{
                ivEmoji.setImageResource(R.drawable.ic_chat_emoji_press)
                expandEmoji=true
            }
        }
    }
}
