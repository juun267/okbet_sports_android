package org.cxct.sportlottery.ui.common

import android.content.Context
import android.content.res.TypedArray
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

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_base_tool_bar_no_drawer, this, true)
        initView(context.theme.obtainStyledAttributes( attrs, R.styleable.CustomTopToolbar,0, 0), view)
    }

    var titleText: String? = null
        set(value) {
            tv_toolbar_title.setTitleLetterSpacing()
            field = value
            tv_toolbar_title.text = value
        }

    fun setOnBackPressListener(listener: () -> Unit) {
        btn_toolbar_back.setOnClickListener { listener.invoke() }
    }

    private fun initView(typedArray: TypedArray, view: View) {
        view.apply {
            tv_toolbar_title.setTitleLetterSpacing()
            tv_toolbar_title.text = typedArray.getString(R.styleable.CustomTopToolbar_topTitleText) ?:""
        }

        typedArray.recycle()
    }

}