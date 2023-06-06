package org.cxct.sportlottery.ui.sport.list

import android.os.Handler
import android.os.Looper
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
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.common.extentions.clean
import org.cxct.sportlottery.common.extentions.rotationAnimation
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.common.*
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectActivity
import org.cxct.sportlottery.ui.sport.list.adapter.SportLeagueAdapter2
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
class SportListFragment2
    : BindingSocketFragment<SportListViewModel, FragmentSportList2Binding>() {

    private var matchType = MatchType.IN_PLAY
    private var gameType: String? = null
        set(value) {
            if (!Objects.equals(value, field)) { // 清除赛选条件
                leagueIdList.clear()
                viewModel.filterLeague(mutableListOf())
            }
            field = value
        }
    private var mCalendarSelected = false //紀錄日期圖示選中狀態
    var leagueIdList = mutableListOf<String>()

    private val gameTypeAdapter by lazy { GameTypeAdapter2(::onGameTypeChanged) }

    private val sportLeagueAdapter2 by lazy {
        SportLeagueAdapter2(matchType, this) { position, view, item ->

        }
    }

    private fun onGameTypeChanged(item: Item, position: Int) {

        Log.e("For Test", "======>>> SportTabViewModel onGameTypeChanged ${item.name}")
        //切換球種，清除日期記憶
        viewModel.tempDatePosition = 0
        //日期圖示選取狀態下，切換球種要重置UI狀態
        gameType = item.code
        viewModel.cleanGameHallResult()
        val layoutManager = binding.sportTypeList.layoutManager as ScrollCenterLayoutManager
        layoutManager.smoothScrollToPosition(binding.sportTypeList, RecyclerView.State(), position)
        clearSubscribeChannels()
        load(item)
        binding.linFilter.isVisible = gameType != GameType.ALL.key
    }

//    private val sportLeagueAdapter by lazy {
//        SportLeagueAdapter(this, matchType).apply {
//            discount = viewModel.userInfo.value?.discount ?: 1.0F
//
//            leagueListener = LeagueListener { resubscribeChannel(400) }
//            leagueOddListener =
//                LeagueOddListener(clickListenerPlayType = { matchId, matchInfoList, _, liveVideo ->
//                    matchInfoList.find {
//                        TextUtils.equals(matchId, it.id)
//                    }?.let {
//                        navMatchDetailPage(it)
//                    }
//                },
//                    clickListenerBet = { view, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap ->
//                        if (mIsEnabled) {
//                            avoidFastDoubleClick()
//                            addOddsDialog(
//                                view, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap
//                            )
//                        }
//                    },
//                    clickListenerFavorite = { matchId ->
//                        matchId?.let {
//                            viewModel.pinFavorite(FavoriteType.MATCH, it)
//                        }
//                    },
//                    clickListenerStatistics = { matchId ->
//                        if (viewModel.checkLoginStatus()) {
//                            data.forEach {
//                                it.matchOdds.forEach {
//                                    if (TextUtils.equals(matchId, it.matchInfo?.id)) {
//                                        navMatchDetailPage(it.matchInfo)
//                                        return@forEach
//                                    }
//                                }
//                            }
//                        }
//                    },
//                    clickLiveIconListener = { matchId, matchInfoList, _, liveVideo ->
//                        if (viewModel.checkLoginStatus()) {
//                            matchInfoList.find {
//                                TextUtils.equals(matchId, it.id)
//                            }?.let {
//                                navMatchDetailPage(it)
//                            }
//                        }
//                    },
//                    clickAnimationIconListener = { matchId, matchInfoList, _, liveVideo ->
//                        if (viewModel.checkLoginStatus()) {
//                            matchInfoList.find {
//                                TextUtils.equals(matchId, it.id)
//                            }?.let {
//                                navMatchDetailPage(it)
//                            }
//                        }
//                    },
//                    clickCsTabListener = { playCate, matchOdd ->
//                        data.forEachIndexed { index, l ->
//                            l.matchOdds.find { m ->
//                                m == matchOdd
//                            }?.let {
//                                it.csTabSelected = playCate
//                                updateLeagueBySelectCsTab(index, matchOdd)
//                            }
//                        }
//                    })
//        }
//    }

    private fun reset() {
        matchType = (arguments?.getSerializable("matchType") as MatchType?) ?: MatchType.IN_PLAY
        gameType = arguments?.getString("gameType")
        viewModel.gameType = gameType ?: GameType.FT.key
        mCalendarSelected = false
        leagueIdList.clear()
        viewModel.sportMenuResult.clean()
        viewModel.filterLeague(mutableListOf())
        gameTypeAdapter.setNewInstance(null)
        sportLeagueAdapter2.setNewInstance(null)
        clearSubscribeChannels()
        setupSportTypeList()
        setupToolbarStatus()
    }

    fun reload() {
        reset()
        viewModel.loadMatchType(matchType)
    }

    private fun navMatchDetailPage(matchInfo: MatchInfo?) = matchInfo?.let {
        SportDetailActivity.startActivity(requireContext(), it, matchType)
    }

    override fun onInitView(view: View){
        initToolbar()
        initSportTypeList()
        initGameListView()
    }

    override fun onBindViewStatus(view: View) {

        Log.e("For Test", "======>>> SportTabViewModel SportListFragment ${viewModel}")
        reset()
        setupOddsChangeListener()
        EventBusUtil.targetLifecycle(this)
        initObserve()
        initSocketObserver()
    }

    override fun onInitData() {
        viewModel.loadMatchType(matchType)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            //receiver.oddsChangeListener為activity底下共用, 顯示當前畫面時需重新配置listener
            setupOddsChangeListener()
            resubscribeChannel(if (needDalay()) 80 else 0)
        } else {
            clearSubscribeChannels()
        }
    }

    override fun onResume() {
        super.onResume()
        resubscribeChannel(if (needDalay()) 80 else 0)
    }

    private fun initToolbar()  = binding.run {

        appbarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            offsetScrollListener?.invoke(
                (-verticalOffset) / Math.max(
                    1.0, appbarLayout.measuredHeight.toDouble()
                )
            )
        })

        linFilter.setOnClickListener {
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
                requireContext(), gameType!!, matchType, null, null, leagueIdList
            )
        }

        ivArrow.bindExpanedAdapter(sportLeagueAdapter2)
    }

    private fun initSportTypeList() = binding.run {
        sportTypeList.layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        sportTypeList.edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
    }

    private fun initGameListView() = binding.gameList.run {
        layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        sportLeagueAdapter2.setPreloadItem()
        adapter = sportLeagueAdapter2
        addOnScrollListener(object : OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState) { // 开始滑动
                    clearSubscribeChannels()
                } else if (RecyclerView.SCROLL_STATE_IDLE == newState) { // 滑动停止
                    resubscribeChannel()
                }
            }
        })
    }


    private fun setupSportTypeList() {
        val visiable = matchType != MatchType.CS //波胆不需要显示球类
        binding.sportTypeList.isVisible = visiable
        binding.sportTypeList.adapter = if (visiable) gameTypeAdapter else null
    }

    var offsetScrollListener: ((Double) -> Unit)? = null

    private fun setupToolbarStatus() = binding.run {
        ivArrow.isSelected = false
        ivArrow.rotationAnimation(0f, 0)
        mCalendarSelected = false
    }

    private fun initObserve() = viewModel.run {

        showErrorDialogMsg.observe(this@SportListFragment2) {
            if (it == null || it.isBlank() || requireContext() == null) {
                return@observe
            }

            showErrorMsgDialog(it)
        }

        sportMenuData.observe(this@SportListFragment2) {
            val apiResult = it.first
            updateSportType(it.second)
            if (!apiResult.succeeded()) {
                ToastUtil.showToast(activity, apiResult.msg)
                return@observe
            }

//            it?.let { (parentFragment as SportFragment2).updateSportMenuResult(it) }
        }


        viewModel.oddsListGameHallResult.observe(this@SportListFragment2) {
            Log.e("For Test", "=========>>> oddsListGameHallResult 111 ${requestTag} ${it.tag}")
            Log.e("For Test", "=========>>> oddsListGameHallResult 2222 ${requestTag} ${it.tag}")
            val oddsListData = it.getContentIfNotHandled()?.oddsListData ?: return@observe
            Log.e("For Test", "=========>>> oddsListGameHallResult 33333 ${requestTag} ${it.tag}")
            val leagueOdds: List<LeagueOdd>? = oddsListData.leagueOdds
            if (leagueOdds.isNullOrEmpty()) {
                return@observe
            }

            val mLeagueOddList = (oddsListData.leagueOddsFilter ?: leagueOdds).toMutableList()
            Log.e("For Test", "=========>>> oddsListGameHallResult 44444 ${requestTag} ${it.tag} ${mLeagueOddList.size}")
            sportLeagueAdapter2.setNewInstance(mLeagueOddList as MutableList<BaseNode> )
            resubscribeChannel(80)
        }
//
//
//        //當前玩法無賽事
        viewModel.isNoEvents.distinctUntilChanged().observe(this@SportListFragment2) {
//            sport_type_list.isVisible = !it && matchType != MatchType.CS
            //无赛事或者为波胆的时候不显示
            binding.llSportType.isVisible = !(it || matchType == MatchType.CS)
//            if (it) {
//                sportLeagueAdapter.removePreloadItem()
//            }
        }
//
//        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
//            it.peekContent().let { betInfoList ->
//                sportLeagueAdapter.betInfoList = betInfoList
//            }
//        }
//
//        viewModel.oddsType.observe(this.viewLifecycleOwner) {
//            it?.let { oddsType ->
//                sportLeagueAdapter.oddsType = oddsType
//            }
//        }
//
//        viewModel.favorMatchList.observe(this.viewLifecycleOwner) {
//            sportLeagueAdapter.data.forEach { leagueOdd ->
//                leagueOdd.matchOdds.forEach { matchOdd ->
//                    matchOdd.matchInfo?.isFavorite = it.contains(matchOdd.matchInfo?.id)
//                }
//            }
//
//            updateAllGameList()
//        }

    }


    private fun initSocketObserver() {

        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }

            if (it == ServiceConnectStatus.CONNECTED) {
                Log.e("For Test", "=======>>> initSocketObserver 1111")
                viewModel.switchMatchType(matchType = matchType)
                subscribeSportChannelHall()
            }
        }

