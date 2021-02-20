package org.cxct.sportlottery.ui.home2.next

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class VPFragmentAdapter(private val titleList: List<Int>, parentFragment: Fragment): FragmentStateAdapter(parentFragment) {

    override fun getItemCount(): Int {
        return titleList.size
    }

    override fun createFragment(position: Int): Fragment {

        when (position) {
            0 -> return SportFragment()
            1 -> return CPFragment()
            2 -> return LiveFragment()
            3 -> return QPFragment()
            4 -> return DZFragment()
            5 -> return BYFragment()
        }
        return SportFragment()
    }
}