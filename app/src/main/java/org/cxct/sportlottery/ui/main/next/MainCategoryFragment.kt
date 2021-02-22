package org.cxct.sportlottery.ui.main.next

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
        listOf(R.drawable.ic_football, R.drawable.ic_alipay, R.drawable.ic_football, R.drawable.ic_football, R.drawable.ic_football, R.drawable.ic_football)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view_pager.adapter = VPFragmentAdapter(tabTitleList, this)

        // attaching tab mediator
        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.setCustomView(R.layout.tab_game)
            tab.customView?.apply {
                findViewById<TextView>(R.id.tv_title)?.text = getString(tabTitleList[position])
                findViewById<ImageView>(R.id.iv_icon)?.setImageResource(tabIconList[position])
            }
        }.attach()
    }

}
