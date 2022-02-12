package org.cxct.sportlottery.ui.common

import android.content.Context
import android.graphics.Typeface
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.content_security_code_style_edittext.view.*
import org.cxct.sportlottery.R

class SecurityCodeStyleEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val typedArray by lazy {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SecurityCodeStyleEditText, 0, 0)
    }

    var mClearEdittextListener: OnClickListener ? = null

    init {
        initView()
    }

    private fun initView() {
        try {
            inflate(context, R.layout.content_security_code_style_edittext, this).apply {
                val labelText = typedArray.getText(R.styleable.SecurityCodeStyleEditText_scLabelText) ?: ""
                val hintText = typedArray.getText(R.styleable.SecurityCodeStyleEditText_scHintText) ?: ""
                val errorText = typedArray.getText(R.styleable.SecurityCodeStyleEditText_scErrorText) ?: ""

                setHint(labelText,hintText)
                setView(errorText)


                edt_security_code.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun afterTextChanged(p0: Editable?) {
                        mClearEdittextListener?.onClick(null)
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    fun showErrorStatus(b: Boolean) {
        ic_error.isVisible = b
        txv_error_code.isVisible = b
    }

    private fun setView(errorText: CharSequence?) {
        txv_error_code.text = errorText
    }

    private fun setHint(labelText: CharSequence, hintText: CharSequence) {

        val hintContentBuilder = SpannableStringBuilder()
        val titleSpan = SpannableString("$labelText   ").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, this.length , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(AbsoluteSizeSpan(13,true), 0, this.length , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorBlackLight)), 0, this.length , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)  // 修改字體
        }
        val detailSpan = SpannableString(hintText).apply {
            setSpan(StyleSpan(Typeface.NORMAL), 0, this.length , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorBlackLight)), 0, this.length , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)  // 修改字體
        }

        edt_security_code.hint =  hintContentBuilder.append(titleSpan).append(detailSpan)
    }

}