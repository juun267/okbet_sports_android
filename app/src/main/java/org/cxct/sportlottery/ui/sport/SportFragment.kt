package org.cxct.sportlottery.ui.sport

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.toBinding
import org.cxct.sportlottery.databinding.FragmentSport2Binding
import org.cxct.sportlottery.databinding.HomeCateTabBinding
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.Menu
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.service.dispatcher.DataResourceChange
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.betRecord.BetRecordActivity
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.sport.endscore.EndScoreFragment
import org.cxct.sportlottery.ui.sport.favorite.FavoriteFragment2
import org.cxct.sportlottery.ui.sport.list.SportListFragment
import org.cxct.sportlottery.ui.sport.list.TodayMenuPop
import org.cxct.sportlottery.ui.sport.list.adapter.SportFooterGamesView
import org.cxct.sportlottery.ui.sport.outright.SportOutrightFragment
import org.cxct.sportlottery.ui.sport.search.SportSearchtActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.dialog.PopImageDialog
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.reflect.KClass

class SportFragment: BaseSocketFragment<SportTabViewModel, FragmentSport2Binding>() {

    override fun createVM(clazz: KClass<SportTabViewModel>) = getViewModel(clazz = clazz)

    private val matchTypeTab = mutableListOf(
        MatchType.END_SCORE,
        MatchType.IN_PLAY,
        MatchType.TODAY,
        MatchType.EARLY,
        MatchType.PARLAY,
        MatchType.OUTRIGHT,
        MatchType.MY_EVENT
    )
    private val todayMatchPosition = 2
    private val matchTypeTodayTab = mutableListOf(
        MatchType.TODAY,
        MatchType.AT_START,
        MatchType.IN12HR,
        MatchType.IN24HR
    )
    private val favoriteIndex = matchTypeTab.indexOf(MatchType.MY_EVENT)
    private inline fun getMainTabActivity() = activity as MainTabActivity
    private val fragmentHelper by lazy { FragmentHelper2(childFragmentManager, R.id.fl_content) }
    private val footView by lazy { SportFooterGamesView(binding.root.context) }
    private val mianViewModel: OKGamesViewModel by viewModel()
    private var todayTabItem:TabLayout.Tab?=null
    private val todayMenuPop by lazy { TodayMenuPop(requireActivity(), Math.max(0, todayMenuPosition)){ position ->
           matchTypeTab[todayMatchPosition] = matchTypeTodayTab[position]
           binding.tabLayout.getTabAt(todayMatchPosition)?.select()
      }
    }

    private var jumpMatchType: MatchType? = null
    private var jumpGameType: GameType? = null
    //根据赛事数量判断默认的分类
    private var defaultMatchType: MatchType? = null
    private var favoriteItems = listOf<Item>()
    private val favoriteDelayRunable by lazy { DelayRunable(this@SportFragment) { viewModel.loadFavoriteGameList() } }
    private inline fun favoriteCount(items: List<Item>): Int {
        return items.sumOf { it.leagueOddsList?.sumOf { it.matchOdds.size } ?: 0 }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        fragmentHelper.currentFragment()?.let {
            if (it.isAdded)
                it.onHiddenChanged(hidden)
        }
    }

    override fun onInitView(view: View) {
        initToolBar()
        initTabLayout()
        PopImageDialog.showDialog(childFragmentManager,ImageType.DIALOG_SPORT)
    }

    override fun onBindViewStatus(view: View) {
        footView.setUp(this, mianViewModel)
        getMenuData(true)
        favoriteDelayRunable.doOnDelay(0)

        initObserve()
    }

    fun initToolBar() = binding.homeToolbar.run {
        background = null
        attach(this@SportFragment, moneyViewEnable = false, onlyShowSeach = true)
        setMenuClick{ getMainTabActivity().showSportLeftMenu() }
        searchIcon.setOnClickListener { startActivity(SportSearchtActivity::class.java) }
        betlistIcon.setOnClickListener {
            loginedRun(it.context) {
                startActivity(BetRecordActivity::class.java)
            }
        }
        onPlaySelectListener = {
            when(it){
                0-> {}
                1-> {
                    OKGamesRepository.okPlayEvent.value?.let { it1 ->
                        getMainTabActivity().enterThirdGame(it1, "体育赛事列表顶部OKPlay")
                    }
                }
            }
        }
        setupOKPlay()
    }

