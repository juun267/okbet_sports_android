package org.cxct.sportlottery.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.lang.ref.WeakReference

class FragmentHelper(
    var fragmentManager: FragmentManager,
    private val viewId: Int,
    private val fragmentClasses: Array<Pair<Class<out Fragment>, Bundle?>>
) {

    private var curPos = -1
    private val fragments: Array<WeakReference<out Fragment>?> =
        Array(fragmentClasses.size) { null }
    private val addedFragment = mutableSetOf<Int>()

    fun getFragment(index: Int): Fragment {
        var fragment = fragments[index]?.get()
        if (fragment == null) {
            val fClass = fragmentClasses[index]
            fragment = fClass.first.newInstance()
            fClass.second?.let { fragment!!.arguments = it }
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
        } else if (from != to) {
            if (to.isAdded || addedFragment.contains(to.hashCode())) {
                transaction.hide(from).show(to).commitAllowingStateLoss()
            } else {
                addedFragment.add(to.hashCode())
                transaction.hide(from).add(viewId, to).commitAllowingStateLoss()
            }
        }
    }

    fun getCurrentFragmentByPos(curPos: Int): Fragment {
        this.curPos = curPos
        return getFragment(curPos)
    }

    fun getFragmentList(): Array<Fragment?> {
        return Array(fragments.size) { fragments.get(it)?.get() }
    }

    fun getCurrentFragment(): Fragment {
        return getFragment(curPos)
    }

    fun getCurrentPosition(): Int {
        return curPos
    }
}