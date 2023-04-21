package org.cxct.sportlottery.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

class IndicatorWidget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private val childParams = LayoutParams(-2, -2)
    var itemPadding = 0

    lateinit var defaultDrawable: Drawable
    lateinit var selectedDrawable: Drawable

    fun setupIndicator(itemCount: Int, defaultPosition: Int = 0) {
        removeAllViews()
        if (itemCount < 1) return

        if(orientation == VERTICAL) {
            childParams.topMargin = itemPadding
            childParams.bottomMargin = itemPadding
        } else {
            childParams.leftMargin = itemPadding
            childParams.rightMargin = itemPadding
        }

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

    /**
     * 需要在viewpager2绑定adapter后调用
     */
    fun setUpViewPager2(viewPager2: ViewPager2) {
        val itemCount = viewPager2.adapter?.itemCount ?: return
        setupIndicator(itemCount)

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                update(position)
            }
        })
    }

    /**
     * 需要在viewpager绑定adapter后调用
     */
    fun setUpViewPager(viewPager: ViewPager) {
        val itemCount = viewPager.adapter?.count ?: return
        setupIndicator(itemCount)

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                update(position)
            }
        })

    }


}