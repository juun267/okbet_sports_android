package org.cxct.sportlottery.ui.sport

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.newInstanceFragment
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.FragmentSport2Binding
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.Menu
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.betRecord.BetRecordActivity
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.menu.SportLeftMenuFragment
import org.cxct.sportlottery.ui.maintab.worldcup.FIBAUtil
import org.cxct.sportlottery.ui.sport.endscore.EndScoreFragment
import org.cxct.sportlottery.ui.sport.favorite.FavoriteFragment2
import org.cxct.sportlottery.ui.sport.list.SportListFragment2
import org.cxct.sportlottery.ui.sport.list.adapter.SportFooterGamesView
import org.cxct.sportlottery.ui.sport.outright.SportOutrightFragment
import org.cxct.sportlottery.ui.sport.search.SportSearchtActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.dialog.PopImageDialog
import org.cxct.sportlottery.view.isVisible
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

class SportFragment2: BindingSocketFragment<SportTabViewModel, FragmentSport2Binding>() {

    private val matchTypeTab = mutableListOf(
        MatchType.END_SCORE,
        MatchType.IN_PLAY,
        MatchType.TODAY,
        MatchType.EARLY,
        MatchType.PARLAY,
        MatchType.OUTRIGHT,
        MatchType.MY_EVENT
    ).apply {
        if (FIBAUtil.enableFiba()){
            add(1,MatchType.FIBA)
        }
    }
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
    private val todayMenuPop by lazy { TodayMenuPop(requireActivity(), onItemClickListener = { position ->
           matchTypeTab[2] = matchTypeTodayTab[position]
           binding.tabLayout.getTabAt(2)?.select()
      })
    }

    private var jumpMatchType: MatchType? = null
    private var jumpGameType: GameType? = null
    //根据赛事数量判断默认的分类
    private var defaultMatchType: MatchType? = null
    private var favoriteItems = listOf<Item>()
    private val favoriteDelayRunable by lazy { DelayRunable(this@SportFragment2) { viewModel.loadFavoriteGameList() } }
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
        showSportDialog()
    }

    override fun onBindViewStatus(view: View) {
        footView.setUp(this, mianViewModel)
        viewModel.getMatchData()
        jumpMatchType?.let { navGameFragment(it) }
        favoriteDelayRunable.doOnDelay(0)

        initObserve()
    }

    fun initToolBar() = binding.homeToolbar.run {
        setPadding(paddingLeft, paddingTop, paddingRight,2.dp)
        background = null
        attach(this@SportFragment2, getMainTabActivity(), viewModel, moneyViewEnable = false, onlyShowSeach = true)
        searchIcon.setOnClickListener { startActivity(SportSearchtActivity::class.java) }
        betlistIcon.setOnClickListener {
            loginedRun(it.context) {
                startActivity(BetRecordActivity::class.java)
            }
        }
        (ivLogo.layoutParams as MarginLayoutParams).apply {
            bottomMargin = 5.dp
            ivLogo.layoutParams = this
        }
        ivMenuLeft.setOnClickListener {
            getMainTabActivity().showSportLeftMenu()
            EventBusUtil.post(MenuEvent(true))
        }
    }

    private fun initTabLayout() = binding.tabLayout.run {
        OverScrollDecoratorHelper.setUpOverScroll(this)
        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            private fun setTabStyle(tab: TabLayout.Tab, color: Int) {
                val color = ContextCompat.getColor(context, color)
                tab.customView!!.tv_number.apply {
                    if (tab.isSelected)
                        setTextColor(color)
                    else
                        setTextColor(ContextCompat.getColor(context, R.color.color_000000))
                }
                tab.customView!!.tv_title.apply {
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
    private fun refreshTabLayout(sportMenuResult: ApiResult<SportMenuData>) {

        val sportMenuData = sportMenuResult.getData()
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
        if (FIBAUtil.enableFiba()){
            addTab(getString(R.string.fiba_2023), FIBAUtil.takeFIBAItem()?.num?:0, ++position)
        }
        addTab(getString(R.string.home_tab_in_play), countInPlay, ++position)
//        addTab(getString(R.string.home_tab_at_start), countAtStart, ++position)
        when (matchTypeTab[2]){
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

        tab.customView?.run {
            tv_title.text = name
            tv_number.text = if(name==getString(R.string.fiba_2023)) "" else num.toString()
            ivArrow.isVisible = showArrow
            if(showArrow){
                todayTabItem = tab
                todayMenuPop.todayTabItem = todayTabItem
                (parent as View).setOnTouchListener { _, event ->
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
    private fun removeTab(name: String) = binding.tabLayout.run {
        for (index in 0 until tabCount){
            if(getTabAt(index)?.customView?.tv_title?.text == name){
                removeTabAt(index)
            }
        }
    }

    private var currentMatchType: MatchType? = null

    private fun selectTab(position: Int) {
        var matchType =  matchTypeTab.getOrNull(position) ?: return
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
                fragmentHelper.show(SportListFragment2::class.java, args) { fragment, newInstance ->
                    fragment.resetFooterView(footView)
                    if (!newInstance && fragment.isAdded) {
                        fragment.reload(matchType, gameType)
                    }
                }
            }
        }

    }

    private var navGameSport: GameType? = null
    fun setJumpESport() {
        jumpToSport(GameType.ES)
    }

    fun jumpToSport(gameType: GameType) {
        if (sportMenu == null) {
            navGameSport = gameType
            return
        }

        val menuData = sportMenu!!
        val matchType = findMatchType(menuData, gameType)
        setJumpSport(matchType, gameType = gameType)
    }

    private fun findMatchType(menu: Menu, gameType: GameType): MatchType {
        val matchType = findESport(menu.inPlay.items, MatchType.IN_PLAY, gameType)
            ?: findESport(menu.today.items, MatchType.TODAY, gameType)
            ?: findESport(menu.early.items, MatchType.EARLY, gameType)

        if (matchType == null) {
            if (gameType == GameType.ES) { // 仅电竞的时候提示
                showPromptDialog(getString(R.string.prompt), getString(R.string.P172)) { }
            }
//            ToastUtil.showToast(context(), R.string.P172)
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

        var favorMatchs = favorMatchList.value
        favorMatchList.observe(viewLifecycleOwner) {
            if (favorMatchs == it) { return@observe }
            favorMatchs = it
            favoriteDelayRunable.doOnDelay(1300)
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
        if (!sportMenuResult.succeeded() || sportMenuResult.getData() == null) {
            navGameSport = null
            return
        }

        val isFirstSwitch = defaultMatchType == null
        refreshTabLayout(sportMenuResult)
        EventBusUtil.post(sportMenuResult)
        if (!isFirstSwitch) {
            navGameSport = null
            return
        }
        val menuData = sportMenuResult.getData()!!.menu
        val matchType = if (navGameSport != null) {
            findMatchType(menuData, navGameSport!!)
        } else {
            jumpMatchType ?: defaultMatchType
        }
        if (matchType != null) {
            // 加post, 避免选中的tab不 能滚动到中间
            post{
                binding.tabLayout.getTabAt(matchTypeTab.indexOfFirst { it == matchType })?.select()
                navGameSport = null
            }
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
    fun onScrollTop(isTop: Boolean){
        binding.tabShadow.layoutParams.apply {
            height = if(isTop) 0 else 1.dp
            binding.tabShadow.layoutParams = this
        }
    }
}