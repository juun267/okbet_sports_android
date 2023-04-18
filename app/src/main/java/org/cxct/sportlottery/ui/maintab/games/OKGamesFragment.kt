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
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.enterThirdGame
import org.cxct.sportlottery.view.transform.TransformInDialog

// okgames主Fragment
class OKGamesFragment : BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentOkgamesBinding

    private val fragmentHelper by lazy {
        FragmentHelper(
            childFragmentManager, R.id.fragmentContainer, arrayOf(
                Pair(AllGamesFragment::class.java, null),
                Pair(PartGamesFragment::class.java, null)
            )
        )
    }

    private inline fun mainTabActivity() = activity as MainTabActivity
    private inline fun getCurrentTab() = binding.topView.currentTab
    private inline fun isShowAll() = fragmentHelper.getCurrentFragment() is AllGamesFragment

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
        initObservable()
        showGameAll()
        initObserver()
        viewModel.getOKGamesHall()


    }

    private fun initObserver() = viewModel.run {
        searchResult.observe(viewLifecycleOwner) {
            if (!getCurrentTab().isSearch() || it.first != searchKey) {
                return@observe
            }
            showSearchResult(it.second)
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

    private fun initTopView() = binding.topView.run {
        onTableClick = ::onTabChange
        onSearchTextChanged = {
            searchKey = it
            if (searchKey.isEmptyStr()) {
                showSearchResult(null)
            } else {
                viewModel.searchGames(searchKey)
            }
        }
    }

    private fun onTabChange(tab: GameTab) {
        if (isShowAll()) {
            showPartGames(tab)
            return
        }

        if (tab.isAll()) {
            showGameAll()
        }
    }

    private fun initObservable() {
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
        viewModel.enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisible)
                enterThirdGame(it.second, it.first)
        }

        viewModel.gameBalanceResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { event ->
                TransformInDialog(event.first, event.second, event.third) {
                    enterThirdGame(it, event.first)
                }.show(childFragmentManager, null)
            }
        }
    }

    /**
     * 显示赛事结果页面，三种场景下的结果显示页面
     */
    open fun showGameResult(
        tagName: String?,
        gameName: String? = null,
        categoryId: String? = null,
        firmId: String? = null,
    ) {
        (fragmentHelper.getFragment(1) as PartGamesFragment).setData(tagName,
            gameName,
            categoryId,
            firmId)
        fragmentHelper.showFragment(1)
    }

    open fun showGameAll() {
        hideKeyboard()
        fragmentHelper.showFragment(0)
    }

    private fun showPartGames(tab: GameTab) {
        (fragmentHelper.showFragment(1) as PartGamesFragment).changeTab()
    }

    private fun showSearchResult(gameList: List<OKGameBean>?) {
        (fragmentHelper.getFragment(1) as PartGamesFragment).showSearchResault(gameList)
    }

}