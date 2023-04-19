package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home_elec.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.fitsSystemStatus
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.FragmentOkgamesBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameLabel
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameTab
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.transform.TransformInDialog

// okgames主Fragment
class OKGamesFragment : BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private val partPageSize = 12
    private lateinit var binding: FragmentOkgamesBinding
//    private lateinit var refreshHelper: RefreshHelper
    private val fragmentHelper by lazy {
        FragmentHelper(
            childFragmentManager, R.id.fragmentContainer, arrayOf(
                Pair(AllGamesFragment::class.java, null),
                Pair(PartGamesFragment::class.java, null)
            )
        )
    }

    private inline fun mainTabActivity() = activity as MainTabActivity
    private inline fun getCurrentTab() = binding.topView.getCurrentTab()
    private inline fun isShowAll() = fragmentHelper.getCurrentFragment() is AllGamesFragment
    private inline fun isShowSearch(): Boolean {
        val fragment = fragmentHelper.getCurrentFragment()
        return fragment is PartGamesFragment && fragment.isShowSearch()
    }

    private var searchKey = ""

    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOkgamesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onBindView(view: View) {
        initToolBar()
        initTopView()
        initRefreshLayout()
        showGameAll()
        initObservable()
        viewModel.getOKGamesHall()
    }

    private fun initRefreshLayout() {
//        refreshHelper = RefreshHelper.of(binding.scrollView, viewLifecycleOwner, false, true)
//        refreshHelper.setRefreshListener {  }
//        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
//            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
//
//            }
//
//        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getOKGamesHall()
        }
    }

    private fun initToolBar() = binding.homeToolbar.run {
        attach(this@OKGamesFragment, mainTabActivity(), viewModel)
        fitsSystemStatus()
        ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            mainTabActivity().showLeftFrament(0, 5)
        }
    }
    private fun initObservable() = viewModel.run {
        gameHall.observe(viewLifecycleOwner) {
            binding.topView.setTabsData(it?.categoryList?.toMutableList())
        }

        gamesList.observe(viewLifecycleOwner) {
            if (it.first) { //搜索结果
                if (it.second == searchKey && isShowSearch()) {
                    showPartGameList(it.third)
                }
                return@observe
            }

            val currentFragment = fragmentHelper.getCurrentFragment()
            if (currentFragment is PartGamesFragment) {
                if (currentFragment.crrentTabId() == it.second) {
                    showPartGameList(it.third)
                }
            }
        }

        totalRewardAmount.observe(viewLifecycleOwner) {
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

        enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisible)
                enterThirdGame(it.second, it.first)
        }

        gameBalanceResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { event ->
                TransformInDialog(event.first, event.second, event.third) {
                    enterThirdGame(it, event.first)
                }.show(childFragmentManager, null)
            }
        }
    }


    private fun initTopView() = binding.topView.run {
        onTableClick = ::onTabChange
        onSearchTextChanged = {
            searchKey = it
            hideKeyboard()
            if (searchKey.isEmptyStr()) {
                showPartGameList(null)
            } else {
                changePartGamesLabel(GameTab.TAB_SEARCH)
                viewModel.searchGames(searchKey, 1, partPageSize)
            }
        }
    }

    private fun onTabChange(tab: OKGameTab) {
        if (tab.isAll()) {
            showGameAll()
            return
        }

        reloadPartGames(tab)
    }

    private fun showGameAll(): AllGamesFragment {
//        refreshHelper.reset()
        return fragmentHelper.showFragment(0) as AllGamesFragment
    }

    private inline fun showPartGameFragment(): PartGamesFragment {
//        refreshHelper.reset()
        return fragmentHelper.showFragment(1) as PartGamesFragment
    }

    fun backGameAll() {
        binding.topView.backAll()
    }

    fun changeGameTable(tab: OKGameTab) {
        binding.topView.changeSelectedGameTab(tab)
    }

    fun changePartGames(okgamesFirm: OKGamesFirm) {
        changePartGamesLabel(okgamesFirm)
        val firmId = okgamesFirm.getKey().toString()
        viewModel.getOKGamesList(firmId, null, firmId, 1, partPageSize)
    }

    private fun reloadPartGames(tab: OKGameTab) {
        changePartGamesLabel(tab)
        val categoryId = tab.getKey().toString()
        viewModel.getOKGamesList(categoryId, categoryId, null, 1, partPageSize)
    }

    private fun changePartGamesLabel(tab: OKGameLabel) {
        showPartGameFragment().changeLabel(tab)
    }

    private fun showPartGameList(gameList: List<OKGameBean>?) {
        showPartGameFragment().showSearchResault(gameList)
//        refreshHelper.setNoMoreData((gameList?.size ?: 0) < partPageSize)
    }

}