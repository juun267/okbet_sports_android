package org.cxct.sportlottery.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentHelper(
    var fragmentManager: FragmentManager,
    private val viewId: Int,
    private val fragments: Array<Fragment>,
) {
    private var curPos = -1
    fun showFragment(index: Int) {
        if (curPos == index) {
            return
        }
        if (curPos >= 0) {
            switchContent(fragments[curPos], fragments[index])
        } else {
            switchContent(null, fragments[index])
        }
        curPos = index
    }

    fun switchContent(from: Fragment?, to: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        if (from == null) {
            transaction.replace(viewId, to).commitAllowingStateLoss()
        } else if (from !== to) {
            if (!to.isAdded) {
                transaction.hide(from).add(viewId, to).commitAllowingStateLoss()
            } else {
                transaction.hide(from).show(to).commitAllowingStateLoss()
            }
        }
    }
}