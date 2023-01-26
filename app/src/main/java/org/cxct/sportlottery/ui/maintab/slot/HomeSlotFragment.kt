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
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.maintab.HomeFragment
import org.cxct.sportlottery.ui.maintab.HomeTabAdapter
import org.cxct.sportlottery.ui.maintab.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.*

/**
 * 首页棋牌
 */
class HomeSlotFragment :
    BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    private val homeTabAdapter by lazy {
        HomeTabAdapter(HomeTabAdapter.getItems(),
            requireArguments().getInt("position"),
            (parentFragment as HomeFragment))
    }

    private val homeSlotAdapter by lazy { HomeSlotAdapter() }

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
        initView()
        initObservable()
        viewModel.getGameEntryConfig(2, 1)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getGameEntryConfig(2, 1)
        }
    }

    private fun initView() {
        initToolBar()
        initTabView()
        initListView()
    }

    fun initToolBar() {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        iv_menu_left.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            (activity as MainTabActivity).showLeftFrament(0, 5)
        }
        iv_logo.setOnClickListener {
            (activity as MainTabActivity).jumpToHome(0)
        }
        btn_register.setOnClickListener {
            startRegister(requireContext())
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
        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let {
                tv_home_money.text = "${sConfigData?.systemCurrencySign} ${TextUtil.format(it)}"
            }
        }
        viewModel.isLogin.observe(viewLifecycleOwner) {
            setupLogin()
        }
        viewModel.slotGameData.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                lin_empty_game.isVisible = true
                rv_slot.isVisible = false
            } else {
                lin_empty_game.isVisible = false
                rv_slot.isVisible = true
                homeSlotAdapter.setNewInstance(it.toMutableList())
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
            btn_register.isVisible = !it && !isUAT()
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
            EnterThirdGameResult.ResultType.NEED_REGISTER -> context?.run { startRegister(this) }
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
