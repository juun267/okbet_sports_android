package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.fitsSystemStatus
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.FragmentOkgamesBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameLabel
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameTab
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.transform.TransformInDialog

// okgames主Fragment
class OKGamesFragment : BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    val gameItemViewPool by lazy {
        RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(0, 20) }
    }

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

    private fun onTabChange(tab: OKGameTab): Boolean {
        when {

            tab.isAll() -> {  // 全部
                showGameAll()
                return true
            }

            tab.isRecent() -> { // 最近
                return loginedRun(binding.root.context) { showRecentPart(tab) }
            }

            tab.isFavorites() -> { // 收藏
                return loginedRun(binding.root.context) { showFavorites(tab) }
            }

            else -> {
                reloadPartGames(tab)
                return true
            }
        }
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

    private fun showFavorites(tab: OKGameTab) {
        retagRequest()
        changePartGamesLabel(tab)
        showPartGameList(viewModel.collectList.value, 0)
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

    fun collectGame(gameData: OKGameBean) {
        loginedRun(binding.root.context) { viewModel.collectGame(gameData) }
    }

}