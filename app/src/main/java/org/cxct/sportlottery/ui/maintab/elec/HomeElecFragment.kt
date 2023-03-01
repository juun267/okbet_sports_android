package org.cxct.sportlottery.ui.maintab.elec

import android.graphics.Color
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_home_elec.*
import kotlinx.android.synthetic.main.fragment_home_elec.homeToolbar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.extentions.fitsSystemStatus
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.common.transform.TransformInDialog
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.maintab.HomeFragment
import org.cxct.sportlottery.ui.maintab.HomeTabAdapter
import org.cxct.sportlottery.ui.maintab.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp

/**
 * 首页电子
 */
class HomeElecFragment: BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    override fun layoutId() = R.layout.fragment_home_elec

    private val homeTabAdapter by lazy {
        HomeTabAdapter(HomeTabAdapter.getItems(), 4, (parentFragment as HomeFragment))
    }
    private val homeElecAdapter by lazy {
        HomeElecAdapter(mutableListOf())
    }

    override fun onBindView(view: View) {
        view.fitsSystemStatus()
        initView()
        initObservable()
        viewModel.getTotalRewardAmount()
        viewModel.getGameEntryConfig(2, 2)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getGameEntryConfig(2, 2)
            viewModel.getTotalRewardAmount()
            viewModel.getRecommend()
        }
    }

    private fun initView() {
        initToolBar()
        initTabView()
        initListView()
        iv_elec_title.setImageResource(
            when (LanguageManager.getSelectLanguage(context)) {
                LanguageManager.Language.ZH -> R.drawable.ic_elec_title
                LanguageManager.Language.VI -> R.drawable.ic_elec_title_vn
                LanguageManager.Language.EN -> R.drawable.ic_elec_title_en
                else -> R.drawable.ic_elec_title_en
            }
        )
    }

    fun initToolBar() {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        homeToolbar.attach(this, (activity as MainTabActivity), viewModel)
        homeToolbar.ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            (activity as MainTabActivity).showLeftFrament(0, 4)
        }
    }

    private fun initObservable() {
        if (viewModel == null) {
            return
        }

        viewModel.totalRewardAmount.observe(viewLifecycleOwner) {
            it.getOrNull(0)?.let {
                tv_first_game_name.text = it.name
                tv_first_amount.text =
                    "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(it.amount, 2)}"
            }
            it.getOrNull(1)?.let {
                tv_second_game_name.text = it.name
                tv_second_amount.text =
                    "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(it.amount, 2)}"
            }
            it.getOrNull(2)?.let {
                tv_third_game_name.text = it.name
                tv_third_amount.text =
                    "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(it.amount, 2)}"
            }
        }

        viewModel.slotGameData.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                lin_empty_game.isVisible = true
                rv_elec.isVisible = false
            } else {
                lin_empty_game.isVisible = false
                rv_elec.isVisible = true
                homeElecAdapter.setNewData(it?.toMutableList())
            }
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
        with(rv_elec) {
            if (layoutManager == null) {
                layoutManager =
                    GridLayoutManager(requireContext(), 3)
            }
            if (adapter == null) {
                adapter = homeElecAdapter
                homeElecAdapter.setOnItemClickListener { adapter, view, position ->
                    if (viewModel.isLogin.value != true) {
                        (activity as MainTabActivity).showLoginNotify()
                    } else {
                        viewModel.requestEnterThirdGame(homeElecAdapter.data[position], this@HomeElecFragment)
                    }
                }
            }
            if (itemDecorationCount == 0) {
                addItemDecoration(GridItemDecoration(12.dp, 12.dp, Color.TRANSPARENT, false))
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
