package org.cxct.sportlottery.ui.sport.outright

import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.chad.library.adapter.base.entity.node.BaseNode
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_sport_list.*
import kotlinx.android.synthetic.main.fragment_sport_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setViewGone
import org.cxct.sportlottery.common.extentions.setViewVisible
import org.cxct.sportlottery.common.extentions.showLoading
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.game.hall.adapter.*
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectActivity
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.layoutmanager.SocketGridManager
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
class SportOutrightFragment: BaseBottomNavigationFragment<SportListViewModel>(SportListViewModel::class) {

    //    private val args: GameV3FragmentArgs by navArgs()
    private val matchType = MatchType.OUTRIGHT
    private var gameType: String = GameType.BK.key
    private var mLeagueIsFiltered = false // 是否套用聯賽過濾
    private var mCalendarSelected = false //紀錄日期圖示選中狀態
    var leagueIdList = mutableListOf<String>() // 赛选的联赛id

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {

                if (!it.isSelected) {
                    //切換球種，清除日期記憶
                    viewModel.tempDatePosition = 0
                    //日期圖示選取狀態下，切換球種要重置UI狀態
                    if (iv_calendar.isSelected) {
                        iv_calendar.performClick()
                    }
                }
                gameType = it.code
                dataSport.forEach { it.isSelected = (it.code == gameType) }
                notifyDataSetChanged()
                viewModel.cleanGameHallResult()
                sportOutrightAdapter2.showLoading(R.layout.view_list_loading)
//                sportOutrightAdapter.setPreloadItem()
                //切換球種後要重置位置
                (sport_type_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
                    sport_type_list,
                    RecyclerView.State(),
                    dataSport.indexOfFirst { it.isSelected })
                loading()
                unSubscribeAll()
                viewModel.switchGameType(matchType, it)
                iv_arrow.isSelected = true
            }

