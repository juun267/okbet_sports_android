package org.cxct.sportlottery.ui.component.tablayout

import com.google.android.material.tabs.TabLayout

open class TabSelectedAdapter(private val selectedCallback: ((TabLayout.Tab) -> Unit)? = null) : TabLayout.OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab) {
        selectedCallback?.invoke(tab)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
    }
}