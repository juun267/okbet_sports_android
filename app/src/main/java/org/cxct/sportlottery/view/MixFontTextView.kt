package org.cxct.sportlottery.view

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.VerifyConstUtil.isValidChineseWord

/**
 * @author kevin
 * @create 2022/12/29
 * @description
 */
class MixFontTextView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle,
) : AppCompatTextView(context, attributeSet, defStyleAttr) {


    private val fontOther = ResourcesCompat.getFont(context, R.font.din_regular)


    var mixFontText: CharSequence? = null
        set(value) {
            if (checkEmoji(value.toString())) { //若包含emoji則為特例處理，使用一般setText
                text = value
            } else {
                setMixText(value)
            }
        }

    init {
        if (!isInEditMode) init(attributeSet)
    }

    private fun init(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MixFontTextView)
        text = typedArray.getString(R.styleable.MixFontTextView_mix_font_text) ?: ""
        typedArray.recycle()
    }

    private fun setMixText(text: CharSequence?) {
        val ssb = SpannableStringBuilder(text)
        ssb.forEachIndexed { index, c ->
            ssb.setSpan(
                fontOther?.let {CustomTypefaceSpan(it) },
                index,
                index + 1,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
        setText(ssb)
    }

    //檢查是否包含emoji
    private fun checkEmoji(text: String): Boolean {
        val emojiPattern = Regex("[\uD83C-\uDBFF\uDC00-\uDFFF]+")
        var result: String? = null
        emojiPattern.findAll(text).forEach {
            result = it.value
        }
        return result != null
    }
}
