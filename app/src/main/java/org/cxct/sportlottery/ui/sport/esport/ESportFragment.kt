package org.cxct.sportlottery.ui.sport.esport

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.toStringS
import org.cxct.sportlottery.databinding.FragmentSport2Binding
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.*
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.betRecord.BetRecordActivity
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.sport.BaseSportListFragment
import org.cxct.sportlottery.ui.sport.SportTabViewModel
import org.cxct.sportlottery.ui.sport.list.TodayMenuPop
import org.cxct.sportlottery.ui.sport.list.adapter.SportFooterGamesView
import org.cxct.sportlottery.ui.sport.search.SportSearchtActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import splitties.fragments.start
import kotlin.reflect.KClass

class ESportFragment: BaseSocketFragment<SportTabViewModel, FragmentSport2Binding>() {

    override fun createVM(clazz: KClass<SportTabViewModel>): SportTabViewModel {
        return getViewModel(clazz = clazz)
    }

    private val matchTypeTab = mutableListOf(
        MatchType.IN_PLAY,
        MatchType.TODAY,
        MatchType.EARLY,
        MatchType.PARLAY,
        MatchType.OUTRIGHT,
        MatchType.MY_EVENT
    )
    private val todayMatchPosition = 1
    private val matchTypeTodayTab = mutableListOf(
        MatchType.TODAY,
        MatchType.AT_START,
        MatchType.IN12HR,
        MatchType.IN24HR
    )
    private val favoriteIndex = matchTypeTab.indexOf(MatchType.MY_EVENT)
    private inline fun getMainTabActivity() = activity as MainTabActivity
    private lateinit var fragmentHelper: FragmentHelper2
    private val footView by lazy { SportFooterGamesView(binding.root.context,esportTheme = true) }
    private val mianViewModel: OKGamesViewModel by viewModel()
    private var todayTabItem:TabLayout.Tab?=null
    private val todayMenuPop by lazy { TodayMenuPop(requireActivity(), Math.max(0, todayMenuPosition)) { position ->
           matchTypeTab[todayMatchPosition] = matchTypeTodayTab[position]
           binding.tabLayout.getTabAt(todayMatchPosition)?.select()
      }
    }

    private var jumpMatchType: MatchType? = null
    private var jumpGameType: String? = null
    //根据赛事数量判断默认的分类
    private var defaultMatchType: MatchType? = null
    private var curFavoriteItem: Item? = null
    private val favoriteDelayRunable by lazy { DelayRunable(this@ESportFragment) { viewModel.loadFavoriteGameList() } }
    private inline fun favoriteCount(items: Item?): Int {
        return items?.leagueOddsList?.sumOf { it.matchOdds.size }?:0
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
        binding.linToolbar.setBackgroundResource(R.drawable.bg_esport_head)
        footView.apply {
            setBackgroundResource(R.color.transparent_white_50)
            homeBottomView.setBackgroundResource(R.color.transparent_white_50)
        }
    }

    override fun onBindViewStatus(view: View) {
        fragmentHelper = FragmentHelper2(childFragmentManager, R.id.fl_content)
        if (binding.tabLayout.tabCount > 0) {
            binding.tabLayout.removeAllTabs()
        }
        footView.setUp(this, mianViewModel)
        binding.homeToolbar.setMenuClick{ getMainTabActivity().showMainLeftMenu(this@ESportFragment.javaClass) }
        binding.homeToolbar.attach(this@ESportFragment, getMainTabActivity(), viewModel, moneyViewEnable = false, onlyShowSeach = true)
        getMenuData(true)
        favoriteDelayRunable.doOnDelay(0)

        initObserve()
    }

    fun initToolBar() = binding.homeToolbar.run {
        background = null
        searchIcon.setOnClickListener { start<SportSearchtActivity> { putExtra("gameType",GameType.ES.key)  } }
        betlistIcon.setOnClickListener {
            loginedRun(it.context) {
                startActivity(BetRecordActivity::class.java)
            }
        }
    }

