package org.cxct.sportlottery.ui.sport

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_sport_list.*
import kotlinx.android.synthetic.main.fragment_sport_list.view.*
import kotlinx.android.synthetic.main.view_game_tab_match_type_v4.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
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
@SuppressLint("NotifyDataSetChanged", "LogNotTimber")
@RequiresApi(Build.VERSION_CODES.M)
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
                iv_arrow.isSelected = false
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
        SportLeagueAdapter(matchType).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            leagueListener = LeagueListener {
                if (it.unfold == FoldState.FOLD.code) {
                    Log.d("[subscribe]", "取消訂閱 ${it.league.name}")
                    unSubscribeChannelHall(it)
                }
                //目前無法監聽收合動畫
                Handler().postDelayed(
                    { game_list?.firstVisibleRange(this, activity ?: requireActivity()) },
                    400
                )
            }
            leagueOddListener = LeagueOddListener(
                clickListenerPlayType = { matchId, matchInfoList, _, liveVideo ->
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
                            matchInfo,
                            odd,
                            playCateCode,
                            playCateName,
                            betPlayCateNameMap
                        )
                    }
                },
                clickListenerQuickCateTab = { matchOdd, quickPlayCate ->
                    matchOdd.matchInfo?.let {
                        setQuickPlayCateSelected(matchOdd, quickPlayCate)
                    }
                },
                clickListenerQuickCateClose = {
                    clearQuickPlayCateSelected()
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
                }
            )
        }
    }

    private fun navMatchDetailPage(matchInfo: MatchInfo?) {
        matchInfo?.let { it ->
            SportDetailActivity.startActivity(requireContext(),
                matchInfo = it,
                matchType = matchType)
        }
    }

    private var mLeagueOddList = ArrayList<LeagueOdd>()


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
        //打开指定球类
        viewModel.matchType = matchType
        gameType = arguments?.getString("gameType")
        gameType?.let {
            viewModel.gameType = it
        }
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

    private fun setupSportTypeList() {
        sport_type_list.apply {
            layoutManager =
                ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
            //波胆不需要显示球类
            isVisible = matchType != MatchType.CS
            adapter = gameTypeAdapter
            removeItemDecorations()
        }
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

                view?.game_filter_type_list?.visibility = when (iv_calendar.isSelected) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
            }
        }
        lin_filter.setOnClickListener {
            gameType?.let {
                if (matchType == MatchType.EARLY || matchType == MatchType.CS || matchType == MatchType.PARLAY) {
                    val timeRangeParams = viewModel.getCurrentTimeRangeParams()
                    LeagueSelectActivity.start(requireContext(),
                        it,
                        matchType,
                        timeRangeParams?.startTime,
                        timeRangeParams?.endTime,
                        leagueIdList)
                } else {
                    LeagueSelectActivity.start(requireContext(),
                        it,
                        matchType,
                        null,
                        null,
                        leagueIdList)
                }
            }
        }
        iv_arrow.setOnClickListener {
            iv_arrow.isSelected = !iv_arrow.isSelected
            sportLeagueAdapter.data.forEach { it ->
                it.unfold = if (iv_arrow.isSelected) FoldState.FOLD.code else FoldState.UNFOLD.code
            }
            sportLeagueAdapter.notifyDataSetChanged()
        }
    }


    private fun setupGameRow() {
        game_filter_type_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            this.adapter = dateAdapter
            removeItemDecorations()
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_date
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

    private fun setupGameListView() {
        game_list.apply {
            this.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = sportLeagueAdapter
            addItemDecoration(VerticalDecoration(context, R.drawable.bg_divide_light_blue_8))
            addScrollWithItemVisibility(
                onScrolling = {
                    unSubscribeChannelHallAll()
                },
                onVisible = {
                    when (adapter) {
                        is SportLeagueAdapter -> {
                            if (sportLeagueAdapter.data.isNotEmpty()) {
                                it.forEach { p ->
                                    Log.d(
                                        "[subscribe]",
                                        "訂閱 ${sportLeagueAdapter.data[p.first].league.name} -> " +
                                                "${sportLeagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.homeName} vs " +
                                                "${sportLeagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.awayName}"
                                    )
                                    subscribeChannelHall(
                                        sportLeagueAdapter.data[p.first].gameType?.key,
                                        sportLeagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.id
                                    )
                                }
                            }
                        }
                    }
                }
            )
            if (viewModel.getMatchCount(matchType) < 1) {
                sportLeagueAdapter.removePreloadItem()
            } else {
                sportLeagueAdapter.setPreloadItem()
            }
        }
    }


    private fun initObserve() {
        viewModel.notifyLogin.observe(this) {
            (activity as MainTabActivity).showLoginNotify()
        }
        viewModel.showErrorDialogMsg.observe(this.viewLifecycleOwner) {
            if (it != null && it.isNotBlank()) {
                context?.let { context ->
                    val dialog = CustomAlertDialog(context)
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
            }
        }

        viewModel.sportMenuResult.distinctUntilChanged().observe(this.viewLifecycleOwner) {
            when (matchType) {
                MatchType.IN_PLAY -> {
                    mutableListOf<Item>(
                        Item(GameType.ALL.key,
                            GameType.FT.name,
                            num = it?.sportMenuData?.menu?.inPlay?.num ?: 0,
                            play = listOf(),
                            sortNum = 0),
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
            it?.let {
                (parentFragment as SportFragment).updateSportMenuResult(it)
            }
        }

        viewModel.curDate.observe(this.viewLifecycleOwner) {
            dateAdapter.data = it
        }

        viewModel.curDatePosition.observe(this.viewLifecycleOwner) {
            var position = viewModel.tempDatePosition
            position = if (position != 0) position else it
        }

        viewModel.userInfo.observe(this.viewLifecycleOwner) { userInfo ->
            when (game_list.adapter) {
                is SportLeagueAdapter -> {
                    sportLeagueAdapter.discount = userInfo?.discount ?: 1.0F
                }
            }
        }

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success) {
                    sportLeagueAdapter.removePreloadItem()
                    mLeagueOddList.clear()
                    mLeagueOddList.addAll(
                        oddsListResult.oddsListData?.leagueOddsFilter
                            ?: oddsListResult.oddsListData?.leagueOdds ?: listOf()
                    )

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

                    game_list?.firstVisibleRange(sportLeagueAdapter, activity ?: requireActivity())

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
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    viewModel.switchMatchType(matchType = matchType)
                    subscribeSportChannelHall()
                } else {
                    stopTimer()
                }
            }
        }

        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            it?.let { matchStatusChangeEvent ->
                sportLeagueAdapter.data.toList().forEachIndexed { index, leagueOdd ->
                    if (matchStatusChangeEvent.matchStatusCO?.status == GameMatchStatus.FINISH.value) {
                        leagueOdd.matchOdds.toList().find { m ->
                            m.matchInfo?.id == matchStatusChangeEvent.matchStatusCO.matchId
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
                            }
                        }

                        val leagueOdds = sportLeagueAdapter.data

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (SocketUpdateUtil.updateMatchStatus(
                                    gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code,
                                    leagueOdd.matchOdds?.toMutableList(),
                                    matchStatusChangeEvent,
                                    context
                                ) &&
                                leagueOdd.unfold == FoldState.UNFOLD.code
                            ) {
                                if (leagueOdd.matchOdds.isNullOrEmpty()) {
                                    unSubscribeChannelHall(leagueOdd)
                                    sportLeagueAdapter.data.remove(leagueOdd)
                                    sportLeagueAdapter.notifyItemRemoved(index)
                                }
                            }
                }
            }
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                when (game_list.adapter) {
                    is SportLeagueAdapter -> {
                        val leagueOdds = sportLeagueAdapter.data

                        leagueOdds.forEachIndexed { _, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateMatchClock(
                                        matchOdd,
                                        matchClockEvent
                                    )
                                } &&
                                leagueOdd.unfold == FoldState.UNFOLD.code) {
                                //暫時不處理 防止過多更新
                            }
                        }
                    }
                }
            }
        }

        setupOddsChangeListener()

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                when (game_list.adapter) {
                    is SportLeagueAdapter -> {
                        val leagueOdds = sportLeagueAdapter.data

                        leagueOdds.forEachIndexed { _, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                                } && leagueOdd.unfold == FoldState.UNFOLD.code) {
                                //暫時不處理 防止過多更新
                            }
                        }
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->

                when (game_list.adapter) {
                    is SportLeagueAdapter -> {
                        val leagueOdds = sportLeagueAdapter.data
                        leagueOdds.forEachIndexed { _, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(
                                        matchOdd,
                                        globalStopEvent
                                    )
                                } &&
                                leagueOdd.unfold == FoldState.UNFOLD.code
                            ) {
                                //暫時不處理 防止過多更新
                            }
                        }
                    }

                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeChannelHallAll()

                when (game_list.adapter) {
                    is SportLeagueAdapter -> {
                        sportLeagueAdapter.data.forEach { leagueOdd ->
                            subscribeChannelHall(leagueOdd)
                        }
                    }
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
            when (game_list?.adapter) {
                is SportLeagueAdapter -> {

                    val leagueOdds = sportLeagueAdapter.data

                    leagueOdds.sortOddsMap()
                    //翻譯更新

                    leagueOdds.forEach { LeagueOdd ->
                        LeagueOdd.matchOdds.forEach { MatchOdd ->
                            if (MatchOdd.matchInfo?.id == oddsChangeEvent.eventId) {
                                //馬克說betPlayCateNameMap還是由socket更新
                                oddsChangeEvent.betPlayCateNameMap?.let {
                                    MatchOdd.betPlayCateNameMap?.putAll(it)
                                }
                            }
                        }
                    }
                    leagueOdds.forEachIndexed { index, leagueOdd ->
                        if (leagueOdd.matchOdds.any { matchOdd ->
                                SocketUpdateUtil.updateMatchOdds(
                                    context, matchOdd, oddsChangeEvent
                                )
                            } &&
                            leagueOdd.unfold == FoldState.UNFOLD.code
                        ) {
                            leagueOddMap[leagueOdd.league.id] = leagueOdd
                            updateGameList(index, leagueOdd)
                            updateBetInfo(leagueOdd, oddsChangeEvent)
                        } else {
                            updateGameList(index, leagueOdd)
                        }
                    }
                }
            }
        }
    }

    private fun updateGameList(index: Int, leagueOdd: LeagueOdd) {
        sportLeagueAdapter.data[index] = leagueOdd
        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
            sportLeagueAdapter.updateLeague(index, leagueOdd)
        }
    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(leagueOdd: LeagueOdd, oddsChangeEvent: OddsChangeEvent) {
        if (!getBetListPageVisible()) {
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
    }


    private fun updateAllGameList() {
        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
            sportLeagueAdapter.data.forEachIndexed { index, leagueOdd ->
                sportLeagueAdapter.updateLeague(index,
                    leagueOdd)
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
        val intent = Intent(activity, MainActivity::class.java)
            .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
        startActivity(intent)
    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            matchInfo?.let { matchInfo ->
                val fastBetDataBean = FastBetDataBean(
                    matchType = matchType,
                    gameType = gameType,
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
        }
    }


    private fun subscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfold == FoldState.UNFOLD.code) {
                true -> {
                    subscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )
                }

                false -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )
                }
            }
        }
    }

    private fun unSubscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfold == FoldState.UNFOLD.code) {
                true -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )

                    matchOdd.quickPlayCateList?.forEach {
                        when (it.isSelected) {
                            true -> {
                                unSubscribeChannelHall(
                                    leagueOdd.gameType?.key,
                                    matchOdd.matchInfo?.id
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun unSubscribeLeagueChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            unSubscribeChannelHall(
                leagueOdd.gameType?.key,
                matchOdd.matchInfo?.id
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

    // region handle LeagueOdd data
    private fun clearQuickPlayCateSelected() {
        mLeagueOddList.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.isExpand = false
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                    quickPlayCate.isSelected = false
                }
            }
        }
    }

    private fun setQuickPlayCateSelected(
        selectedMatchOdd: MatchOdd,
        selectedQuickPlayCate: QuickPlayCate,
    ) {
        mLeagueOddList.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                if (selectedMatchOdd.matchInfo?.id == matchOdd.matchInfo?.id) {
                    matchOdd.isExpand = true
                    matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                        if (selectedQuickPlayCate.code == quickPlayCate.code) quickPlayCate.isSelected =
                            true
                    }
                }
            }
        }
    }

    @Subscribe
    fun onSelectLeague(leagueList: List<League>) {
        viewModel.filterLeague(leagueList)
        leagueIdList.clear()
        leagueList.forEach {
            leagueIdList.add(it.id)
        }
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
}