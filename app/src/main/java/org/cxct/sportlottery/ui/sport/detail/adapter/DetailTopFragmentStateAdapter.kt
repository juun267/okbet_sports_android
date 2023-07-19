package org.cxct.sportlottery.ui.sport.detail.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class DetailTopFragmentStateAdapter(
    val activity: FragmentActivity, val list: MutableList<Fragment>
) : FragmentStateAdapter(activity) {


    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

}