package org.cxct.sportlottery.event

import androidx.fragment.app.Fragment
import org.cxct.sportlottery.ui.maintab.worldcup.HomeWorldCupFragment

// 首页tab切换
data class HomeTabEvent(val fragment: Fragment) {

    fun isWorldCupTab(): Boolean {
        return fragment is HomeWorldCupFragment
    }
}