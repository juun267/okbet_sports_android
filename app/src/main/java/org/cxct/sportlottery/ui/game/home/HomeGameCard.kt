package org.cxct.sportlottery.ui.game.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import kotlinx.android.synthetic.main.home_game_card.*
import kotlinx.android.synthetic.main.home_game_card.view.*
import me.jessyan.autosize.utils.AutoSizeUtils
import org.cxct.sportlottery.R

class HomeGameCard @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.home_game_card, this, false)
        addView(view)

        view.post {
            cl_card.layoutParams.height = AutoSizeUtils.dp2px(context,50f)
        }

        val typedArray = context.theme
            .obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        try {
            view.tv_title.text = typedArray.getText(R.styleable.CustomView_cvTitle)
            view.tv_count.text = typedArray.getText(R.styleable.CustomView_cvCount)
            view.iv_icon.setImageResource(typedArray.getResourceId(R.styleable.CustomView_cvIcon, -1))
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