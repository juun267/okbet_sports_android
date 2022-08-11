package org.cxct.sportlottery.ui.maintab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.view_toolbar_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.greenrobot.eventbus.EventBus

class MainHomeFragment : BaseFragment<BaseViewModel>(BaseViewModel::class) {
    companion object {
        fun newInstance(): MainHomeFragment {
            val args = Bundle()
            val fragment = MainHomeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        tv_balance_currency.text = sConfigData?.systemCurrencySign
//        viewModel.userMoney.observe(this.viewLifecycleOwner, Observer {
//            hideLoading()
//            it?.apply {
//                tv_balance.text = TextUtil.format(it)
//            }
//        })
        iv_menu_left.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(true))
        }
    }
}