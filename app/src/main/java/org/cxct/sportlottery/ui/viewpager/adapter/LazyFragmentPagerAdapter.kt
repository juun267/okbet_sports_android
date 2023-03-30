package org.cxct.sportlottery.ui.viewpager.adapter

import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import java.lang.ref.WeakReference

class LazyFragmentPagerAdapter: FragmentPagerAdapter {

    private val pages: FragmentPagerItems
    private val holder: SparseArrayCompat<WeakReference<Fragment>>

    constructor(fm: FragmentManager, pages: FragmentPagerItems): super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        this.pages = pages
        this.holder = SparseArrayCompat(pages.size)
    }

    override fun getCount(): Int {
        return pages.size
    }

    override fun getItem(position: Int): Fragment {
        return getPagerItem(position).instantiate(pages.context, position)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val item = super.instantiateItem(container, position)
        if (item is Fragment) {
            holder.put(position, WeakReference(item))
        }
        return item
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        holder.remove(position)
        super.destroyItem(container, position, `object`)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return getPagerItem(position).title
    }

    override fun getPageWidth(position: Int): Float {
        return super.getPageWidth(position)
    }

    fun getPage(position: Int): Fragment? {
        val weakRefItem = holder[position]
        return weakRefItem?.get()
    }

    protected fun getPagerItem(position: Int): FragmentPagerItem {
        return pages[position]
    }

    fun getPages(): FragmentPagerItems {
        return pages
    }
}