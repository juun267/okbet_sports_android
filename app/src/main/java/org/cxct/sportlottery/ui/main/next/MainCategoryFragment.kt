package org.cxct.sportlottery.ui.main.next

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
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
        view_pager.adapter = VPFragmentAdapter(tabTitleList, this)


        view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e(">>>", "main pos = $position")
                viewModel.setSwipeable(view_pager.currentItem != 4)
            }
        })

        // attaching tab mediator
        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.setCustomView(R.layout.tab_item_game)
            tab.customView?.apply {
                findViewById<TextView>(R.id.tv_title)?.text = getString(tabTitleList[position])
                findViewById<ImageView>(R.id.iv_icon)?.setImageResource(tabIconList[position])
            }
        }.attach()
    }

    private fun initObserver() {
        viewModel.isMainCateVpSwipeable.observe(viewLifecycleOwner) {
            view_pager.isUserInputEnabled = it
//            view_pager.requestDisallowInterceptTouchEvent(true)

        }
    }

}
