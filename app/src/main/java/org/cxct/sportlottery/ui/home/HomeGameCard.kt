package org.cxct.sportlottery.ui.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.card_home_game.view.*
import org.cxct.sportlottery.R

class HomeGameCard @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.card_home_game, this, false)
        addView(view)

        try {
            val typedArray = context.theme
                .obtainStyledAttributes(attrs, R.styleable.HomeGameCard, 0, 0)

            view.tv_title.text = typedArray.getText(R.styleable.HomeGameCard_hgCard_title)
            view.tv_count.text = typedArray.getText(R.styleable.HomeGameCard_hgCard_count)
            view.iv_icon.setImageResource(typedArray.getResourceId(R.styleable.HomeGameCard_hgCard_icon, -1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}