package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * 客製化 TabLayout
 */
class CustomTopToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val typedArray by lazy {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomTopToolbar,
            0,
            0
        )
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_base_tool_bar_no_drawer, this, false)
        addView(view)
        initView(view)
    }

    var titleText: String? = null
        set(value) {
            tv_toolbar_title.setTitleLetterSpacing()
            field = value
            tv_toolbar_title.text = value
        }

    var backPressListener: (() -> Unit)? = null

    fun setOnBackPressListener(listener: () -> Unit) {
        backPressListener = listener
    }

    private fun initView(view: View) {
        view.apply {
            tv_toolbar_title.setTitleLetterSpacing()
            tv_toolbar_title.text = typedArray.getString(R.styleable.CustomTopToolbar_topTitleText) ?:""
            btn_toolbar_back.setOnClickListener {
                backPressListener?.invoke()
            }
        }
    }

}