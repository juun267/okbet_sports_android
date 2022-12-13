package org.cxct.sportlottery.ui.sport

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_sport_list.*
import kotlinx.android.synthetic.main.fragment_sport_list.view.*
import kotlinx.android.synthetic.main.itemview_league_v5.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.*
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.SportFragment
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.ui.sport.favorite.LeagueListener
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.widget.VerticalDecoration
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
class SportListFragment :
    BaseBottomNavigationFragment<SportListViewModel>(SportListViewModel::class) {
    companion object {
        fun newInstance(
            matchType: MatchType? = MatchType.IN_PLAY,
            gameType: String? = GameType.ALL.key,
            outrightLeagueId: String? = null,
        ): SportListFragment {
            val args = Bundle()
            args.putSerializable("matchType", matchType)
            args.putString("gameType", gameType)
            outrightLeagueId?.let {
                args.putString("outrightLeagueId", outrightLeagueId)
            }
            val fragment = SportListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //    private val args: GameV3FragmentArgs by navArgs()
    private val matchType by lazy {
        (arguments?.getSerializable("matchType") as MatchType?) ?: MatchType.IN_PLAY
    }
    private var gameType: String? = null
        set(value) {
            if (!Objects.equals(value, field)) { // 清除赛选条件
                leagueIdList.clear()
                viewModel.filterLeague(mutableListOf())
            }
            field = value
        }
    private var mView: View? = null
    private var mLeagueIsFiltered = false // 是否套用聯賽過濾
    private var mCalendarSelected = false //紀錄日期圖示選中狀態
    var leagueIdList = mutableListOf<String>()

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {

                if (!it.isSelected) {
                    //切換球種，清除日期記憶
                    viewModel.tempDatePosition = 0
                    //日期圖示選取狀態下，切換球種要重置UI狀態
                    if (iv_calendar.isSelected) iv_calendar.performClick()
                }
                gameType = it.code
                dataSport.forEach { item ->
                    item.isSelected = (item.code == gameType)
                }
                notifyDataSetChanged()
                viewModel.cleanGameHallResult()
                sportLeagueAdapter.removePreloadItem()
                (sport_type_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
                    sport_type_list,
                    RecyclerView.State(),
                    dataSport.indexOfFirst { it.isSelected })
                //切換球種後要重置位置
                loading()
                unSubscribeChannelHallAll()
                viewModel.switchGameType(it)
                iv_arrow.isSelected = true
                lin_filter.isVisible = gameType != GameType.ALL.key
            }

            thirdGameListener = ThirdGameListener {
                navThirdGame(it)
            }
        }
    }

    private val dateAdapter by lazy {
        DateAdapter().apply {
            dateListener = DateListener {
                viewModel.switchMatchDate(matchType, it)
                loading()
            }
        }
    }


    private val sportLeagueAdapter by lazy {
        SportLeagueAdapter(this, matchType).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            leagueListener = LeagueListener {
                if (it.unfoldStatus == FoldState.FOLD.code) {
                    Log.d("[subscribe]", "取消訂閱 ${it.league.name}")
                    unSubscribeChannelHall(it)
                }
                //目前無法監聽收合動畫
                firstVisibleRange(400)
            }
            leagueOddListener =
                LeagueOddListener(clickListenerPlayType = { matchId, matchInfoList, _, liveVideo ->
                    matchInfoList.find {
                        TextUtils.equals(matchId, it.id)
                    }?.let {
                        navMatchDetailPage(it)
                    }
                },
                    clickListenerBet = { matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap ->
                        if (mIsEnabled) {
                            avoidFastDoubleClick()
                            addOddsDialog(
                                matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap
                            )
                        }
                    },
                    clickListenerQuickCateTab = { matchOdd, quickPlayCate ->

                    },
                    clickListenerQuickCateClose = {

                    },
                    clickListenerFavorite = { matchId ->
                        matchId?.let {
                            viewModel.pinFavorite(FavoriteType.MATCH, it)
                        }
                    },
                    clickListenerStatistics = { matchId ->
                        if (viewModel.checkLoginStatus()) {
                            data.forEach {
                                it.matchOdds.forEach {
                                    if (TextUtils.equals(matchId, it.matchInfo?.id)) {
                                        navMatchDetailPage(it.matchInfo)
                                        return@forEach
                                    }
                                }
                            }
                        }
                    },
                    refreshListener = { matchId ->
                        loading()
//                    viewModel.refreshGame(
//                        matchType,
//                        listOf(),
//                        listOf(matchId)
//                    )
                    },
                    clickLiveIconListener = { matchId, matchInfoList, _, liveVideo ->
                        if (viewModel.checkLoginStatus()) {
                            matchInfoList.find {
                                TextUtils.equals(matchId, it.id)
                            }?.let {
                                navMatchDetailPage(it)
                            }
                        }
                    },
                    clickAnimationIconListener = { matchId, matchInfoList, _, liveVideo ->
                        if (viewModel.checkLoginStatus()) {
                            matchInfoList.find {
                                TextUtils.equals(matchId, it.id)
                            }?.let {
                                navMatchDetailPage(it)
                            }
                        }
                    },
                    clickCsTabListener = { playCate, matchOdd ->
                        data.forEachIndexed { index, l ->
                            l.matchOdds.find { m ->
                                m == matchOdd
                            }?.let {
                                it.csTabSelected = playCate
                                updateLeagueBySelectCsTab(index, matchOdd)
                            }
                        }
                    })
        }
    }

    private fun navMatchDetailPage(matchInfo: MatchInfo?) = matchInfo?.let {
        SportDetailActivity.startActivity(requireContext(), it, matchType)
    }


    override fun loading() {
        stopTimer()
    }

    override fun hideLoading() {
        if (timer == null) startTimer()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mView = inflater.inflate(R.layout.fragment_sport_list, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBusUtil.targetLifecycle(this)
        //打开指定球类
        viewModel.matchType = matchType
        gameType = arguments?.getString("gameType")
        gameType?.let { viewModel.gameType = it }
        setupSportTypeList()
        setupToolbar()
        setupGameRow()
        setupGameListView()
        initObserve()
        initSocketObserver()
        viewModel.cleanGameHallResult()
        sportLeagueAdapter.setPreloadItem()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            //receiver.oddsChangeListener為activity底下共用, 顯示當前畫面時需重新配置listener
            receiver.oddsChangeListener = mOddsChangeListener
        }
    }

    private fun setupSportTypeList() = sport_type_list.run {
        layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
        //波胆不需要显示球类
        isVisible = matchType != MatchType.CS
        adapter = gameTypeAdapter
        removeItemDecorations()
    }

    private fun setupToolbar() {
        iv_calendar.apply {
            visibility = when (matchType) {
                MatchType.EARLY, MatchType.CS -> View.VISIBLE
                else -> View.GONE
            }

            setOnClickListener {
                val newSelectedStatus = !isSelected
                mCalendarSelected = newSelectedStatus
                isSelected = newSelectedStatus

                view?.game_filter_type_list?.isVisible = iv_calendar.isSelected
            }
        }

        lin_filter.setOnClickListener {
            if (TextUtils.isEmpty(gameType)) {
                return@setOnClickListener
            }

            if (matchType == MatchType.EARLY || matchType == MatchType.CS || matchType == MatchType.PARLAY) {
                val timeRangeParams = viewModel.getCurrentTimeRangeParams()
                LeagueSelectActivity.start(
                    requireContext(),
                    gameType!!,
                    matchType,
                    timeRangeParams?.startTime,
                    timeRangeParams?.endTime,
                    leagueIdList
                )
                return@setOnClickListener
            }

            LeagueSelectActivity.start(
                requireContext(),
                gameType!!,
                matchType,
                null,
                null,
                leagueIdList
            )
        }
        iv_arrow.isSelected = true
        iv_arrow.setOnClickListener {
            iv_arrow.isSelected = !iv_arrow.isSelected
            sportLeagueAdapter.data.forEach {
                it.unfoldStatus =
                    if (iv_arrow.isSelected) FoldState.UNFOLD.code else FoldState.FOLD.code
            }
            sportLeagueAdapter.notifyDataSetChanged()
        }
    }


    private fun setupGameRow() {
        game_filter_type_list.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            this.adapter = dateAdapter
            removeItemDecorations()
            addItemDecoration(
                SpaceItemDecoration(
                    context, R.dimen.recyclerview_item_dec_spec_date
                )
            )
        }
        when (matchType) {
            MatchType.EARLY, MatchType.CS -> {
                mCalendarSelected = false
                iv_calendar.isVisible = true
                game_filter_type_list.isVisible = false
            }

            else -> {
                mCalendarSelected = false
                iv_calendar.isVisible = false
                game_filter_type_list?.isVisible = iv_calendar.isSelected
            }
        }
    }

    private fun setupGameListView() = game_list.run {
        if (this.layoutManager != null) {
            if (viewModel.getMatchCount(matchType) < 1) {
                sportLeagueAdapter.removePreloadItem()
            } else {
                sportLeagueAdapter.setPreloadItem()
            }
            return@run
        }

        this.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = sportLeagueAdapter
        addItemDecoration(VerticalDecoration(context, R.drawable.bg_divide_light_blue_8))
        addScrollWithItemVisibility(onScrolling = {
            unSubscribeChannelHallAll()
        }, onVisible = {
            if (!(adapter is SportLeagueAdapter) || sportLeagueAdapter.data.isNullOrEmpty()) {
                return@addScrollWithItemVisibility
            }

            it.forEach { p ->
                Log.d(
                    "[subscribe]", "訂閱 ${sportLeagueAdapter.data[p.first].league.name} -> "
                            + "${sportLeagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.homeName} vs "
                            + "${sportLeagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.awayName}"
                )

                subscribeChannelHall(
                    sportLeagueAdapter.data[p.first].gameType?.key,
                    sportLeagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.id
                )
            }
        })

        sportLeagueAdapter.setPreloadItem()
    }


    private fun initObserve() {
        viewModel.notifyLogin.observe(viewLifecycleOwner) {
            (activity as MainTabActivity).showLoginNotify()
        }

        viewModel.showErrorDialogMsg.observe(this.viewLifecycleOwner) {
            if (it == null || it.isBlank() || requireContext() == null) {
                return@observe
            }

            val dialog = CustomAlertDialog(requireContext())
            dialog.setTitle(resources.getString(R.string.prompt))
            dialog.setMessage(it)
            dialog.setTextColor(R.color.color_E44438_e44438)
            dialog.setNegativeButtonText(null)
            dialog.setPositiveClickListener {
                viewModel.resetErrorDialogMsg()
                dialog.dismiss()
                back()
            }
            dialog.setCanceledOnTouchOutside(false)
            dialog.isCancelable = false
            dialog.show(childFragmentManager, null)
        }

        viewModel.sportMenuResult.distinctUntilChanged().observe(this.viewLifecycleOwner) {
            when (matchType) {
                MatchType.IN_PLAY -> {
                    mutableListOf<Item>(
                        Item(
                            GameType.ALL.key,
                            GameType.FT.name,
                            num = it?.sportMenuData?.menu?.inPlay?.num ?: 0,
                            play = listOf(),
                            sortNum = 0
                        ),
                    ).apply {
                        addAll(it?.sportMenuData?.menu?.inPlay?.items ?: listOf())
                    }.let {
                        updateSportType(it)
                    }
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

            it?.let { (parentFragment as SportFragment).updateSportMenuResult(it) }
        }

        viewModel.curDate.observe(this.viewLifecycleOwner) { dateAdapter.data = it }

        viewModel.curDatePosition.observe(this.viewLifecycleOwner) {
            var position = viewModel.tempDatePosition
            position = if (position != 0) position else it
        }

        viewModel.userInfo.observe(this.viewLifecycleOwner) { userInfo ->
            if (game_list.adapter is SportLeagueAdapter) {
                sportLeagueAdapter.discount = userInfo?.discount ?: 1.0F
            }
        }

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success && !oddsListResult.oddsListData?.leagueOdds.isNullOrEmpty()) {
//                    sportLeagueAdapter.removePreloadItem()
                    var mLeagueOddList = (oddsListResult.oddsListData?.leagueOddsFilter
                        ?: oddsListResult.oddsListData?.leagueOdds)?.toMutableList()
                        ?: mutableListOf()
//                    val gameType = GameType.getGameType(oddsListResult.oddsListData?.sport?.code)
                    if (mLeagueOddList.isNotEmpty()) {
                        sportLeagueAdapter.data = mLeagueOddList.onEach { leagueOdd ->
                            // 將儲存的賠率表指定的賽事列表裡面
                            val leagueOddFromMap = leagueOddMap[leagueOdd.league.id]
                            leagueOddFromMap?.let {
                                leagueOdd.matchOdds.forEach { mMatchOdd ->
                                    mMatchOdd.oddsMap =
                                        leagueOddFromMap.matchOdds.find { matchOdd -> mMatchOdd.matchInfo?.id == matchOdd.matchInfo?.id }?.oddsMap
                                }
                            }
                            leagueOdd.gameType =
                                GameType.getGameType(leagueOdd.matchOdds.firstOrNull()?.matchInfo?.gameType)
                        }.toMutableList()
                    } else {
                        sportLeagueAdapter.data = mLeagueOddList
                        // Todo: MatchType.OTHER 要顯示無資料與隱藏篩選清單
                    }
                    mLeagueOddList.forEach { leagueOdd ->
                        unSubscribeChannelHall(leagueOdd)
                    }

                    sportLeagueAdapter.limitRefresh()
                    // TODO 這裡要確認是否有其他地方重複呼叫
                    Log.d("Hewie", "observe => OddsListGameHallResult")

                    firstVisibleRange()

                } else {
                    sportLeagueAdapter.removePreloadItem()
                }
            } ?: run {
                sportLeagueAdapter.setPreloadItem()
            }
            hideLoading()
        }


        //當前玩法無賽事
        viewModel.isNoEvents.distinctUntilChanged().observe(this.viewLifecycleOwner) {
            sport_type_list.isVisible = !it && matchType != MatchType.CS
            iv_calendar.isVisible =
                (matchType == MatchType.EARLY || matchType == MatchType.CS) && !it
            sportLeagueAdapter.removePreloadItem()
            hideLoading()
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let { betInfoList ->
                sportLeagueAdapter.betInfoList = betInfoList
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                sportLeagueAdapter.oddsType = oddsType
            }
        }


        viewModel.favorLeagueList.observe(this.viewLifecycleOwner) {

        }

        viewModel.favorMatchList.observe(this.viewLifecycleOwner) {
            sportLeagueAdapter.data.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.isFavorite = it.contains(matchOdd.matchInfo?.id)
                }
            }

            updateAllGameList()
        }

        viewModel.leagueFilterList.observe(this.viewLifecycleOwner) { leagueList ->
//            mLeagueIsFiltered = leagueList.isNotEmpty()
//            sport_type_list.visibility = if (mLeagueIsFiltered) View.GONE else View.VISIBLE
        }
    }


    private val leagueOddMap = HashMap<String, LeagueOdd>()
    private fun initSocketObserver() {

        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }

            if (it == ServiceConnectStatus.CONNECTED) {
                viewModel.switchMatchType(matchType = matchType)
                subscribeSportChannelHall()
            } else {
                stopTimer()
            }
        }

        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }

            val matchStatusChangeEvent = it!!

            val isFinished =
                matchStatusChangeEvent.matchStatusCO?.status == GameMatchStatus.FINISH.value
            val matchId = matchStatusChangeEvent.matchStatusCO?.matchId
            sportLeagueAdapter.data.toList().forEachIndexed { index, leagueOdd ->
                if (isFinished) {
                    leagueOdd.matchOdds.toList().find { m ->
                        m.matchInfo?.id == matchId
                    }?.let { mo ->
                        leagueOdd.matchOdds.remove(mo)
                        if (leagueOdd.matchOdds.size > 0) {
                            sportLeagueAdapter.notifyItemChanged(index)
                        } else {
                            unSubscribeChannelHall(leagueOdd)
                            sportLeagueAdapter.data.remove(leagueOdd)
                            sportLeagueAdapter.notifyItemRemoved(index)
                        }
                    }
                } else {
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        if (SocketUpdateUtil.updateMatchStatus(
                                leagueOdd.gameType?.key,
                                matchOdd,
                                matchStatusChangeEvent,
                                context
                            )
                            && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
                        ) {
                            sportLeagueAdapter.updateMatch(index, matchOdd)
                        }
                    }
                }
            }

        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            if (it == null || game_list.adapter !is SportLeagueAdapter) {
                return@observe
            }

            val matchClockEvent = it
            val leagueOdds = sportLeagueAdapter.data
            leagueOdds.forEachIndexed { leagueIndex, leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->

                    if (SocketUpdateUtil.updateMatchClock(matchOdd, matchClockEvent)
                        && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
                    ) {
                        updateMatch(leagueIndex, matchOdd)
                    }
                }
            }
        }

        setupOddsChangeListener()

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            if (it == null || game_list.adapter !is SportLeagueAdapter) {
                return@observe
            }
            val matchOddsLockEvent = it
            val leagueOdds = sportLeagueAdapter.data

            leagueOdds.forEachIndexed { leagueIndex, leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    if (SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                        && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
                    ) {
                        updateMatch(leagueIndex, matchOdd)
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            if (it == null || game_list.adapter !is SportLeagueAdapter) {
                return@observe
            }

            val leagueOdds = sportLeagueAdapter.data
            val globalStopEvent = it
            leagueOdds.forEachIndexed { leagueIndex, leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    if (SocketUpdateUtil.updateOddStatus(matchOdd, globalStopEvent)
                        && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
                    ) {
                        //暫時不處理 防止過多更新
                        updateMatch(leagueIndex, matchOdd)
                    }
                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }
            unSubscribeChannelHallAll()
            if (game_list.adapter is SportLeagueAdapter) {
                sportLeagueAdapter.data.forEach { leagueOdd ->
                    subscribeChannelHall(leagueOdd)
                }
            }
        }

        //distinctUntilChanged -> 短時間內收到相同leagueChangeEvent僅會執行一次
        receiver.leagueChange.distinctUntilChanged().observe(this.viewLifecycleOwner) {
//            it?.let { leagueChangeEvent ->
//                viewModel.checkGameInList(
//                    leagueChangeEvent = leagueChangeEvent,
//                )
//                //待優化: 應有個暫存leagueChangeEvent的機制，確認後續流程更新完畢，再處理下一筆leagueChangeEvent，不過目前後續操作並非都是suspend，需重構後續流程
//            }
        }

        receiver.closePlayCate.observe(this.viewLifecycleOwner) { event ->
            event?.peekContent()?.let {
                if (gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code != it.gameType) return@observe
                sportLeagueAdapter.data.closePlayCate(it)
                sportLeagueAdapter.notifyDataSetChanged()
            }
        }
    }

    fun setupOddsChangeListener() {
        if (isAdded) {
            receiver.oddsChangeListener = mOddsChangeListener
        }
    }

    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            val leagueOdds = sportLeagueAdapter.data
            leagueOdds.forEachIndexed { leagueIndex, leagueOdd ->
                leagueOdd.matchOdds.forEachIndexed { index, matchOdd ->
                    if (SocketUpdateUtil.updateMatchOdds(context, matchOdd, oddsChangeEvent)) {
                        leagueOddMap[leagueOdd.league.id] = leagueOdd
                        updateMatch(leagueIndex, matchOdd)
                        updateBetInfo(leagueOdd, oddsChangeEvent)
                    }
                }
            }
        }
    }

    private fun updateLeague(index: Int, leagueOdd: LeagueOdd) {
        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
            sportLeagueAdapter.updateLeague(index, leagueOdd)
        }
    }

    private fun updateMatch(index: Int, matchOdd: MatchOdd) {
        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
            sportLeagueAdapter.updateMatch(index, matchOdd)
        }
    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(leagueOdd: LeagueOdd, oddsChangeEvent: OddsChangeEvent) {
        if (getBetListPageVisible()) {
            return
        }

        //尋找是否有加入注單的賠率項
        if (leagueOdd.matchOdds.filter { matchOdd ->
                matchOdd.matchInfo?.id == oddsChangeEvent.eventId
            }.any { matchOdd ->
                matchOdd.oddsMap?.values?.any { oddList ->
                    oddList?.any { odd ->
                        odd?.isSelected == true
                    } == true
                } == true
            }) {
            viewModel.updateMatchOdd(oddsChangeEvent)
        }
    }


    private fun updateAllGameList() {
        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
            sportLeagueAdapter.data.forEachIndexed { index, leagueOdd ->
                sportLeagueAdapter.updateLeague(index, leagueOdd)
            }
        }
    }

    private fun MutableList<LeagueOdd>.sortOddsMap() {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { MatchOdd ->
                MatchOdd.oddsMap?.forEach { (_, value) ->
                    if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                        value?.sortBy {
                            it?.marketSort
                        }
                    }
                }
            }
        }
    }

    private fun updateSportType(gameTypeList: List<Item>) {
        if (gameTypeList.isEmpty()) {
            sport_type_list.isVisible = matchType != MatchType.CS
            iv_calendar.isVisible = matchType == MatchType.EARLY || matchType == MatchType.CS
            sportLeagueAdapter.removePreloadItem()
            hideLoading()
            return
        }
        //处理默认不选中的情况
        if (gameType.isNullOrEmpty()) {
            (gameTypeList.find { it.num > 0 } ?: gameTypeList.first()).let {
                it.isSelected = true
                gameType = it.code
                viewModel.switchGameType(it)
            }
        } else {
            (gameTypeList.find { it.code == gameType } ?: gameTypeList.first()).let {
                if (!it.isSelected) {
                    it.isSelected = true
                    viewModel.switchGameType(it)
                }
            }
        }
        //全部球类tab不支持联赛筛选
        lin_filter.isVisible = gameType != GameType.ALL.key
        gameTypeAdapter.apply {
            dataSport = gameTypeList
        }
        (sport_type_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
            sport_type_list,
            RecyclerView.State(),
            gameTypeAdapter.dataSport.indexOfFirst { it.isSelected })
        //post待view繪製完成
        sport_type_list?.post {
            if (gameTypeList.isEmpty()) {
                sport_type_list?.isVisible = false
                iv_calendar?.isVisible = false
                game_filter_type_list?.isVisible = false
            } else {
                sport_type_list?.isVisible = matchType != MatchType.CS
                iv_calendar?.apply {
                    visibility = when (matchType) {
                        MatchType.EARLY, MatchType.CS -> View.VISIBLE
                        else -> View.GONE
                    }
                    isSelected = mCalendarSelected
                }
                game_filter_type_list?.isVisible = iv_calendar.isSelected
            }
        }
    }

    private fun navThirdGame(thirdGameCategory: ThirdGameCategory) {
        val intent = Intent(activity, MainActivity::class.java).putExtra(
            MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory
        )
        startActivity(intent)
    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    ) {

        var gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)
        if (gameType == null || matchInfo == null) {
            return
        }
        if (gameType == GameType.ALL) {
            gameType = GameType.getGameType(matchInfo.gameType)
        }
        val fastBetDataBean = FastBetDataBean(
            matchType = matchType,
            gameType = gameType!!,
            playCateCode = playCateCode,
            playCateName = playCateName,
            matchInfo = matchInfo,
            matchOdd = null,
            odd = odd,
            subscribeChannelType = ChannelType.HALL,
            betPlayCateNameMap = betPlayCateNameMap,
        )
        (activity as MainTabActivity).setupBetData(fastBetDataBean)
    }


    private fun subscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            if (leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
                subscribeChannelHall(leagueOdd.gameType?.key, matchOdd.matchInfo?.id)
            } else {
                unSubscribeChannelHall(leagueOdd.gameType?.key, matchOdd.matchInfo?.id)
            }
        }
    }

    private fun unSubscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            if (leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
                unSubscribeChannelHall(leagueOdd.gameType?.key, matchOdd.matchInfo?.id)
            }
        }
    }

    private fun unSubscribeLeagueChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            unSubscribeChannelHall(
                leagueOdd.gameType?.key, matchOdd.matchInfo?.id
            )
        }
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
        game_list.adapter = null
        stopTimer()
        unSubscribeChannelHallAll()
        unSubscribeChannelHallSport()
    }

    @Subscribe
    fun onSelectLeague(leagueList: List<League>) {
        viewModel.filterLeague(leagueList)
        leagueIdList.clear()
        leagueList.forEach { leagueIdList.add(it.id) }
        viewModel.getGameHallList(
            isReloadDate = true,
            isReloadPlayCate = false,
            isLastSportType = true,
            leagueIdList = leagueIdList
        )
    }

    open fun getCurGameType(): GameType {
        return GameType.getGameType(gameType) ?: GameType.ALL
    }

    private fun firstVisibleRange(delay: Long = 100) = game_list.postDelayed({
        if (game_list == null) {
            return@postDelayed
        }
        val adapter = game_list.adapter as SportLeagueAdapter
        if (adapter.data.isNullOrEmpty()) {
            return@postDelayed
        }

        game_list.getVisibleRangePosition().forEach { leaguePosition ->
            val view =
                game_list.layoutManager?.findViewByPosition(leaguePosition) ?: return@postDelayed

            val viewHolder = game_list.getChildViewHolder(view)
            if (viewHolder is SportLeagueAdapter.ItemViewHolder) {
                viewHolder.itemView.league_odd_list.getVisibleRangePosition()
                    .forEach { matchPosition ->
                        if (leaguePosition < adapter.data.size) {

                            val leagueOdd = adapter.data.getOrNull(leaguePosition)
                            val matchOdd = leagueOdd?.matchOdds?.getOrNull(matchPosition)

                            Log.d(
                                "[subscribe]", "訂閱 ${leagueOdd?.league?.name} -> " +
                                        "${matchOdd?.matchInfo?.homeName} vs " +
                                        "${matchOdd?.matchInfo?.awayName}"
                            )

                            subscribeChannelHall(leagueOdd?.gameType?.key, matchOdd?.matchInfo?.id)
                        }
                    }
            }
        }
    }, delay)
}