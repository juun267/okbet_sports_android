package org.cxct.sportlottery.ui.main

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.home_game_card.view.*

class MainTab @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    init {
        val view = LayoutInflater.from(context).inflate(org.cxct.sportlottery.R.layout.main_tab, this, false)
        addView(view)

        val typedArray = context.theme
            .obtainStyledAttributes(attrs, org.cxct.sportlottery.R.styleable.CustomView, 0, 0)
        try {
            view.tv_title.text = typedArray.getText(org.cxct.sportlottery.R.styleable.CustomView_cvTitle)
            view.iv_icon.setImageResource(typedArray.getResourceId(org.cxct.sportlottery.R.styleable.CustomView_cvIcon, -1))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        //被選中時字體加粗
        val typeface = if (selected) Typeface.BOLD else Typeface.NORMAL
        tv_title.setTypeface(null, typeface)
    }

}