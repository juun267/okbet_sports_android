package org.cxct.sportlottery.ui.menu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.menu_item.view.*
import org.cxct.sportlottery.R

class MenuItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : ConstraintLayout(context, attrs, defStyle) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_item, this, false)
        addView(view)

        try {
            val typedArray = context.theme
                .obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)

            view.tv_title.text = typedArray.getText(R.styleable.CustomView_cvTitle)
            view.iv_icon.setImageResource(typedArray.getResourceId(R.styleable.CustomView_cvIcon, -1))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}