package org.cxct.sportlottery.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class DotIndicator@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private val childParams = LayoutParams(-2, -2)
    var dotPadding = 0

    var defaultDrawable: Drawable? = null
        get() {
            if (field == null) {
                field = createDef()
            }
            return field
        }

    var selectedDrawable: Drawable? = null
        get() {
            if (field == null) {
                field = createSelected()
            }
            return field
        }


    private fun createDef(): Drawable {
        val wh = 4.dp.toFloat()
        return DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#7599FF"))
            .setShapeAlpha(0.5f)
            .setCornersRadius(wh)
            .setSizeHeight(wh)
            .setSizeWidth(wh)
            .build()
    }

    private fun createSelected(): Drawable {
        val w = 4.dp.toFloat()
        val h = 12.dp.toFloat()
        return DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#7599FF"))
            .setCornersRadius(w)
            .setSizeHeight(w)
            .setSizeWidth(h)
            .build()
    }

    fun setupIndicator(itemCount: Int, defaultPosition: Int = 0) {
        removeAllViews()
        if (itemCount <= 0) return

        childParams.leftMargin = dotPadding
        childParams.rightMargin = dotPadding
        repeat(itemCount) { index ->
            val view = ImageView(context)
            view.setImageDrawable(if (index == defaultPosition) selectedDrawable else defaultDrawable)
            addView(view, childParams)
        }
    }

    fun update(position: Int) {
        if (position < 0 || position >= childCount) return

        repeat(childCount) { index ->
            (getChildAt(index) as ImageView).setImageDrawable(if (index == position) selectedDrawable else defaultDrawable)
        }
    }

    // 需要在viewpager绑定adapter后调用
    fun setUpViewPager2(viewPager2: ViewPager2) {
        val itemCount = viewPager2.adapter?.itemCount ?: return
        setupIndicator(itemCount)
        viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                update(position)
            }

        })

    }
}