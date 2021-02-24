package org.cxct.sportlottery.ui.common

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

/**
 * 20181130
 * 於原始 MarqueeView 再添加 setTextColor function
 * 才可以在 code 中動態改變 text color
 */
class NoScrollViewPager : ViewPager {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}

    override fun canScrollHorizontally(direction: Int): Boolean {
        return false
    }
}