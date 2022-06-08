package org.cxct.sportlottery.ui.game.home

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.home_game_card_v2.view.*
import org.cxct.sportlottery.R

class HomeGameCardV2 @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    LinearLayout(context, attrs, defStyle) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.home_game_card_v2, this, false)
        addView(view)

        val typedArray = context.theme
            .obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        try {
            view.tv_title.text = typedArray.getText(R.styleable.CustomView_cvTitle)
            view.tv_count.text = typedArray.getText(R.styleable.CustomView_cvCount)
            val textSize = typedArray.getInt(R.styleable.CustomView_cvTextSize, 12)
            view.tv_count.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize.toFloat())
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

    fun setTitle(title: SpannableStringBuilder) {
        tv_title.text = title
    }

    fun setCount(count: String?) {
        tv_count.text = count
    }

    fun setIcon(@DrawableRes drawableRes: Int) {
        iv_icon.setImageResource(drawableRes)
    }

    fun setCount(num: Int) {
        tv_count.text = num.toString()
    }

    fun setDividerVisibility(show: Boolean) {
        divider.isVisible = show
    }
}