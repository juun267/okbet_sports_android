package org.cxct.sportlottery.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.lang.ref.WeakReference

class FragmentHelper(
    var fragmentManager: FragmentManager,
    private val viewId: Int,
    private val fragmentClasses: Array<Param>,
) {

    private var curPos = -1
    private val fragments: Array<WeakReference<out Fragment>?> =
        Array(fragmentClasses.size) { null }
    private val addedFragment = mutableSetOf<Int>()

    fun getFragment(index: Int): Fragment {
        var fragment = fragments[index]?.get()
        if (fragment == null) {
            val fClass = fragmentClasses[index]
            fragment = fClass.clazz.newInstance()
            fClass.bundle?.let { fragment!!.arguments = it }
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
            switchContent(fragments[curPos]?.get(), fragment, index)
        } else {
            switchContent(null, fragment, index)
        }

        curPos = index
        return fragment
    }

    private fun switchContent(from: Fragment?, to: Fragment, index: Int) {
        val transaction = fragmentManager.beginTransaction()
        val hashCode = to.hashCode()
        val tag = to.javaClass.name;
        if (from == null) {
            addedFragment.add(hashCode)
            transaction.replace(viewId, to, tag).commitAllowingStateLoss()
            return
        }

        if (from != to) {
            val param = fragmentClasses[curPos]
            if (param.needRemove) {
                addedFragment.remove(from.hashCode())
                transaction.remove(from)
            } else {
                transaction.hide(from)
            }
            // fixed java.lang.IllegalStateException: Fragment already added:
            if (to.isAdded || addedFragment.contains(hashCode)|| fragmentManager.findFragmentByTag(tag)!=null) {
                transaction.show(to).commitAllowingStateLoss()
            } else {
                transaction.add(viewId, to,tag).commitAllowingStateLoss()
            }
            addedFragment.add(hashCode)
        }
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

data class Param(
    val clazz: Class<out Fragment>,
    val bundle: Bundle? = null,     // fragment创建时的bundle
    val needRemove: Boolean = false, // 当为true时切换到不可见时remove掉
)