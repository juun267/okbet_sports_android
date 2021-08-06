package org.cxct.sportlottery.ui.menu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.menu_left_item.view.*
import org.cxct.sportlottery.R

class MenuLeftItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : ConstraintLayout(context, attrs, defStyle) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_left_item, this, false)
        addView(view)

        val typedArray = context.theme
            .obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        try {
            view.tv_title.text = typedArray.getText(R.styleable.CustomView_cvTitle)
            view.tv_count.text = typedArray.getText(R.styleable.CustomView_cvCount)
            view.iv_icon.setImageResource(typedArray.getResourceId(R.styleable.CustomView_cvIcon, 0))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    fun setCount(count: String?) {
        tv_count.text = count
    }
}