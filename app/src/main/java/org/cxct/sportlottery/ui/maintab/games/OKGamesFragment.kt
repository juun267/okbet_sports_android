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

    private lateinit var binding: FragmentOkgamesBinding
    private val fragmentHelper by lazy {
        FragmentHelper(childFragmentManager, R.id.fragmentContainer, arrayOf(
                Pair(AllGamesFragment::class.java, null),
                Pair(PartGamesFragment::class.java, null)
            )
        )
    }

    private inline fun mainTabActivity() = activity as MainTabActivity

    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentOkgamesBinding.inflate(layoutInflater).apply { binding = this }.root
    }

    override fun onBindView(view: View) {
        initToolBar()
        initTopView()
        showGameAll()
        initObservable()
        viewModel.getOKGamesHall()
    }

    private var requestTag: Any = Any()
    private var requestBlock: ((Int) -> Unit)? = null
    private fun retagRequest(): Any {
        return Any().apply { requestTag = this }
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
            if (it.first == requestTag) {
                showPartGameList(it.third, it.second)
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
        onSearchTextChanged = { searchKey ->
            hideKeyboard()
            if (searchKey.isEmptyStr()) {
                showPartGameList(null, 0)
            } else {
                changePartGamesLabel(GameTab.TAB_SEARCH)
                startLoad{ viewModel.searchGames(retagRequest(), searchKey, it, PartGamesFragment.pageSize) }
            }
        }
    }

    private fun onTabChange(tab: OKGameTab) {
        if (tab.isAll()) {  // 全部
            showGameAll()
            return
        }

        if (tab.isRecent()) { // 最近
            showRecentPart(tab)
            return
        }

        reloadPartGames(tab)
    }

    private fun showGameAll(): AllGamesFragment {
        requestBlock = null
        retagRequest()
        return fragmentHelper.showFragment(0) as AllGamesFragment
    }

    private inline fun showPartGameFragment(): PartGamesFragment {
        return fragmentHelper.showFragment(1) as PartGamesFragment
    }

    private fun showRecentPart(tab: OKGameTab) {
        retagRequest()
        changePartGamesLabel(tab)
        showPartGameList(viewModel.recentPlay.value, 0)
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
        startLoad{ viewModel.getOKGamesList(retagRequest(), null, firmId, it, PartGamesFragment.pageSize) }
    }

    private fun reloadPartGames(tab: OKGameTab) {
        changePartGamesLabel(tab)
        val categoryId = tab.getKey().toString()
        startLoad{ viewModel.getOKGamesList(retagRequest(), categoryId, null, it, PartGamesFragment.pageSize) }
    }

    private fun startLoad(request: (Int) -> Unit) {
        requestBlock = request
        request.invoke(1)
    }

    private fun changePartGamesLabel(tab: OKGameLabel) {
        showPartGameFragment().changeLabel(tab)
    }

    private fun showPartGameList(gameList: List<OKGameBean>?, total: Int) {
        showPartGameFragment().showSearchResault(gameList, total)
    }

    fun loadNextPage(pageIndex: Int): Boolean {
        if (requestBlock == null) {
            return false
        }
        requestBlock!!.invoke(pageIndex)
        return true
    }

}