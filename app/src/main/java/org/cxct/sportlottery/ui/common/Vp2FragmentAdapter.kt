package org.cxct.sportlottery.ui.common

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * ViewPager2： Fragment 切換
 * https://www.jianshu.com/p/25aa5cacbfb9
 */
class Vp2FragmentAdapter(private val fragList: List<Fragment>, parentFragment: Fragment) : FragmentStateAdapter(parentFragment) {
    override fun getItemCount(): Int {
        return fragList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragList[position]
    }
}
