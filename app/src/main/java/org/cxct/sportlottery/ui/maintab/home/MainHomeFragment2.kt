package org.cxct.sportlottery.ui.maintab.home

import android.view.View
import kotlinx.android.synthetic.main.fragment_main_home.*
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.fitsSystemStatus
import org.cxct.sportlottery.databinding.FragmentMainHome2Binding
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.setupBackTop

class MainHomeFragment2: BindingSocketFragment<MainHomeViewModel, FragmentMainHome2Binding>() {

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private inline fun getHomeFragment() = parentFragment as HomeFragment

    override fun onInitView(view: View) = binding.run {
        scrollView.setupBackTop(ivBackTop, 180.dp)

    }

    override fun onBindViewStatus(view: View) {
        initToolBar()
    }

    override fun onInitData() {

    }

    fun initToolBar() = binding.run {
        homeToolbar.attach(this@MainHomeFragment2, getMainTabActivity(), viewModel)
        homeToolbar.fitsSystemStatus()
        homeToolbar.ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            getMainTabActivity().showLeftFrament(0, 0)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        homeToolbar.onRefreshMoney()
    }


}