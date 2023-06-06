package org.cxct.sportlottery.ui.sport.list

import android.annotation.SuppressLint
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
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_sport_list.*
import kotlinx.android.synthetic.main.fragment_sport_list.view.*
import kotlinx.android.synthetic.main.item_league.view.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.TimeRangeEvent
import org.cxct.sportlottery.common.extentions.rotationAnimation
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.sport.SportFragment
import org.cxct.sportlottery.ui.sport.common.*
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.ui.sport.favorite.LeagueListener
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager
import org.greenrobot.eventbus.Subscribe
import java.util.*
import java.util.Date
import kotlin.collections.ArrayList

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
open class SportListFragment :
    BaseBottomNavigationFragment<SportListViewModel>(SportListViewModel::class) {


    override fun layoutId() = R.layout.fragment_sport_list

    //    private val args: GameV3FragmentArgs by navArgs()
    private val matchType by lazy {
        (arguments?.getSerializable("matchType") as MatchType?) ?: MatchType.IN_PLAY
    }
    private var gameType: String? = null
        set(value) {
            if (!Objects.equals(value, field)) { // 清除赛选条件
                viewModel.selectMatchIdList = arrayListOf()
            }
            field = value
        }

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {
//                val time1 = System.currentTimeMillis()
//                Timber.d("当前时间")
                if (!it.isSelected) {
                    //切換球種，清除日期記憶
                    viewModel.tempDatePosition = 0
                }
                gameType = it.code
                dataSport.forEach { item ->
                    item.isSelected = (item.code == gameType)
                }
                notifyDataSetChanged()
                viewModel.cleanGameHallResult()
                sportLeagueAdapter.setPreloadItem()
                (sport_type_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(sport_type_list,
                    RecyclerView.State(),
                    dataSport.indexOfFirst { it.isSelected })
                //切換球種後要重置位置
                loading()
                clearSubscribeChannels()
                viewModel.switchGameType(matchType, it)
                iv_arrow.isSelected = true
                lin_filter.isVisible = gameType != GameType.ALL.key
//                val time2 = System.currentTimeMillis()
//                Timber.d("时间差:${time2 - time1}")
            }

            thirdGameListener = ThirdGameListener {
                navThirdGame(it)
            }
        }
    }


    private val sportLeagueAdapter by lazy {
        SportLeagueAdapter(this, matchType).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            leagueListener = LeagueListener { resubscribeChannel(400) }
            leagueOddListener =
                LeagueOddListener(clickListenerPlayType = { matchId, matchInfoList, _, liveVideo ->
                    matchInfoList.find {
                        TextUtils.equals(matchId, it.id)
                    }?.let {
                        navMatchDetailPage(it)
                    }
                },
                    clickListenerBet = { view, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap ->
                        if (mIsEnabled) {
                            avoidFastDoubleClick()
                            addOddsDialog(
                                view, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap
                            )
                        }
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

    override fun onBindView(view: View) {
        EventBusUtil.targetLifecycle(this)
        arguments?.getString("gameType")?.let {
            gameType = it
            viewModel.gameType = it
        }
        setupSportTypeList()
        setupToolbar()
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

    override fun onStop() {
        super.onStop()
//        printLog("clearSubscribeChannels by onStop")
//        clearSubscribeChannels()
    }

    private fun setupSportTypeList() = sport_type_list.run {
        layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
        //波胆不需要显示球类
        isVisible = matchType != MatchType.CS
        adapter = gameTypeAdapter
        removeItemDecorations()
    }

    var offsetScrollListener: ((Double) -> Unit)? = null

    private fun setupToolbar() {

        appbar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            offsetScrollListener?.invoke(
                (-verticalOffset) / Math.max(
                    1.0, appbar_layout.measuredHeight.toDouble()
                )
            )
        })

        lin_filter.setOnClickListener {
            if (TextUtils.isEmpty(gameType)) {
                return@setOnClickListener
            }

            LeagueSelectActivity.start(
                requireContext(), gameType!!, matchType,  viewModel.selectTimeRangeParams, viewModel.selectMatchIdList
            )
        }
        iv_arrow.isSelected = false
        iv_arrow.setOnClickListener {
            val selected = !iv_arrow.isSelected
            iv_arrow.isSelected = selected
            if (selected) {
                iv_arrow.rotationAnimation(180f, 0)
            } else {
                iv_arrow.rotationAnimation(0f, 0)
            }
            sportLeagueAdapter.data.forEach {
                it.unfoldStatus =
                    if (!iv_arrow.isSelected) FoldState.UNFOLD.code else FoldState.FOLD.code
            }
            sportLeagueAdapter.notifyDataSetChanged()
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
//        addItemDecoration(VerticalDecoration(context, R.drawable.bg_divide_light_blue_8))
        game_list.addOnScrollListener(object : OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState) { // 开始滑动
                    clearSubscribeChannels()
                } else if (RecyclerView.SCROLL_STATE_IDLE == newState) { // 滑动停止
                    resubscribeChannel()
                }
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

            showErrorMsgDialog(it)
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

//            it?.let { (parentFragment as SportFragment).updateSportMenuResult(it) }
        }

        viewModel.userInfo.observe(this.viewLifecycleOwner) { userInfo ->
            if (game_list.adapter is SportLeagueAdapter) {
                sportLeagueAdapter.discount = userInfo?.discount ?: 1.0F
            }
        }

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner) {
            if (gameType != it.tag) {
                return@observe
            }

            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success && !oddsListResult.oddsListData?.leagueOdds.isNullOrEmpty()) {
//                    sportLeagueAdapter.removePreloadItem()
                    var mLeagueOddList = (oddsListResult.oddsListData?.leagueOddsFilter
                        ?: oddsListResult.oddsListData?.leagueOdds)?.toMutableList()
                        ?: mutableListOf()
//                    val gameType = GameType.getGameType(oddsListResult.oddsListData?.sport?.code)
                    if (mLeagueOddList.isNotEmpty()) {
                        sportLeagueAdapter.data = mLeagueOddList.onEach { leagueOdd ->
//                            // 將儲存的賠率表指定的賽事列表裡面
//                            val leagueOddFromMap = leagueOddMap[leagueOdd.league.id]
//                            leagueOddFromMap?.let {
//                                leagueOdd.matchOdds.forEach { mMatchOdd ->
//                                    mMatchOdd.oddsMap =
//                                        leagueOddFromMap.matchOdds.find { matchOdd -> mMatchOdd.matchInfo?.id == matchOdd.matchInfo?.id }?.oddsMap
//
//
//                                }
//                            }
                            leagueOdd.gameType =
                                GameType.getGameType(leagueOdd.matchOdds.firstOrNull()?.matchInfo?.gameType)
                        }.toMutableList()


                    } else {
                        sportLeagueAdapter.data = mLeagueOddList
                        // Todo: MatchType.OTHER 要顯示無資料與隱藏篩選清單
                    }

                    sportLeagueAdapter.limitRefresh()
                    // TODO 這裡要確認是否有其他地方重複呼叫
//                    Timber.tag("Hewie").d("observe => OddsListGameHallResult")

                    resubscribeChannel()
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
//            sport_type_list.isVisible = !it && matchType != MatchType.CS
            //无赛事或者为波胆的时候不显示
            ll_sport_type.isVisible = !(it || matchType == MatchType.CS)
            if (it) {
                sportLeagueAdapter.removePreloadItem()
            }
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

//
//        viewModel.favorLeagueList.observe(this.viewLifecycleOwner) {
//
//        }

        viewModel.favorMatchList.observe(this.viewLifecycleOwner) {
            sportLeagueAdapter.data.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.isFavorite = it.contains(matchOdd.matchInfo?.id)
                }
            }

            updateAllGameList()
        }

//        viewModel.leagueFilterList.observe(this.viewLifecycleOwner) { leagueList ->
//            mLeagueIsFiltered = leagueList.isNotEmpty()
//            sport_type_list.visibility = if (mLeagueIsFiltered) View.GONE else View.VISIBLE
//        }
    }


    //    private val leagueOddMap = HashMap<String, LeagueOdd>()
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
                                leagueOdd.gameType?.key, matchOdd, matchStatusChangeEvent, context
                            ) && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
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

                    if (SocketUpdateUtil.updateMatchClock(
                            matchOdd,
                            matchClockEvent
                        ) && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
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
                    if (SocketUpdateUtil.updateOddStatus(
                            matchOdd,
                            matchOddsLockEvent
                        ) && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
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
                    if (SocketUpdateUtil.updateOddStatus(
                            matchOdd,
                            globalStopEvent
                        ) && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
                    ) {
                        //暫時不處理 防止過多更新
                        updateMatch(leagueIndex, matchOdd)
                    }
                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) { //開啟允許投注
            if (it == null) {
                return@observe
            }

            resubscribeChannel()
        }

        //distinctUntilChanged -> 短時間內收到相同leagueChangeEvent僅會執行一次
//        receiver.leagueChange.distinctUntilChanged().observe(this.viewLifecycleOwner) {
//            it?.let { leagueChangeEvent ->
//                viewModel.checkGameInList(
//                    leagueChangeEvent = leagueChangeEvent,
//                )
//                //待優化: 應有個暫存leagueChangeEvent的機制，確認後續流程更新完畢，再處理下一筆leagueChangeEvent，不過目前後續操作並非都是suspend，需重構後續流程
//            }
//        }

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
                    if (SocketUpdateUtil.updateMatchOdds(
                            context, matchOdd, oddsChangeEvent, matchType
                        )
                    ) {
//                        leagueOddMap[leagueOdd.league.id] = leagueOdd
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

    private fun updateSportType(gameTypeList: List<Item>) {

        if (gameTypeList.isEmpty()) {
            sport_type_list.isVisible = matchType != MatchType.CS
            sportLeagueAdapter.removePreloadItem()
            hideLoading()
            return
        }
        //处理默认不选中的情况
        if (gameType.isNullOrEmpty()) {
            (gameTypeList.find { it.num > 0 } ?: gameTypeList.first()).let {
                it.isSelected = true
                gameType = it.code
                sportLeagueAdapter.setPreloadItem()
                viewModel.switchGameType(matchType, it)
            }
        } else {
            (gameTypeList.find { it.code == gameType } ?: gameTypeList.first()).let {
                gameType = it.code
                if (!it.isSelected) {
                    it.isSelected = true
                    sportLeagueAdapter.setPreloadItem()
                    viewModel.switchGameType(matchType, it)
                }
            }
        }
        //全部球类tab不支持联赛筛选
        lin_filter.isVisible = gameType != GameType.ALL.key
        gameTypeAdapter.dataSport = gameTypeList

        (sport_type_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
            sport_type_list,
            RecyclerView.State(),
            gameTypeAdapter.dataSport.indexOfFirst { it.isSelected })
        //post待view繪製完成
        sport_type_list?.post {
            if (gameTypeList.isEmpty()) {
                sport_type_list?.isVisible = false
            } else {
                sport_type_list?.isVisible = matchType != MatchType.CS
            }
        }
    }

    private fun navThirdGame(thirdGameCategory: ThirdGameCategory) {
//        val intent = Intent(activity, MainActivity::class.java).putExtra(
//            MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory
//        )
//        startActivity(intent)
    }

    private fun addOddsDialog(
        view: View,
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
        (activity as MainTabActivity).setupBetData(fastBetDataBean, view)
    }

    private fun unSubscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            if (leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
                unSubscribeChannelHall(leagueOdd.gameType?.key, matchOdd.matchInfo?.id)
            }
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
        offsetScrollListener = null
        stopTimer()
        clearSubscribeChannels()
        unSubscribeChannelHallSport()
    }

    @Subscribe
    fun onSelectMatch(matchIdList: ArrayList<String>) {
        viewModel.selectMatchIdList = matchIdList
    }

    @Subscribe
    fun onSelectDate(timeRangeEvent: TimeRangeEvent) {
        viewModel.selectTimeRangeParams = object : TimeRangeParams {
            override val startTime: String
                get() = timeRangeEvent.startTime
            override val endTime: String
                get() = timeRangeEvent.endTime
        }
    }

    open fun getCurGameType(): GameType {
        return GameType.getGameType(gameType) ?: GameType.ALL
    }

    private val subscribedChannel = mutableListOf<Pair<String?, String?>>()
    private val subscribeHandler = Handler(Looper.getMainLooper())
    private var channelOk = false

    private fun subscribeChannel(gameType: String?, eventId: String?) {
        subscribedChannel.add(Pair(gameType, eventId))
        subscribeChannelHall(gameType, eventId)
    }

    private fun clearSubscribeChannels() {
        channelOk = false
        if (subscribedChannel.size > 0) {
            unSubscribeChannelHallAll()
            subscribedChannel.clear()
        }
        subscribeHandler.removeCallbacksAndMessages(null)
    }

    private fun needDalay(): Boolean {
        if (sportLeagueAdapter.data.size < 1) {
            return false
        }

        val view = game_list.layoutManager?.findViewByPosition(0) ?: return false
        val viewHolder = game_list.getChildViewHolder(view)
        return (viewHolder !is SportLeagueAdapter.ItemViewHolder)
    }

    private fun resubscribeChannel(delay: Long = 0) {
        clearSubscribeChannels()
        if (!isVisible) {
            return
        }
        val adapter = game_list.adapter as SportLeagueAdapter
        if (adapter.data.size > 0) {
            firstVisibleRange(delay)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun firstVisibleRange(delay: Long = 100) = subscribeHandler.postDelayed({
        if (game_list == null || game_list.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
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

                            printLog(
                                "訂閱 ${leagueOdd?.gameType?.key} ${leagueOdd?.league?.name} -> " + "${matchOdd?.matchInfo?.homeName} vs " + "${matchOdd?.matchInfo?.awayName}"
                            )

                            subscribeChannel(leagueOdd?.gameType?.key, matchOdd?.matchInfo?.id)
                        }
                    }
            } else if (isVisible) {
                resubscribeChannel(50)
                return@postDelayed
            }
        }

    }, delay)


    private fun printLog(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e("SportListFragment", msg)
        }
    }
}