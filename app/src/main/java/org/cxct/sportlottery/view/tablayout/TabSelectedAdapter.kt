package org.cxct.sportlottery.view.tablayout

import com.google.android.material.tabs.TabLayout

open class TabSelectedAdapter(private val selectedCallback: ((TabLayout.Tab, Boolean) -> Unit)? = null) : TabLayout.OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab) {
        selectedCallback?.invoke(tab, false)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        selectedCallback?.invoke(tab, true)
    }
}