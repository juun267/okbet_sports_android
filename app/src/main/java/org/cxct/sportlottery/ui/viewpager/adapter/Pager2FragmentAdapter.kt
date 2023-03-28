package org.cxct.sportlottery.ui.viewpager.adapter

import android.util.Log
import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.lang.ref.WeakReference

class Pager2FragmentAdapter: FragmentStateAdapter {

    private val pages: FragmentPagerItems
    private val holder: SparseArrayCompat<WeakReference<Fragment>>

    constructor(fragmentActivity: FragmentActivity, pages: FragmentPagerItems): super(fragmentActivity.supportFragmentManager, fragmentActivity.lifecycle) {
        this.pages = pages
        this.holder = SparseArrayCompat(pages.size)
    }


    constructor(fragment: Fragment, pages: FragmentPagerItems): super(fragment.childFragmentManager, fragment.lifecycle) {
        this.pages = pages
        this.holder = SparseArrayCompat(pages.size)
    }


    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        var fragment = holder[position]?.get()
        if (fragment == null) {
            fragment = pages[position].instantiate(pages.context,position)
            holder.put(position, WeakReference(fragment))
        }
        return fragment
    }

    fun getFragmentAt(position: Int): Fragment? {
        Log.e("POSITION","POSITION:$position")
        if(holder[position] == null){
            createFragment(position)
        }
        return holder[position]?.get()
    }

}