    private fun initTabLayout() = binding.tabLayout.run {
        OverScrollDecoratorHelper.setUpOverScroll(this)
        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            private fun setTabStyle(tab: TabLayout.Tab, color: Int) {
                val color = ContextCompat.getColor(context, color)
                tab.customView!!.findViewById<TextView>(R.id.tv_number).apply {
                    if (tab.isSelected)
                        setTextColor(color)
                    else
                        setTextColor(ContextCompat.getColor(context, R.color.color_000000))
                }
                tab.customView!!.findViewById<TextView>(R.id.tv_title).apply {
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
        val countInPlay = sportMenuData?.menu?.inPlay?.numOfESport()?:0
        val countAtStart = sportMenuData?.atStart?.numOfESport()?:0
        val countIn12hr = sportMenuData?.in12hr?.numOfESport()?:0
        val countIn24hr = sportMenuData?.in24hr?.numOfESport()?:0
        val countToday = sportMenuData?.menu?.today?.numOfESport()?:0
        val countEarly = sportMenuData?.menu?.early?.numOfESport()?:0
        val countOutright = sportMenuData?.menu?.outright?.numOfESport()?:0
        val countParlay = sportMenuData?.menu?.parlay?.numOfESport()?:0
        defaultMatchType = when {
            countInPlay > 0 -> MatchType.IN_PLAY
//            countAtStart > 0 -> MatchType.AT_START
            countToday > 0 -> MatchType.TODAY
            else -> MatchType.EARLY
        }
        var position =0
        addTab(getString(R.string.home_tab_in_play), countInPlay, +position)
        when (matchTypeTab[todayMatchPosition]){
            MatchType.TODAY-> addTab(getString(R.string.home_tab_today), countToday, ++position,true)
            MatchType.AT_START-> addTab(getString(R.string.home_tab_at_start), countAtStart, ++position,true)
            MatchType.IN12HR-> addTab(getString(R.string.P228), countIn12hr, ++position,true)
            MatchType.IN24HR-> addTab(getString(R.string.P229), countIn24hr, ++position,true)
        }
        addTab(getString(R.string.home_tab_early), countEarly, ++position)
        addTab(getString(R.string.home_tab_parlay), countParlay, ++position)
        addTab(getString(R.string.home_tab_outright), countOutright, ++position)
        val tabView = addTab(getString(R.string.N082), favoriteCount(curFavoriteItem), ++position)
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
            findViewById<TextView>(R.id.tv_title).text = name
            findViewById<TextView>(R.id.tv_number).text = num.toString()
            findViewById<ImageView>(R.id.ivArrow).isVisible = showArrow
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
            if(getTabAt(index)?.customView?.findViewById<TextView>(R.id.tv_title)?.text == name){
                removeTabAt(index)
            }
        }
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
        var gameType = navGameSport ?: jumpGameType
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
                fragmentHelper.show(ESportOutrightFragment::class.java, args) { fragment, newInstance ->
                    fragment.resetFooterView(footView)
                    if (!newInstance && fragment.isAdded) {
                        gameType?.let { fragment.reload(it) }
                    }
                }
            }

            MatchType.MY_EVENT -> {
                fragmentHelper.show(ESportFavoriteFragment::class.java, args) { fragment, _ ->
                    fragment.resetFooterView(footView)
                    fragment.setFavoriteData(curFavoriteItem)
                }
            }

            else -> {
                fragmentHelper.show(ESportListFragment::class.java, args) { fragment, newInstance ->
                    fragment.resetFooterView(footView)
                    if (!newInstance && fragment.isAdded) {
                        fragment.reload(matchType, gameType)
                    }
                }
            }
        }

    }

    private var navGameSport: String? = null

    fun jumpToSport(gameType: String) {
        if (sportMenu == null) {
            navGameSport = gameType
            return
        }

        val menuData = sportMenu!!
        val matchType = findMatchType(menuData, gameType)
        setJumpSport(matchType, gameType = gameType)
    }

    private fun findMatchType(menu: Menu, gameType: String): MatchType {
        return findESport(menu.inPlay.items, MatchType.IN_PLAY, gameType)
            ?: findESport(menu.today.items, MatchType.TODAY, gameType)
            ?: findESport(menu.early.items, MatchType.EARLY, gameType) ?: MatchType.EARLY
    }
    private fun findESport(items: List<Item>, matchType: MatchType, gameType: String): MatchType? {
        items.forEach {
            if (gameType == it.code) {
                jumpMatchType = matchType
                return matchType
            }
        }

        return null
    }

    var todayMenuPosition = 0
    fun setJumpSport(matchType: MatchType? = null, gameType: String? = null) {
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

        esportTypeMenuData.observe(viewLifecycleOwner) {
            updateFavoriteItem(it.first)
        }

        var favorMatchs = favorMatchList.value
        favorMatchList.observe(viewLifecycleOwner) {
            if (favorMatchs == it) { return@observe }
            favorMatchs = it
            favoriteDelayRunable.doOnDelay(1300)
        }
    }

    private fun updateFavoriteItem(favoriteItem: Item?) {
        val favoriteTab = binding.tabLayout.getTabAt(favoriteIndex)
        if (favoriteTab == null) {
            curFavoriteItem = favoriteItem
            return
        }

        favoriteTab.customView?.findViewById<TextView>(R.id.tv_number)?.text = favoriteCount(favoriteItem).toString()
        val currentFragment = fragmentHelper.currentFragment()
        if (currentFragment is ESportFavoriteFragment) {
            currentFragment.setFavoriteData(curFavoriteItem)
        }
        curFavoriteItem = favoriteItem
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
        val menuData = sportMenuResult.getData()!!.menu
        val matchType = if (navGameSport != null) {
            findMatchType(menuData, navGameSport!!)
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
    private fun getMenuData(newData:Boolean) {
        if (newData){
            viewModel.getSportMenuData(isNew = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        defaultMatchType = null
        fragmentHelper.destory()
    }
    fun tabLayoutSelect(matchType: MatchType){
        val todayIndex= matchTypeTodayTab.indexOf(matchType)
        if (todayIndex>=0){
            matchTypeTab[todayMatchPosition] = matchTypeTodayTab[todayIndex]
            todayTabItem?.customView?.apply {
               val tvTitle = findViewById<TextView>(R.id.tv_title)
                val tvNumber = findViewById<TextView>(R.id.tv_number)
                when (matchType){
                    MatchType.TODAY-> {
                        tvTitle.text = getString(R.string.home_tab_today)
                        tvNumber.text = sportMenu?.today?.numOfESport().toStringS("0")
                    }
                    MatchType.AT_START-> {
                        tvTitle.text = getString(R.string.home_tab_at_start)
                        tvNumber.text = sportMenuData?.atStart?.numOfESport().toStringS("0")
                    }
                    MatchType.IN12HR-> {
                        tvTitle.text = getString(R.string.P228)
                        tvNumber.text = sportMenuData?.in12hr?.numOfESport().toStringS("0")
                    }
                    MatchType.IN24HR-> {
                        tvTitle.text = getString(R.string.P229)
                        tvNumber.text = sportMenuData?.in24hr?.numOfESport().toStringS("0")
                    }
                }
            }
            binding.tabLayout.getTabAt(todayMatchPosition)?.select()
        }else{
            binding.tabLayout.getTabAt(matchTypeTab.indexOfFirst { it == matchType })?.select()
        }
    }

}