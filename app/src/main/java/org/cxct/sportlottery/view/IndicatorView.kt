package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewIndicatorBinding
import splitties.systemservices.layoutInflater

/**
 * 客製化 ViewPager2 的指示器
 */
class IndicatorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private val binding by lazy { ViewIndicatorBinding.inflate(layoutInflater,this,false) }
    init {
        addView(binding.root)
    }

    fun setupWithViewPager2(viewPager2: ViewPager2) {
        TabLayoutMediator(binding.tabLayout, viewPager2) { tab, _ ->
            tab.setCustomView(R.layout.indicator_tab)
        }.attach()
    }

}