//        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
//            if (it == null) {
//                return@observe
//            }
//
//            val matchStatusChangeEvent = it!!
//
//            val isFinished =
//                matchStatusChangeEvent.matchStatusCO?.status == GameMatchStatus.FINISH.value
//            val matchId = matchStatusChangeEvent.matchStatusCO?.matchId
//            sportLeagueAdapter.data.toList().forEachIndexed { index, leagueOdd ->
//                if (isFinished) {
//                    leagueOdd.matchOdds.toList().find { m ->
//                        m.matchInfo?.id == matchId
//                    }?.let { mo ->
//                        leagueOdd.matchOdds.remove(mo)
//                        if (leagueOdd.matchOdds.size > 0) {
//                            sportLeagueAdapter.notifyItemChanged(index)
//                        } else {
//                            unSubscribeChannelHall(leagueOdd)
//                            sportLeagueAdapter.data.remove(leagueOdd)
//                            sportLeagueAdapter.notifyItemRemoved(index)
//                        }
//                    }
//                } else {
//                    leagueOdd.matchOdds.forEach { matchOdd ->
//                        if (SocketUpdateUtil.updateMatchStatus(
//                                leagueOdd.gameType?.key, matchOdd, matchStatusChangeEvent, context
//                            ) && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
//                        ) {
//                            sportLeagueAdapter.updateMatch(index, matchOdd)
//                        }
//                    }
//                }
//            }
//
//        }
//
//        receiver.matchClock.observe(this.viewLifecycleOwner) {
//            if (it == null || binding.gameList.adapter !is SportLeagueAdapter) {
//                return@observe
//            }
//
//            val matchClockEvent = it
//            val leagueOdds = sportLeagueAdapter.data
//            leagueOdds.forEachIndexed { leagueIndex, leagueOdd ->
//                leagueOdd.matchOdds.forEach { matchOdd ->
//
//                    if (SocketUpdateUtil.updateMatchClock(
//                            matchOdd,
//                            matchClockEvent
//                        ) && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
//                    ) {
//                        updateMatch(leagueIndex, matchOdd)
//                    }
//                }
//            }
//        }
//
//        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
//            if (it == null || binding.gameList.adapter !is SportLeagueAdapter) {
//                return@observe
//            }
//            val matchOddsLockEvent = it
//            val leagueOdds = sportLeagueAdapter.data
//
//            leagueOdds.forEachIndexed { leagueIndex, leagueOdd ->
//                leagueOdd.matchOdds.forEach { matchOdd ->
//                    if (SocketUpdateUtil.updateOddStatus(
//                            matchOdd,
//                            matchOddsLockEvent
//                        ) && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
//                    ) {
//                        updateMatch(leagueIndex, matchOdd)
//                    }
//                }
//            }
//        }
//
//        receiver.globalStop.observe(this.viewLifecycleOwner) {
//            if (it == null || binding.gameList.adapter !is SportLeagueAdapter) {
//                return@observe
//            }
//
//            val leagueOdds = sportLeagueAdapter.data
//            val globalStopEvent = it
//            leagueOdds.forEachIndexed { leagueIndex, leagueOdd ->
//                leagueOdd.matchOdds.forEach { matchOdd ->
//                    if (SocketUpdateUtil.updateOddStatus(
//                            matchOdd,
//                            globalStopEvent
//                        ) && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
//                    ) {
//                        //暫時不處理 防止過多更新
//                        updateMatch(leagueIndex, matchOdd)
//                    }
//                }
//            }
//        }
//
//        receiver.producerUp.observe(this.viewLifecycleOwner) { //開啟允許投注
//            if (it == null) {
//                return@observe
//            }
//
//            resubscribeChannel()
//        }
//
//
//        receiver.closePlayCate.observe(this.viewLifecycleOwner) { event ->
//            event?.peekContent()?.let {
//                if (gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code != it.gameType) return@observe
//                sportLeagueAdapter.data.closePlayCate(it)
//                sportLeagueAdapter.notifyDataSetChanged()
//            }
//        }
    }

    private fun setupOddsChangeListener() {
        if (isAdded) {
            receiver.oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener {
                sportLeagueAdapter2.onOddsChangeEvent(it, subscribedMatchOdd)
            }
        }

    }


