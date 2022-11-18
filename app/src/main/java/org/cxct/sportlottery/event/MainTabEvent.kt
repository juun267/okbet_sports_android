package org.cxct.sportlottery.event

import androidx.fragment.app.Fragment
import org.cxct.sportlottery.ui.maintab.HomeFragment

data class MainTabEvent(val fragment: Fragment) {

    fun isHomeTab(): Boolean {
        return fragment is HomeFragment
    }
}