package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.view_indicator.view.*
import org.cxct.sportlottery.R

/**
 * 客製化 ViewPager2 的指示器
 */
class IndicatorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_indicator, this, false)
        addView(view)
    }

    fun setupWithViewPager2(viewPager2: ViewPager2) {
        TabLayoutMediator(tabLayout, viewPager2) { tab, _ ->
            tab.setCustomView(R.layout.indicator_tab)
        }.attach()
    }

}