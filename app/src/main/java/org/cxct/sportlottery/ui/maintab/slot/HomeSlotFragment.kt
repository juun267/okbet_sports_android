package org.cxct.sportlottery.ui.maintab.slot

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_home_slot.*
import kotlinx.android.synthetic.main.fragment_home_slot.homeToolbar
import kotlinx.android.synthetic.main.fragment_home_slot.lin_empty_game
import kotlinx.android.synthetic.main.fragment_home_slot.rv_tab_home
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.extentions.fitsSystemStatus
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.common.transform.TransformInDialog
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

    override fun layoutId() = R.layout.fragment_home_slot

    private val homeTabAdapter by lazy {
        HomeTabAdapter(HomeTabAdapter.getItems(),
            requireArguments().getInt("position"),
            (parentFragment as HomeFragment))
    }

    private val homeSlotAdapter by lazy { HomeSlotAdapter() }

    override fun onBindView(view: View) {
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
        homeToolbar.attach(this, (activity as MainTabActivity), viewModel)
        homeToolbar.ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            (activity as MainTabActivity).showLeftFrament(0, 5)
        }
    }

    private fun initObservable() {

        viewModel.slotGameData.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                lin_empty_game.isVisible = true
                rv_slot.isVisible = false
                return@observe
            }
            lin_empty_game.isVisible = false
            rv_slot.isVisible = true

            if (!isCreditSystem()) {
                homeSlotAdapter.setNewInstance(it.toMutableList())
                return@observe
            }

            val list = mutableListOf<QueryGameEntryData>()
            it.forEach { item ->
                if ("CGQP" == item.firmCode || "KY" == item.firmCode) {
                    list.add(item)
                }
            }

            if (list.isEmpty()) {
                lin_empty_game.isVisible = true
                rv_slot.isVisible = false
                return@observe
            }

            homeSlotAdapter.setNewInstance(list)
        }

        viewModel.enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisible)
                enterThirdGame(it)
        }

        viewModel.gameBalanceResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                //            if (it.second < 0) {
//                return@observe
//            }

                TransformInDialog(it.first, it.second) {
                    viewModel.requestEnterThirdGame("${it.firmType}", "${it.gameCode}", "${it.gameCategory}")
                }.show(childFragmentManager, null)
            }
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
                        viewModel.requestEnterThirdGame(homeSlotAdapter.data[position], this@HomeSlotFragment)
                    }
                }
            }
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
            EnterThirdGameResult.ResultType.NEED_REGISTER -> requireActivity().startRegister()
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
