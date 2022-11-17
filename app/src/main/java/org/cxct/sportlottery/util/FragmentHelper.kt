package org.cxct.sportlottery.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.lang.ref.WeakReference

class FragmentHelper(
    var fragmentManager: FragmentManager,
    private val viewId: Int,
    private val fragmentClasses: Array<Class<out Fragment>>
) {

    private var curPos = -1
    private val fragments: Array<WeakReference<out Fragment>?> = Array(fragmentClasses.size) { null }

    fun getFragment(index: Int): Fragment {
        var fragment = fragments[index]?.get()
        if (fragment == null) {
            fragment = fragmentClasses[index].newInstance()
            fragments[index] = WeakReference(fragment)
        }
        return fragment!!
    }

    fun showFragment(index: Int): Fragment {
        if (curPos == index) {
            return getCurrentFragment()
        }

        val fragment = getFragment(index)
        if (curPos >= 0) {
            switchContent(fragments[curPos]?.get(), fragment)
        } else {
            switchContent(null, fragment)
        }

        curPos = index
        return fragment
    }

    private fun switchContent(from: Fragment?, to: Fragment) {
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

    fun getFragmentList(): Array<Fragment?> {
        return Array(fragments.size) { fragments.get(it)?.get() }
    }

    fun getCurrentFragment(): Fragment {
        return getFragment(curPos)
    }
}