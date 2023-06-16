package org.cxct.sportlottery.ui.sport.list.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp

class EmptySportGamesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        layoutParams = FrameLayout.LayoutParams(-1, 212.dp)
        gravity = Gravity.CENTER
        orientation = VERTICAL

        val img = AppCompatImageView(context)
        img.setImageResource(R.drawable.img_no_sportgames)
        120.dp.let { addView(img, LayoutParams(it, it)) }

        val text = AppCompatTextView(context)
        text.setTextColor(ContextCompat.getColor(context, R.color.color_BEC7DC))
        text.textSize = 14f
        text.text = "There is no games yet!"
        addView(text, LayoutParams(-2, -2))
    }



}