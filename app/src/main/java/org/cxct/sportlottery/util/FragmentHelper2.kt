package org.cxct.sportlottery.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentHelper2(
    private val fragmentManager: FragmentManager,
    private val viewId: Int) {

    private val fragmentHolder = ObjectHolder()

    private var current: Fragment? = null

    fun <T: Fragment> show(fragment: Class<T>,
                           bundle: Bundle? = null,
                           block: ((T, Boolean) -> Unit)? = null): T {

        if (current?.javaClass == fragment) {
            bundle?.let { current!!.arguments?.putAll(it) }
            block?.invoke(current!! as T, false)
            return current as T
        }

        var fragmentInstance = fragmentHolder.take(fragment)
        if (fragmentInstance == null) {
            fragmentInstance = fragment.newInstance()
            fragmentInstance!!.arguments = bundle
            fragmentHolder.put(fragmentInstance)
            block?.invoke(fragmentInstance!!, true)
        } else {
            bundle?.let { fragmentInstance!!.arguments?.putAll(it) }
            block?.invoke(fragmentInstance!!, false)
        }

        val transaction = fragmentManager.beginTransaction()
        current?.let { transaction.remove(it) }
        current = fragmentInstance!!
        transaction.add(viewId, fragmentInstance!!).commitAllowingStateLoss()
        return current as T
    }

    fun currentFragment() = current

    fun destory() {
        if (current != null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.remove(current!!).commitAllowingStateLoss()
            current = null
        }
    }

}