package org.cxct.sportlottery.view

import android.content.Context
import android.graphics.Typeface
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentSecurityCodeStyleEdittextBinding
import splitties.systemservices.layoutInflater

class SecurityCodeStyleEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val typedArray by lazy {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SecurityCodeStyleEditText, 0, 0)
    }

    var mClearEdittextListener: OnClickListener ? = null
    val binding by lazy { ContentSecurityCodeStyleEdittextBinding.inflate(layoutInflater,this,true) }

    init {
        initView()
    }

    private fun initView() {
        try {
            binding.apply {
                val hintText = typedArray.getText(R.styleable.SecurityCodeStyleEditText_scHintText) ?: ""
                val errorText = typedArray.getText(R.styleable.SecurityCodeStyleEditText_scErrorText) ?: ""
                val icon = typedArray.getResourceId(R.styleable.SecurityCodeStyleEditText_scIcon, -1)

                setHint(hintText)
                setView(errorText)
                setEdittext()
                setImg(icon)

            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    private fun setImg(icon: Int) {
        binding.imgIc.setImageResource(icon)
    }

    private fun setEdittext() {
        binding.edtSecurityCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                mClearEdittextListener?.onClick(null)
            }
        })
    }

    fun showErrorStatus(b: Boolean) {
        binding.icError.isVisible = b
        binding.txvErrorCode.isVisible = b
    }

    private fun setView(errorText: CharSequence?) {
        binding.txvErrorCode.text = errorText
    }

    private fun setHint( hintText: CharSequence) {

        val hintContentBuilder = SpannableStringBuilder()
        val detailSpan = SpannableString(hintText).apply {
            setSpan(StyleSpan(Typeface.NORMAL), 0, this.length , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.color_404040_cccccc)), 0, this.length , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)  // 修改字體
        }

        binding.edtSecurityCode.hint =  hintContentBuilder.append(detailSpan)
    }

}