package org.cxct.sportlottery.ui.common

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.cxct.sportlottery.R

class SecurityCodeStyleEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val typedArray by lazy {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
    }

    init {
        initView(attrs)
    }


    private fun initView(attrs: AttributeSet?) {
        try {
            inflate(context, R.layout.content_security_code_style_edittext, this).apply {

            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()//使用完回收
        }

    }


}