package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.hideSoftKeyboard
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.FragmentOkgamesBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameLabel
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameTab
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.dialog.PopImageDialog

// okgames主Fragment
class OKLiveFragment : BaseSocketFragment<OKLiveViewModel,FragmentOkgamesBinding>() {

    val gameItemViewPool by lazy {
        RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(0, 20) }
    }

    private lateinit var fragmentHelper: FragmentHelper

    private fun isAllTba() = fragmentHelper.getCurrentFragment() is AllLiveFragment

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) { //不可见时切回AllGames
            if (!isAllTba()) {
                backGameAll()
            }
            backTop()
        }
        fragmentHelper.getCurrentFragment().onHiddenChanged(hidden)
    }

    private fun backTop() {
        val behavior = (binding.appBar.layoutParams as CoordinatorLayout.LayoutParams).behavior
        if (behavior is AppBarLayout.Behavior) {
            val topAndBottomOffset = behavior.topAndBottomOffset
            if (topAndBottomOffset != 0) {
                behavior.topAndBottomOffset = 0
            }
        }
    }

     inline fun mainTabActivity() = activity as MainTabActivity

    override fun onInitView(view: View) {
        fragmentHelper = FragmentHelper(childFragmentManager, R.id.fragmentContainer, arrayOf(
            Param(AllLiveFragment::class.java), Param(PartLiveFragment::class.java)
        ))
        initToolBar()
        initTopView()
        showGameAll()
        initObservable()
        viewModel.getOKLiveHall()
        PopImageDialog.showDialog(childFragmentManager,ImageType.DIALOG_OKLIVE.code)
    }
    private var requestTag: Any = Any()
    private var requestBlock: ((Int) -> Unit)? = null
    private fun retagRequest(): Any {
        return Any().apply { requestTag = this }
    }

    private fun initToolBar() = binding.homeToolbar.run {
        attach(this@OKLiveFragment, mainTabActivity(), viewModel)
        tvUserMoney.setOnClickListener {
            EventBusUtil.post(MenuEvent(true, Gravity.RIGHT))
            mainTabActivity().showMainRightMenu()
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

    }


    private fun initTopView() = binding.topView.run {
        setup(this@OKLiveFragment, 18, gameType = "oklive")
        setProviderSelect {
            backGameAll()
            showGameAll()
            changePartGames(it)
        }
        setProviderVisible(false)
        onTableClick = ::onTabChange
        onSearchTextChanged = { searchKey ->
            requireActivity().hideSoftKeyboard()
            if (!searchKey.isEmptyStr()) {
                changePartGamesLabel(GameTab.TAB_SEARCH, searchKey)
                startLoad {
                    viewModel.searchGames(
                        retagRequest(), searchKey, it, PartLiveFragment.pageSize
                    )
                }
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
                return loginedRun(binding.root.context) { loadFavorite(tab) }
            }

            else -> {
                reloadPartGames(tab)
                return true
            }
        }
    }

    private fun showGameAll(): AllLiveFragment {
        requestBlock = null
        retagRequest()
        return fragmentHelper.showFragment(0) as AllLiveFragment
    }

    private inline fun showPartGameFragment(): PartLiveFragment {
        return fragmentHelper.showFragment(1) as PartLiveFragment
    }

    private fun showRecentPart(tab: OKGameTab) {
        retagRequest()
        changePartGamesLabel(tab)
        showPartGameList(viewModel.recentPlay.value, 0)
    }

    private fun showFavorites(tab: OKGameTab) {
        retagRequest()
        changePartGamesLabel(tab)
        showPartGameList(viewModel.collectList.value?.second, 0)
    }

    fun enterGame(bean: OKGameBean) {
        mainTabActivity().enterThirdGame(bean)
        if (LoginRepository.isLogined()) {
            viewModel.addRecentPlay(bean)
        }
    }

    fun backGameAll() {
        binding.topView.backAll()
    }

    fun changeGameTable(tab: OKGameTab) {
        binding.topView.changeSelectedGameTab(tab)
    }

    fun changePartGames(okgamesFirm: OKGamesFirm) {
        changePartGamesLabel(okgamesFirm,okgamesFirm.firmName)
        val firmId = okgamesFirm.getKey().toString()
        startLoad {
            viewModel.getOKGamesList(
                retagRequest(), null, firmId, it, PartLiveFragment.pageSize
            )
        }
    }

    private fun loadFavorite(tab: OKGameTab) {
        changePartGamesLabel(tab)
        startLoad { viewModel.getFavoriteOKGames(retagRequest(), it, PartLiveFragment.pageSize) }
    }

    private fun reloadPartGames(tab: OKGameTab) {
        changePartGamesLabel(tab)
        val categoryId = tab.getKey().toString()
        startLoad {
            viewModel.getOKGamesList(
                retagRequest(), categoryId, null, it, PartLiveFragment.pageSize
            )
        }
    }

    private fun startLoad(request: (Int) -> Unit) {
        requestBlock = request
        request.invoke(1)
    }

    private fun changePartGamesLabel(tab: OKGameLabel, labelName: String? = null) {
        showPartGameFragment().changeLabel(tab, labelName)
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

    fun collectGame(gameData: OKGameBean): Boolean {
        return loginedRun(binding.root.context) { viewModel.collectGame(gameData) }
    }

    open fun getCurrentFragment() = fragmentHelper.getCurrentFragment()

    fun search(key: String){
        if (isAdded){
            binding.topView.binding.edtSearchGames.setText(key)
            changePartGamesLabel(GameTab.TAB_SEARCH, key)
            startLoad {
                viewModel.searchGames(
                    retagRequest(), key, it, PartGamesFragment.pageSize
                )
            }
        }
    }
    fun showByProvider(okgamesFirm: OKGamesFirm){
        if (isAdded){
            backGameAll()
            showGameAll()
            changePartGames(okgamesFirm)
        }
    }
    fun setupProvider(firmList:MutableList<OKGamesFirm>)=binding.topView.run{
        setProviderItems(firmList)
        setProviderVisible(firmList.isNotEmpty())
        setProviderArrowVisible(firmList.size > 3)
    }
}