            thirdGameListener = ThirdGameListener {  }
        }
    }


    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            if (game_list == null || context == null || oddsChangeEvent.oddsList.isNullOrEmpty()) {
                return@OddsChangeListener
            }

            sportOutrightAdapter2.onMatchOdds(subscribedMatchOdd, oddsChangeEvent)
        }
    }

    private fun setupOddsChangeListener() {
        receiver.oddsChangeListener = mOddsChangeListener
    }

    private val dateAdapter by lazy {
        DateAdapter().apply {
            dateListener = DateListener {
                viewModel.switchMatchDate(matchType, it)
                loading()
            }
        }
    }

    override fun loading() {
        stopTimer()
    }

    override fun hideLoading() {
        if (timer == null) startTimer()
    }

    override fun layoutId() = R.layout.fragment_sport_list

    private val sportOutrightAdapter2: SportOutrightAdapter2 by lazy {

        game_list?.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    setOutrightLeagueAdapter(0)
                } else {
                    unSubscribeAll()
                }
            }
        })

        SportOutrightAdapter2(this@SportOutrightFragment) { _, _, item ->
            if (item is Odd) {  // 赔率
                addOutRightOddsDialog((item.parentNode as CategoryOdds).matchOdd, item, item.outrightCateKey ?: "")
            } else { // 展开或收起
                setOutrightLeagueAdapter(200)
            }
        }
    }

    override fun onBindView(view: View) {
        EventBusUtil.targetLifecycle(this)
        arguments?.getString("gameType")?.let { gameType = it }
        gameType?.let { viewModel.gameType = it  }
        setupSportTypeList()
        setupToolbar()
        setupGameRow()
        setupGameListView()
        initObserve()
        initSocketObserver()
        viewModel.cleanGameHallResult()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            unSubscribeAll()
        } else {
            //receiver.oddsChangeListener為activity底下共用, 顯示當前畫面時需重新配置listener
            setupOddsChangeListener()
            setOutrightLeagueAdapter(0)
        }
    }

    private fun setupSportTypeList() {
        sport_type_list.apply {
            layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
            adapter = gameTypeAdapter
        }
    }

    var offsetScrollListener: ((Double) -> Unit)? = null

    private fun setupToolbar() {

        appbar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            offsetScrollListener?.invoke((-verticalOffset) / Math.max(1.0, appbar_layout.measuredHeight.toDouble()))
        })

        iv_calendar.apply {
            isVisible = matchType == MatchType.EARLY
            setOnClickListener {
                val newSelectedStatus = !isSelected
                mCalendarSelected = newSelectedStatus
                isSelected = newSelectedStatus
                view?.game_filter_type_list?.isVisible = iv_calendar.isSelected
            }
        }
        //冠军不需要筛选
        lin_filter.isVisible = false
        lin_filter.setOnClickListener {
            gameType?.let {
                LeagueSelectActivity.start(requireContext(),
                    it,
                    matchType,
                    null,
                    null,
                    leagueIdList)
            }
        }

        iv_arrow.bindExpanedAdapter(sportOutrightAdapter2) { setOutrightLeagueAdapter(120) }
    }

    private fun setupGameRow() {
        game_filter_type_list.run {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
            adapter = dateAdapter
            removeItemDecorations()
            addItemDecoration(SpaceItemDecoration(context, R.dimen.recyclerview_item_dec_spec_date))
        }

        if (matchType == MatchType.EARLY) {
            mCalendarSelected = true
            setViewVisible(iv_calendar, game_filter_type_list)
        } else {
            mCalendarSelected = false
            setViewGone(iv_calendar, game_filter_type_list)
        }
    }

    private fun setupGameListView() {
        game_list.apply {
//            layoutManager = GridLayoutManager(context, 2)
            layoutManager = SocketGridManager(context, 2)
            adapter = sportOutrightAdapter2
            if (viewModel.getMatchCount(matchType) > 0) {
                sportOutrightAdapter2.showLoading(R.layout.view_list_loading)
            }
        }
    }

    /**
     * 設置冠軍adapter, 訂閱當前頁面上的資料
     */
    private fun setOutrightLeagueAdapter(delay: Long = 0) {
        game_list?.removeCallbacks(subscribeVisibleRange)
        unSubscribeAll()
        game_list?.postDelayed(subscribeVisibleRange, delay)
    }

    private val subscribeVisibleRange by lazy {
        Runnable {
            if (game_list == null
                || sportOutrightAdapter2.getCount() < 1
                || game_list?.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                return@Runnable
            }

            val matchOdds = mutableSetOf<org.cxct.sportlottery.network.outright.odds.MatchOdd>()
            sportOutrightAdapter2.doOnVisiableRange { _, item ->
                if(item is Odd) {
                    matchOdds.add((item.parentNode as CategoryOdds).matchOdd)
                }
            }
            matchOdds.forEach { subscribeChannelHall(it) }
        }
    }

    private fun initObserve() = viewModel.run {
        notifyLogin.observe(viewLifecycleOwner) {
            (activity as MainTabActivity).showLoginNotify()
        }

        showErrorDialogMsg.observe(viewLifecycleOwner) {
            if (requireContext() == null || TextUtils.isEmpty(it)) {
                return@observe
            }

            showErrorMsgDialog(it)
        }

        sportMenuResult.distinctUntilChanged().observe(viewLifecycleOwner) {
            when (matchType) {
                MatchType.IN_PLAY -> {
                    updateSportType(it?.sportMenuData?.menu?.inPlay?.items ?: listOf())
                }

                MatchType.TODAY -> {
                    updateSportType(it?.sportMenuData?.menu?.today?.items ?: listOf())
                }

                MatchType.EARLY -> {
                    updateSportType(it?.sportMenuData?.menu?.early?.items ?: listOf())
                }
                MatchType.CS -> {
                    updateSportType(it?.sportMenuData?.menu?.cs?.items ?: listOf())
                }

                MatchType.PARLAY -> {
                    updateSportType(it?.sportMenuData?.menu?.parlay?.items ?: listOf())
                }

                MatchType.OUTRIGHT -> {
                    updateSportType(it?.sportMenuData?.menu?.outright?.items ?: listOf())
                }

                MatchType.AT_START -> {
                    updateSportType(it?.sportMenuData?.atStart?.items ?: listOf())
                }

                MatchType.EPS -> {
                    updateSportType(it?.sportMenuData?.menu?.eps?.items ?: listOf())
                }

                else -> {
                }
            }
        }

        curDate.observe(viewLifecycleOwner) { dateAdapter.data = it }
        outrightList.observe(viewLifecycleOwner) {
            if (gameType != it.tag) {
                return@observe
            }
            val data = it?.getContentIfNotHandled()?.outrightOddsListData?.leagueOdds ?: return@observe
            val list = mutableListOf<MatchOdd>()
            data.forEach { it.matchOdds?.let { list.addAll(it) } }

            if (list.isEmpty()) {
                sportOutrightAdapter2.setEmptyView(R.layout.itemview_game_no_record)
                return@observe
            }
            sportOutrightAdapter2.setNewInstance(list as MutableList<BaseNode>)
            setOutrightLeagueAdapter(120)
        }

        //當前玩法無賽事
        isNoEvents.distinctUntilChanged().observe(viewLifecycleOwner) {
            sport_type_list.isVisible = !it
            hideLoading()
        }

        betInfoList.observe(viewLifecycleOwner) {
            it.peekContent().let {
                sportOutrightAdapter2.updateOddsSelectedStatus(it)
            }
        }

        oddsType.observe(viewLifecycleOwner) { sportOutrightAdapter2.oddsType = it }

        leagueFilterList.observe(viewLifecycleOwner) { leagueList ->
            mLeagueIsFiltered = leagueList.isNotEmpty()
            sport_type_list.visibility = if (mLeagueIsFiltered) View.GONE else View.VISIBLE
        }

    }

    private fun initSocketObserver() = receiver.run {
        serviceConnectStatus.observe(viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    viewModel.switchMatchType(matchType = matchType)
                    subscribeSportChannelHall()
                } else {
                    stopTimer()
                }
            }
        }

        producerUp.observe(viewLifecycleOwner) {
            it?.let {
                unSubscribeAll()
            }
        }
    }

    private fun updateSportType(gameTypeList: List<Item>) {
        if (gameTypeList.isEmpty()) {
            sport_type_list.isVisible = true
            iv_calendar.isVisible = matchType == MatchType.EARLY
            hideLoading()
            return
        }

        //处理默认不选中的情况
        if (gameType.isNullOrEmpty()) {
            gameTypeList.find { it.num > 0 }?.let {
                it.isSelected = true
                gameType = it.code
                viewModel.switchGameType(matchType, it)
            }
        } else {
            (gameTypeList.find { it.code == gameType } ?: gameTypeList.first()).let {
                gameType = it.code
                if (!it.isSelected) {
                    it.isSelected = true
                    viewModel.switchGameType(matchType, it)
                }
            }
        }

        gameTypeAdapter.dataSport = gameTypeList
        (sport_type_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
            sport_type_list,
            RecyclerView.State(),
            gameTypeAdapter.dataSport.indexOfFirst { it.isSelected })
        sport_type_list?.post {
            //球種如果選過，下次回來也需要滑動置中
            if (gameTypeList.isEmpty()) {
                sport_type_list?.visibility = View.GONE
                iv_calendar?.visibility = View.GONE
                game_filter_type_list?.visibility = View.GONE
            } else {
                sport_type_list?.visibility = if (mLeagueIsFiltered) View.GONE else View.VISIBLE
                iv_calendar?.apply {
                    visibility = when (matchType) {
                        MatchType.EARLY -> View.VISIBLE
                        else -> View.GONE
                    }
                    isSelected = mCalendarSelected
                }
            }
        }
    }

    private fun addOutRightOddsDialog(
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: Odd,
        playCateCode: String) {

        GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.let {

            (activity as MainTabActivity).setupBetData(FastBetDataBean(
                matchType = MatchType.OUTRIGHT,
                gameType = it,
                playCateCode = playCateCode,
                playCateName = (odd.parentNode as CategoryOdds).name,
                matchInfo = matchOdd.matchInfo!!,
                matchOdd = matchOdd,
                odd = odd,
                subscribeChannelType = ChannelType.HALL,
                betPlayCateNameMap = null))
        }

    }

    private val subscribedMatchOdd = mutableMapOf<String, org.cxct.sportlottery.network.outright.odds.MatchOdd>()

    private fun subscribeChannelHall(matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd) {
        val gameType = GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)
        gameType?.let {
            subscribedMatchOdd["${matchOdd.matchInfo?.id}"] = matchOdd
            subscribeChannelHall(it.key, matchOdd?.matchInfo?.id)
            matchOdd?.matchInfo?.let { Log.e("[subscribe]","訂閱 ${it.name} ${it.id} -> " + "${it.homeName} vs " + "${it.awayName}") }
        }
    }

    private fun unSubscribeAll() {
        subscribedMatchOdd.clear()
        unSubscribeChannelHallAll()
    }

    private var timer: Timer? = null

    private fun startTimer() {
        stopTimer()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                viewModel.switchMatchType(matchType)
            }
        }, 60 * 3 * 1000L, 60 * 3 * 1000L)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }


    override fun onDestroyView() {
        super.onDestroyView()
        offsetScrollListener = null
        game_list.adapter = null
        game_list?.removeCallbacks(subscribeVisibleRange)
        stopTimer()
        unSubscribeAll()
        unSubscribeChannelHallSport()
    }

    // 赛选联赛
    @Subscribe
    fun onSelectLeague(leagueList: List<League>) {
        viewModel.filterLeague(leagueList)
        leagueIdList.clear()
        leagueList.forEach { leagueIdList.add(it.id) }
        viewModel.getGameHallList(
            matchType,
            isReloadDate = true,
            isReloadPlayCate = false,
            isLastSportType = true,
            leagueIdList = leagueIdList
        )
    }

    open fun getCurGameType(): GameType {
        return GameType.getGameType(gameType) ?: GameType.ALL
    }
}