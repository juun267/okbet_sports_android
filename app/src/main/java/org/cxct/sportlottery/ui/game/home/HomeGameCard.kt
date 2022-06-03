package org.cxct.sportlottery.ui.game.home

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.home_game_card.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.setTextTypeFace

class HomeGameCard @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.home_game_card, this, false)
        addView(view)

        val typedArray = context.theme
            .obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        try {
            view.tv_title.text = typedArray.getText(R.styleable.CustomView_cvTitle)
            view.tv_count.text = typedArray.getText(R.styleable.CustomView_cvCount)
            val textSize = typedArray.getInt(R.styleable.CustomView_cvTextSize, 16)
            view.tv_count.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize.toFloat())
            var colorRes = R.color.home_card_text_color
            var padding = 0
            var typeface = Typeface.BOLD
            if (textSize == 12) {
                typeface = Typeface.NORMAL
                colorRes = R.color.home_card_coming_soon_text_color
                padding = 4.dp
            }
            view.tv_title.setTextTypeFace(typeface)
            view.tv_count.setTextColor(ContextCompat.getColor(context, colorRes))
            view.tv_count.setPadding(0, padding, 0, 0)
            view.iv_icon.setImageResource(typedArray.getResourceId(R.styleable.CustomView_cvIcon, 0))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    fun setTitle(title: String?) {
        tv_title.text = title
    }

    fun setCount(count: String?) {
        tv_count.text = count
    }

    fun setIcon(@DrawableRes drawableRes: Int){
        iv_icon.setImageResource(drawableRes)
    }

    fun setCount(num: Int){
        tv_count.text = num.toString()
    }
}