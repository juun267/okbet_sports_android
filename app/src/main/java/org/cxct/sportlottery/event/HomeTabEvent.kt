package org.cxct.sportlottery.event

import androidx.fragment.app.Fragment

// 首页tab切换
data class HomeTabEvent(val fragment: Fragment) {

    fun isWorldCupTab(): Boolean {
        return false
    }
}