    private fun initTabLayout() = binding.tabLayout.run {
        OverScrollDecoratorHelper.setUpOverScroll(this)
        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            private fun setTabStyle(tab: TabLayout.Tab, color: Int) {
                val color = ContextCompat.getColor(context, color)
                val binding = tab.customView!!.toBinding<HomeCateTabBinding>()
                binding.tvNumber.apply {
                    if (tab.isSelected)
                        setTextColor(color)
                    else
                        setTextColor(ContextCompat.getColor(context, R.color.color_000000))
                }
                binding.tvTitle.apply {
                    setTextColor(color)
                }
            }
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectTab(tab.position)
                setTabStyle(tab, R.color.color_025BE8)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                setTabStyle(tab, R.color.color_6D7693)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                //带箭头的就是today选项，进行弹窗选择
                selectTab(tab.position)
            }
        })
    }

    private var sportMenu: Menu? = null
    private var sportMenuData: SportMenuData? = null
    private fun refreshTabLayout(sportMenuResult: ApiResult<SportMenuData>) {

        sportMenuData = sportMenuResult.getData()
        sportMenuData?.menu?.let { sportMenu = it }
        val countInPlay = sportMenuData?.menu?.inPlay?.items?.sumOf { it.num } ?: 0
        val countAtStart = sportMenuData?.atStart?.items?.sumOf { it.num } ?: 0
        val countIn12hr = sportMenuData?.in12hr?.items?.sumOf { it.num } ?: 0
        val countIn24hr = sportMenuData?.in24hr?.items?.sumOf { it.num } ?: 0
        val countToday = sportMenuData?.menu?.today?.items?.sumOf { it.num } ?: 0
        val countEarly = sportMenuData?.menu?.early?.items?.sumOf { it.num } ?: 0
//        val countCS = sportMenuData?.menu?.cs?.items?.sumOf { it.num } ?: 0
        val countOutright = sportMenuData?.menu?.outright?.items?.sumOf { it.num } ?: 0
        val countParlay = sportMenuData?.menu?.parlay?.items?.sumOf { it.num } ?: 0
        val countBkEnd = sportMenuData?.menu?.bkEnd?.items?.sumOf { it.num } ?: 0
        defaultMatchType = when {
            countInPlay > 0 -> MatchType.IN_PLAY
//            countAtStart > 0 -> MatchType.AT_START
            countToday > 0 -> MatchType.TODAY
            else -> MatchType.EARLY
        }
        var position =0
        addTab(getString(R.string.home_tab_end_score), countBkEnd, position)
        addTab(getString(R.string.home_tab_in_play), countInPlay, ++position)
//        addTab(getString(R.string.home_tab_at_start), countAtStart, ++position)
        when (matchTypeTab[todayMatchPosition]){
            MatchType.TODAY-> addTab(getString(R.string.home_tab_today), countToday, ++position,true)
            MatchType.AT_START-> addTab(getString(R.string.home_tab_at_start), countAtStart, ++position,true)
            MatchType.IN12HR-> addTab(getString(R.string.P228), countAtStart, ++position,true)
            MatchType.IN24HR-> addTab(getString(R.string.P229), countAtStart, ++position,true)
        }
        addTab(getString(R.string.home_tab_early), countEarly, ++position)
        addTab(getString(R.string.home_tab_parlay), countParlay, ++position)
        addTab(getString(R.string.home_tab_outright), countOutright, ++position)
        val tabView = addTab(getString(R.string.N082), favoriteCount(favoriteItems), ++position)
        todayMenuPop.updateCount(countToday,countAtStart,countIn12hr,countIn24hr)
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

    private fun addTab(name: String, num: Int, position: Int,showArrow:Boolean = false): View = binding.tabLayout.run {

        val tab = if (tabCount > position) {
            getTabAt(position)!!
        } else {
            newTab().setCustomView(R.layout.home_cate_tab).apply {
                addTab(this, position, false)
            }
        }

        tab.customView!!.toBinding<HomeCateTabBinding>().run {
            tvTitle.text = name
            tvNumber.text = num.toString()
            ivArrow.isVisible = showArrow
            if(showArrow){
                todayTabItem = tab
                todayMenuPop.todayTabItem = todayTabItem
                root.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        if (todayMenuPop.isShowing){
                            todayMenuPop.dismiss()
                        }else{
                            todayMenuPop.showAsDropDown(binding.tabLayout)
                        }
                    }
                    return@setOnTouchListener true
                }
            }
        }

        return@run tab.customView!!
    }

    private var currentMatchType: MatchType? = null

    private fun selectTab(position: Int) {
        var matchType =  matchTypeTab.getOrNull(position) ?: return
        //排除之前未选中matchType的情况
        if (currentMatchType!=null){
            getMenuData(true)
        }
        currentMatchType = matchType
        navGameFragment(matchType)
    }


    private fun navGameFragment(matchType: MatchType) {
        var gameType = navGameSport?.key ?: jumpGameType?.key
        jumpMatchType = null
        jumpGameType = null

        val currentFragment = fragmentHelper.currentFragment() as BaseSportListFragment<*, *>?
        if (currentFragment?.currentMatchType() == matchType && gameType == currentFragment.currentGameType()) {
            return
        }

        val args = Bundle()
        args.putSerializable("matchType", matchType)
        args.putString("gameType", gameType)
        when (matchType) {
            MatchType.OUTRIGHT -> {
                fragmentHelper.show(SportOutrightFragment::class.java, args) { fragment, newInstance ->
                    fragment.resetFooterView(footView)
                    if (!newInstance && fragment.isAdded) {
                        gameType?.let { fragment.reload(it) }
                    }
                }
            }

            MatchType.END_SCORE -> {
                fragmentHelper.show(EndScoreFragment::class.java, args) { fragment, newInstance ->
                    fragment.resetFooterView(footView)
                }
            }

            MatchType.MY_EVENT -> {
                fragmentHelper.show(FavoriteFragment2::class.java, args) { fragment, newInstance ->
                    fragment.resetFooterView(footView)
                    fragment.setFavoriteData(favoriteItems)
                }
            }

            else -> {
                fragmentHelper.show(SportListFragment::class.java, args) { fragment, newInstance ->
                    fragment.resetFooterView(footView)
                    if (!newInstance && fragment.isAdded) {
                        fragment.reload(matchType, gameType)
                    }
                }
            }
        }

    }

    private var navGameSport: GameType? = null

    fun jumpToSport(gameType: GameType) {
        if (sportMenu == null) {
            navGameSport = gameType
            return
        }
        val matchType = findMatchType(sportMenuData, gameType)
        setJumpSport(matchType, gameType = gameType)
    }

    private fun findMatchType(sportMenuData: SportMenuData?, gameType: GameType): MatchType {
        var matchType: MatchType? = null
        if (sportMenuData!=null){
            val menu = sportMenuData?.menu
            matchType = findESport(menu.inPlay.items, MatchType.IN_PLAY, gameType)
                ?: findESport(menu.today.items, MatchType.TODAY, gameType)
                ?: findESport(sportMenuData.atStart.items, MatchType.AT_START, gameType)
                ?: findESport(sportMenuData.in12hr.items, MatchType.IN12HR, gameType)
                ?: findESport(sportMenuData.in24hr.items, MatchType.IN24HR, gameType)
                ?: findESport(menu.early.items, MatchType.EARLY, gameType)
            if (matchType!=null){
                return matchType
            }
        }
        if (matchType == null) {
            if (gameType == GameType.ES) { // 仅电竞的时候提示
                showPromptDialog(getString(R.string.prompt), getString(R.string.P172)) { }
            }
            return MatchType.EARLY
        }
        return matchType
    }
    private fun findESport(items: List<Item>, matchType: MatchType, gameType: GameType): MatchType? {
        items.forEach {
            if (gameType.key == it.code) {
                jumpMatchType = matchType
                return matchType
            }
        }

        return null
    }

    private var todayMenuPosition = 0
    fun setJumpSport(matchType: MatchType? = null, gameType: GameType? = null) {

        jumpMatchType = matchType
        jumpGameType = gameType

        //如果是今日，即将，12，24小时，则要标记上选中位置
        todayMenuPosition = matchTypeTodayTab.indexOf(matchType)

        if (isAdded) {
            if (todayMenuPosition >= 0){
                todayMenuPop.lastSelectPosition = todayMenuPosition
            }
            //如果体育当前已经在指定的matchType页面时，跳过检查重复选中的机制，强制筛选sportListFragment
            jumpMatchType = jumpMatchType ?: defaultMatchType
            matchType?.let { tabLayoutSelect(it) }
        }
    }

    private fun initObserve() = viewModel.run {
        sportMenuResult.observe(viewLifecycleOwner) {
            //这里的回调数据有可能来自其他页面的请求
            if (isAdded) {
                hideLoading()
                updateUiWithResult(it)
            }
        }

        sportTypeMenuData.observe(viewLifecycleOwner) { updateFavoriteItem(it.first) }

        var favorMatchs = favorMatchList.value
        favorMatchList.observe(viewLifecycleOwner) {
            if (favorMatchs == it) { return@observe }
            favorMatchs = it
            favoriteDelayRunable.doOnDelay(1300)
        }
        OKGamesRepository.okPlayEvent.observe(viewLifecycleOwner){
            binding.homeToolbar.setupOKPlay()
        }

        DataResourceChange.observe(viewLifecycleOwner) { getMenuData(true) }
    }

    private fun updateFavoriteItem(favoriteLeagues: List<Item>) {

        val favoriteTab = binding.tabLayout.getTabAt(favoriteIndex)
        if (favoriteTab == null) {
            favoriteItems = favoriteLeagues
            return
        }

        favoriteTab.customView!!.toBinding<HomeCateTabBinding>().tvNumber.text = favoriteCount(favoriteLeagues).toString()
        val currentFragment = fragmentHelper.currentFragment()
        if (currentFragment is FavoriteFragment2) {
            currentFragment.setFavoriteData(favoriteLeagues)
        }
        favoriteItems = favoriteLeagues
    }

    private fun updateUiWithResult(sportMenuResult: ApiResult<SportMenuData>) {
        if (!sportMenuResult.succeeded() || sportMenuResult.getData() == null) {
            navGameSport = null
            return
        }

        val isFirstSwitch = defaultMatchType == null
        refreshTabLayout(sportMenuResult)
        if (!isFirstSwitch) {
            navGameSport = null
            return
        }
        val matchType = if (navGameSport != null) {
            findMatchType(sportMenuResult.getData(), navGameSport!!)
        } else {
            jumpMatchType ?: defaultMatchType
        }
        if (matchType != null) {
            // 加post, 避免选中的tab不 能滚动到中间
            post{
                tabLayoutSelect(matchType)
                navGameSport = null
            }
        }
    }
    //是否拿最新的sportMenu数据
    private fun getMenuData(newData: Boolean) {
        viewModel.getSportMenuData(isNew = true)
    }

    private fun tabLayoutSelect(matchType: MatchType){
       val todayIndex= matchTypeTodayTab.indexOf(matchType)
        if (todayIndex>=0){
            matchTypeTab[todayMatchPosition] = matchTypeTodayTab[todayIndex]
            todayTabItem?.customView?.toBinding<HomeCateTabBinding>()?.apply {
                when (matchType){
                    MatchType.TODAY-> {
                        tvTitle.text = getString(R.string.home_tab_today)
                        tvNumber.text = sportMenu?.today?.num.toString()
                    }
                    MatchType.AT_START-> {
                        tvTitle.text = getString(R.string.home_tab_at_start)
                        tvNumber.text = sportMenuData?.atStart?.num.toString()
                    }
                    MatchType.IN12HR-> {
                        tvTitle.text = getString(R.string.P228)
                        tvNumber.text = sportMenuData?.in12hr?.num.toString()
                    }
                    MatchType.IN24HR-> {
                        tvTitle.text = getString(R.string.P229)
                        tvNumber.text = sportMenuData?.in24hr?.num.toString()
                    }
                }
            }
            binding.tabLayout.getTabAt(todayMatchPosition)?.select()
        }else{
            binding.tabLayout.getTabAt(matchTypeTab.indexOfFirst { it == matchType })?.select()
        }
    }
}