package org.cxct.sportlottery.ui.maintab.slot

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_home_slot.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.extentions.fitsSystemStatus
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.maintab.HomeFragment
import org.cxct.sportlottery.ui.maintab.HomeTabAdapter
import org.cxct.sportlottery.ui.maintab.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.*
import org.greenrobot.eventbus.EventBus

/**
 * 首页棋牌
 */
class HomeSlotFragment :
    BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    companion object {
        fun newInstance(): HomeSlotFragment {
            val args = Bundle()
            val fragment = HomeSlotFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val homeTabAdapter by lazy {
        HomeTabAdapter(HomeTabAdapter.getItems(), 5).apply {
            setOnItemClickListener { adapter, view, position ->
                (parentFragment as HomeFragment).onTabClickByPosition(position)
            }
        }
    }
    private val homeSlotAdapter by lazy {
        HomeSlotAdapter(mutableListOf())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_home_slot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.fitsSystemStatus()
        viewModel.getConfigData()
        initView()
        initObservable()
        viewModel.getGameEntryConfig(2, 1)
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getGameEntryConfig(2, 1)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun initView() {
        initToolBar()
        initTabView()
        initListView()
    }

    fun initToolBar() {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        iv_menu_left.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(true))
            (activity as MainTabActivity).showLeftFrament(0, 5)
        }
        iv_logo.setOnClickListener {
            (activity as MainTabActivity).jumpToHome(0)
        }
        btn_register.setOnClickListener {
            startActivity(Intent(requireActivity(), RegisterOkActivity::class.java))
        }
        btn_login.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
        iv_money_refresh.setOnClickListener {
            iv_money_refresh.startAnimation(RotateAnimation(0f,
                720f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f).apply {
                duration = 1000
            })
            viewModel.getMoney()
        }
//        lin_search.setOnClickListener {
//            startActivity(Intent(requireActivity(), SportSearchtActivity::class.java))
//        }
        setupLogin()
    }

    private fun initObservable() {
        if (viewModel == null) {
            return
        }
        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let {
                tv_home_money.text = "${sConfigData?.systemCurrencySign} ${TextUtil.format(it)}"
            }
        }
        viewModel.isLogin.observe(viewLifecycleOwner) {
            setupLogin()
        }
        viewModel.homeGameData.observe(viewLifecycleOwner) {
            it?.let {
                homeSlotAdapter.setNewData(it)
                LogUtil.d("HomeSlotFragment")
            }
        }
        viewModel.enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisible)
                enterThirdGame(it)
        }
    }

    private fun initTabView() {
        with(rv_tab_home) {
            if (layoutManager == null) {
                layoutManager =
                    ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            if (adapter == null) {
                adapter = homeTabAdapter
            }
            post {
                (layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(this,
                    RecyclerView.State(),
                    homeTabAdapter.selectPos)
            }
        }
    }

    private fun initListView() {
        with(rv_slot) {
            if (layoutManager == null) {
                layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            }
            if (adapter == null) {
                adapter = homeSlotAdapter
                homeSlotAdapter.setOnItemClickListener { adapter, view, position ->
                    if (viewModel.isLogin.value != true) {
                        (activity as MainTabActivity).showLoginNotify()
                    } else {
                        viewModel.requestEnterThirdGame(homeSlotAdapter.data[position])
                    }
                }
            }
        }
    }

    private fun setupLogin() {
        viewModel.isLogin.value?.let {
            btn_register.isVisible = !it
            btn_login.isVisible = !it
            ll_user_money.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun enterThirdGame(result: EnterThirdGameResult) {
        hideLoading()
        when (result.resultType) {
            EnterThirdGameResult.ResultType.SUCCESS -> context?.run {
                JumpUtil.toThirdGameWeb(
                    this,
                    result.url ?: "",
                    thirdGameCategoryCode = result.thirdGameCategoryCode
                )
            }
            EnterThirdGameResult.ResultType.FAIL -> showErrorPromptDialog(
                getString(R.string.prompt),
                result.errorMsg ?: ""
            ) {}
            EnterThirdGameResult.ResultType.NEED_REGISTER -> context?.startActivity(
                Intent(
                    context,
                    if (isOKPlat()) RegisterOkActivity::class.java else RegisterActivity::class.java)
            )
            EnterThirdGameResult.ResultType.GUEST -> showErrorPromptDialog(
                getString(R.string.error),
                result.errorMsg ?: ""
            ) {}
            EnterThirdGameResult.ResultType.NONE -> {
            }
        }
        if (result.resultType != EnterThirdGameResult.ResultType.NONE)
            viewModel.clearThirdGame()
    }
}
