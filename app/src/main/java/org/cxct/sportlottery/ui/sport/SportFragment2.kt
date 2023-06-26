package org.cxct.sportlottery.ui.sport

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.newInstanceFragment
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.FragmentSport2Binding
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.sport.endscore.EndScoreFragment
import org.cxct.sportlottery.ui.sport.favorite.FavoriteFragment2
import org.cxct.sportlottery.ui.sport.list.SportListFragment2
import org.cxct.sportlottery.ui.sport.list.adapter.SportFooterGamesView
import org.cxct.sportlottery.ui.sport.outright.SportOutrightFragment
import org.cxct.sportlottery.ui.sport.search.SportSearchtActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.FragmentHelper2
import org.cxct.sportlottery.util.phoneNumCheckDialog
import org.cxct.sportlottery.view.dialog.PopImageDialog
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SportFragment2: BindingSocketFragment<SportTabViewModel, FragmentSport2Binding>() {

    private val matchTypeTab = listOf(
        MatchType.END_SCORE,
        MatchType.IN_PLAY,
        MatchType.AT_START,
        MatchType.TODAY,
        MatchType.EARLY,
        MatchType.PARLAY,
//        MatchType.CS,
        MatchType.OUTRIGHT,
        MatchType.MY_EVENT
    )
    private val favoriteIndex = matchTypeTab.indexOf(MatchType.MY_EVENT)
    private inline fun getMainTabActivity() = activity as MainTabActivity
    private val fragmentHelper by lazy { FragmentHelper2(childFragmentManager, R.id.fl_content) }
    private val footView by lazy { SportFooterGamesView(binding.root.context) }
    private val mianViewModel: OKGamesViewModel by sharedViewModel()

    private var jumpMatchType: MatchType? = null
    private var jumpGameType: GameType? = null
    //根据赛事数量判断默认的分类
    private var defaultMatchType: MatchType? = null
    private var favoriteItems = listOf<Item>()
    private inline fun favoriteCount(items: List<Item>): Int {
        return items.sumOf { it.leagueOddsList.sumOf { it.matchOdds.size } }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        fragmentHelper.currentFragment()?.let {
            if (it.isAdded)
                it.onHiddenChanged(hidden)
        }
    }

    override fun onInitView(view: View) {
        initToolBar()
        initTabLayout()
        showSportDialog()
    }

    override fun onBindViewStatus(view: View) {
        initObserve()
        footView.setUp(this, mianViewModel)
        viewModel.getMatchData()
        jumpMatchType?.let {
            navGameFragment(it)
        }

        viewModel.loadFavoriteGameList()
    }

    fun initToolBar() = binding.homeToolbar.run {
        background = null
        attach(this@SportFragment2, getMainTabActivity(), viewModel, false)
        searchIcon.setOnClickListener { startActivity(SportSearchtActivity::class.java) }
        ivMenuLeft.setOnClickListener {
            getMainTabActivity().showSportLeftMenu()
            EventBusUtil.post(MenuEvent(true))
        }
    }

    private fun initTabLayout() = binding.tabLayout.run {
        addOnTabSelectedListener(TabSelectedAdapter{ tab, _ ->selectTab(tab.position) })
        OverScrollDecoratorHelper.setUpOverScroll(this)
    }

    private fun refreshTabLayout(sportMenuResult: ApiResult<SportMenuData>) {

        val sportMenuData = sportMenuResult.getData()
        val countInPlay = sportMenuData?.menu?.inPlay?.items?.sumOf { it.num } ?: 0
        val countAtStart = sportMenuData?.atStart?.items?.sumOf { it.num } ?: 0
        val countToday = sportMenuData?.menu?.today?.items?.sumOf { it.num } ?: 0
        val countEarly = sportMenuData?.menu?.early?.items?.sumOf { it.num } ?: 0
//        val countCS = sportMenuData?.menu?.cs?.items?.sumOf { it.num } ?: 0
        val countOutright = sportMenuData?.menu?.outright?.items?.sumOf { it.num } ?: 0
        val countParlay = sportMenuData?.menu?.parlay?.items?.sumOf { it.num } ?: 0
        val countBkEnd = sportMenuData?.menu?.bkEnd?.items?.sumOf { it.num } ?: 0
        defaultMatchType = when {
            countInPlay > 0 -> MatchType.IN_PLAY
            countAtStart > 0 -> MatchType.AT_START
            countToday > 0 -> MatchType.TODAY
            else -> MatchType.EARLY
        }
        addTab(getString(R.string.home_tab_end_score), countBkEnd, 0)
        addTab(getString(R.string.home_tab_in_play), countInPlay, 1)
        addTab(getString(R.string.home_tab_at_start), countAtStart, 2)
        addTab(getString(R.string.home_tab_today), countToday, 3)
        addTab(getString(R.string.home_tab_early), countEarly, 4)
        addTab(getString(R.string.home_tab_parlay), countParlay, 5)
//        addTab(getString(R.string.home_tab_cs), countCS, 6)
        addTab(getString(R.string.home_tab_outright), countOutright, 6)
        val tabView = addTab(getString(R.string.my_favorite), favoriteCount(favoriteItems), 7)
        if (!LoginRepository.isLogined()) {
            tabView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    startActivity(LoginOKActivity::class.java)
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            }
        }
    }

    private fun addTab(name: String, num: Int, position: Int): View = binding.tabLayout.run {

        val tab = if (tabCount > position) {
            getTabAt(position)!!
        } else {
            newTab().setCustomView(R.layout.home_cate_tab).apply {
                addTab(this, position, false)
            }
        }

        tab.customView?.run {
            tv_title.text = name
            tv_number.text = num.toString()
        }

        return@run tab.customView!!
    }

    private var currentMatchType: MatchType? = null

    private fun selectTab(position: Int) {
        var matchType =  matchTypeTab[position] ?: return
        currentMatchType = matchType
        navGameFragment(matchType)
    }

    private var lastMatchType: MatchType? = null
    private var lastGameType: String? = null
    private fun navGameFragment(matchType: MatchType) {
        var gameType = jumpGameType?.key
        if (lastMatchType == matchType && lastGameType == gameType) {
            return
        }

        lastMatchType = matchType
        lastGameType = gameType

        val args = Bundle()
        args.putSerializable("matchType", matchType)
        args.putString("gameType", gameType)

        when (matchType) {
            MatchType.OUTRIGHT -> {
                fragmentHelper.show(SportOutrightFragment::class.java, args) { fragment, newInstance ->
                    fragment.offsetScrollListener = ::setTabElevation
                    fragment.resetFooterView(footView)
                }
            }

            MatchType.END_SCORE -> {
                fragmentHelper.show(EndScoreFragment::class.java, args) { fragment, newInstance ->
                    fragment.offsetScrollListener = ::setTabElevation
                    fragment.resetFooterView(footView)
                }
            }

            MatchType.MY_EVENT -> {
                fragmentHelper.show(FavoriteFragment2::class.java, args) { fragment, newInstance ->
                    fragment.offsetScrollListener = ::setTabElevation
                    fragment.resetFooterView(footView)
                    viewModel.loadFavoriteGameList()
                }
            }

            else -> {
                fragmentHelper.show(SportListFragment2::class.java, args) { fragment, newInstance ->
                    fragment.offsetScrollListener = ::setTabElevation
                    fragment.resetFooterView(footView)
                    if (!newInstance && fragment.isAdded) {
                        fragment.reload()
                    }
                }
            }
        }

    }

    private val maxElevation = 5.dp
    private fun setTabElevation(elevation: Double) = binding.run {
//        val elevation = (elevation * elevation * maxElevation).toFloat()
    }

    fun setJumpSport(matchType: MatchType? = null, gameType: GameType? = null) {
        jumpMatchType = matchType
        jumpGameType = gameType
        if (isAdded) {
            //如果体育当前已经在指定的matchType页面时，跳过检查重复选中的机制，强制筛选sportListFragment
            jumpMatchType = jumpMatchType ?: defaultMatchType
            binding.tabLayout.getTabAt(matchTypeTab.indexOfFirst { it == matchType })?.select()
        }
    }

    private fun initObserve() = viewModel.run {

        //使用者沒有電話號碼
        showPhoneNumberMessageDialog.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (!b) phoneNumCheckDialog(requireContext(), childFragmentManager)
            }
        }

        sportMenuResult.observe(viewLifecycleOwner) {
            hideLoading()
            updateUiWithResult(it)
        }

        sportTypeMenuData.observe(viewLifecycleOwner) { updateFavoriteItem(it.first) }
        favorMatchList.observe(viewLifecycleOwner) {
            viewModel.loadFavoriteGameList()
        }
    }

    private fun updateFavoriteItem(favoriteLeagues: List<Item>) {

        val favoriteTab = binding.tabLayout.getTabAt(favoriteIndex)
        if (favoriteTab == null) {
            favoriteItems = favoriteLeagues
            return
        }

        favoriteTab.customView?.tv_number?.text = favoriteCount(favoriteLeagues).toString()
        val currentFragment = fragmentHelper.currentFragment()
        if (currentFragment is FavoriteFragment2) {
            currentFragment.setFavoriteData(favoriteLeagues)
        }
        favoriteItems = favoriteLeagues
    }

    private fun updateUiWithResult(sportMenuResult: ApiResult<SportMenuData>) {
        if (!sportMenuResult.succeeded()) {
            return
        }

        val isFirstSwitch = defaultMatchType == null
        refreshTabLayout(sportMenuResult)
        EventBusUtil.post(sportMenuResult)
        if (!isFirstSwitch) {
            return
        }

        val matchType = jumpMatchType ?: defaultMatchType
        if (matchType != null) {
            binding.tabLayout.getTabAt(matchTypeTab.indexOfFirst { it == matchType })?.select()
            return
        }

    }

    fun updateSportMenuResult(sportMenuResult: ApiResult<SportMenuData>) {
        viewModel.setSportMenuResult(sportMenuResult)
    }

    private fun showSportDialog(){
        if (PopImageDialog.showSportDialog) {
            PopImageDialog.showSportDialog = false
            if (PopImageDialog.checkImageTypeAvailable(ImageType.DIALOG_SPORT.code)) {
                requireContext().newInstanceFragment<PopImageDialog>(Bundle().apply {
                    putInt(PopImageDialog.IMAGE_TYPE, ImageType.DIALOG_SPORT.code)
                }).show(childFragmentManager, PopImageDialog::class.simpleName)
            }
        }
    }

}