//    private fun updateMatch(index: Int, matchOdd: MatchOdd) {
//        if (binding.gameList.scrollState == RecyclerView.SCROLL_STATE_IDLE && !binding.gameList.isComputingLayout) {
//            sportLeagueAdapter.updateMatch(index, matchOdd)
//        }
//    }

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
//        if (binding.gameList.scrollState == RecyclerView.SCROLL_STATE_IDLE && !binding.gameList.isComputingLayout) {
//            sportLeagueAdapter.data.forEachIndexed { index, leagueOdd ->
//                sportLeagueAdapter.updateLeague(index, leagueOdd)
//            }
//        }
    }

    private var requestTag: Any? = null
    private fun load(item: Item) {
        if (sportLeagueAdapter2.getCount() > 0) {
            sportLeagueAdapter2.setNewInstance(null)
        }

        Log.e("For Test", "======>>> SportTabViewModel load ${item.name}")
        requestTag = Any().apply { viewModel.switchGameType(matchType, item, this) }
    }

    private fun updateSportType(gameTypeList: List<Item>) {

        if (gameTypeList.isEmpty()) {
            binding.sportTypeList.isVisible = matchType != MatchType.CS
//            binding.ivCalendar.isVisible = matchType == MatchType.EARLY || matchType == MatchType.CS
            hideLoading()
            return
        }
        //处理默认不选中的情况
        if (gameType.isNullOrEmpty()) {
            (gameTypeList.find { it.num > 0 } ?: gameTypeList.first()).let {
                it.isSelected = true
                gameType = it.code
                load(it)
            }
        } else {
            (gameTypeList.find { it.code == gameType } ?: gameTypeList.first()).let {
                gameType = it.code
                if (!it.isSelected) {
                    it.isSelected = true
                    load(it)
                }
            }
        }
        //全部球类tab不支持联赛筛选
        binding.linFilter.isVisible = gameType != GameType.ALL.key
        gameTypeAdapter.setNewInstance(gameTypeList.toMutableList())
        (binding.sportTypeList.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
            binding.sportTypeList,
            RecyclerView.State(),
            gameTypeAdapter.data.indexOfFirst { it.isSelected })

    }


    private fun addOddsDialog(
        view: View,
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    ) {

        var gameType = GameType.getGameType(gameTypeAdapter.currentItem?.code)
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
        (activity as MainTabActivity).setupBetData(fastBetDataBean, view)
    }

    private fun unSubscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            if (leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
                unSubscribeChannelHall(leagueOdd.gameType?.key, matchOdd.matchInfo?.id)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sportLeagueAdapter2.setNewInstance(null)
        offsetScrollListener = null
        clearSubscribeChannels()
        unSubscribeChannelHallSport()
    }

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

    private val subscribedChannel = mutableListOf<Pair<String?, String?>>()
    private val subscribeHandler = Handler(Looper.getMainLooper())

    private fun subscribeChannel(gameType: String?, eventId: String?) {
        subscribedChannel.add(Pair(gameType, eventId))
        subscribeChannelHall(gameType, eventId)
    }

    private fun clearSubscribeChannels() {
        subscribedMatchOdd.clear()
        if (subscribedChannel.size > 0) {
            unSubscribeChannelHallAll()
            subscribedChannel.clear()
        }
        subscribeHandler.removeCallbacksAndMessages(null)
    }

    private fun needDalay(): Boolean {
        if (sportLeagueAdapter2.getCount() < 1) {
            return false
        }

        val view = binding.gameList.layoutManager?.findViewByPosition(0) ?: return false
        val viewHolder = binding.gameList.getChildViewHolder(view)
        return (viewHolder !is SportLeagueAdapter.ItemViewHolder)
    }

    private fun resubscribeChannel(delay: Long = 0) {
        clearSubscribeChannels()
        if (!isVisible) {
            return
        }
        val adapter = binding.gameList.adapter as SportLeagueAdapter2
        if (adapter.getCount() > 0) {
            firstVisibleRange(delay)
        }
    }

    private val subscribedMatchOdd = mutableMapOf<String, Pair<org.cxct.sportlottery.network.odds.list.MatchOdd, Int>>()
    private fun firstVisibleRange(delay: Long = 100) = subscribeHandler.postDelayed({

        if (binding.gameList.scrollState != RecyclerView.SCROLL_STATE_IDLE
            || sportLeagueAdapter2.getCount() < 1
            || binding.gameList.isComputingLayout) {
            return@postDelayed
        }

        sportLeagueAdapter2.doOnVisiableRange { i, baseNode ->
            if (baseNode is MatchOdd) {
                val matchOdd = baseNode as org.cxct.sportlottery.network.odds.list.MatchOdd
                matchOdd.matchInfo?.let {
                    subscribedMatchOdd[it.id] = Pair(matchOdd, i)
                    subscribeChannel(it.gameType, it.id)
                }
            }
        }
    }, delay)


    private fun printLog(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e("SportListFragment", msg)
        }
    }

}