package org.cxct.sportlottery.ui.main.next

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_main_category.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.MainViewModel

class MainCategoryFragment : BaseFragment<MainViewModel>(MainViewModel::class) {

    private val tabTitleList by lazy {
        listOf(R.string.sport, R.string.cp, R.string.live, R.string.qp, R.string.dz, R.string.by)
    }
    private val tabIconList by lazy {
        listOf(R.drawable.ic_sport, R.drawable.ic_cp, R.drawable.ic_live, R.drawable.ic_qp, R.drawable.ic_dz, R.drawable.ic_by)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        initObserver()
//        view_pager.adapter = VPFragmentAdapter(tabTitleList, this)
        view_pager.adapter = VpFragmentPagerAdapter(tabTitleList, parentFragmentManager)

        tab_layout.clearOnTabSelectedListeners()
        view_pager.clearOnPageChangeListeners()
        tab_layout.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(view_pager) {})
        view_pager.addOnPageChangeListener(object : TabLayout.TabLayoutOnPageChangeListener(tab_layout) {})
/*
        tabTitleList.forEachIndexed { index, data ->
            tab_layout.addTab(createGameTab(data, tabIconList[index]))
        }
        */
/*
        // attaching tab mediator
        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.setCustomView(R.layout.tab_item_game)
            tab.customView?.apply {
                findViewById<TextView>(R.id.tv_title)?.text = getString(tabTitleList[position])
                findViewById<ImageView>(R.id.iv_icon)?.setImageResource(tabIconList[position])
            }
        }.attach()
        */
    }

    private fun initObserver() {
        viewModel.isMainCateVpSwipeable.observe(viewLifecycleOwner) {
//            view_pager.isUserInputEnabled = it
//            view_pager.requestDisallowInterceptTouchEvent(true)

        }
    }

    private fun createGameTab(title: Int, img: Int): TabLayout.Tab {
        val customTab = tab_layout.newTab().setCustomView(R.layout.tab_item_game)
        customTab.tag = title
        try {
            customTab.customView?.apply {
                findViewById<TextView>(R.id.tv_title).text = getString(title)
                findViewById<ImageView>(R.id.iv_icon).setImageResource(img)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return customTab
    }
}

class VpFragmentPagerAdapter(private val titleList: List<Int>, fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return titleList.size
    }

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return SportFragment()
            1 -> return CPFragment()
            2 -> return LiveFragment()
            3 -> return QPFragment()
            4 -> return DZFragment()
            5 -> return BYFragment()
        }
        return SportFragment()
    }
}
