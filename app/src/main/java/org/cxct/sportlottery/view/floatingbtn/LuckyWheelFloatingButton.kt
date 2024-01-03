package org.cxct.sportlottery.view.floatingbtn

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp

class LuckyWheelFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {


    init {

        val lp = LayoutParams(-2, -2)
        lp.gravity = Gravity.RIGHT or Gravity.BOTTOM
        lp.rightMargin = 10.dp
        lp.bottomMargin = 300.dp
        layoutParams = lp

        val imageView = AppCompatImageView(context)
        imageView.setImageResource(R.drawable.ic_lucky_wheel)
        imageView.adjustViewBounds = true
        val imgLP = LayoutParams(96.dp, -2)
        imgLP.topMargin = 6.dp
        addView(imageView, imgLP)

        val ivClose = AppCompatImageView(context)
        val dp15 = 15.dp
        val closeLP = LayoutParams(dp15, dp15)
        closeLP.gravity = Gravity.RIGHT
        closeLP.rightMargin = 9.dp
        ivClose.setImageResource(R.drawable.ic_close_float)
        addView(ivClose, closeLP)

        setOnTouchListener(SuckEdgeTouch())
        setOnClickListener { LuckyWheelManager.instance.clickContent() }
        ivClose.setOnClickListener { LuckyWheelManager.instance.clickCloseFloatBtn() }
    }

}