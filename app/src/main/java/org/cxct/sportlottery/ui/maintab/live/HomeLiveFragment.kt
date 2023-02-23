package org.cxct.sportlottery.ui.maintab.live

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_home_live.*
import kotlinx.android.synthetic.main.fragment_home_live.homeToolbar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.extentions.fitsSystemStatus
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchLiveData
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.maintab.HomeFragment
import org.cxct.sportlottery.ui.maintab.HomeTabAdapter
import org.cxct.sportlottery.ui.maintab.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp

class HomeLiveFragment :
    BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    override fun layoutId() = R.layout.fragment_home_live

    private val homeTabAdapter by lazy {
        HomeTabAdapter(HomeTabAdapter.getItems(), 1, (parentFragment as HomeFragment))
    }
    private val homeLiveAdapter by lazy {
        HomeLiveAdapter(
            this,
            HomeLiveListener(
                onItemClickListener = {
                    navOddsDetailFragment(MatchType.IN_PLAY, it.matchInfo)
                },
                onClickBetListener = { gameType, matchType, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap, playCateMenuCode ->
                    if (mIsEnabled) {
                        avoidFastDoubleClick()
                        addOddsDialog(
                            gameType,
                            matchType,
                            matchInfo,
                            odd,
                            playCateCode,
                            playCateName,
                            betPlayCateNameMap,
                            playCateMenuCode
                        )
                    }
                },
                onClickLiveListener = { matchInfo, roundNo ->
                    if (matchInfo.pullRtmpUrl.isNullOrEmpty()) {
                        viewModel.getLiveInfo(roundNo, 1)
                    }
                }
            )
        )
    }

    override fun onBindView(view: View) {
        view.fitsSystemStatus()
        initView()
        initObservable()
        initSocketObservers()
        viewModel.getLiveRoundHall()
    }

    override fun onPause() {
        super.onPause()
        homeLiveAdapter.playerView?.onVideoPause()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
//            homeLiveAdapter.playerView?.startPlayLogic()
            viewModel.getLiveRoundHall()
            setupOddsChangeListener()
        } else {
            homeLiveAdapter.playerView?.onVideoPause()
            homeLiveAdapter.expandMatchId = null
        }
    }

    private fun initView() {
        initToolBar()
        initTabView()
        initListView()

    }

    private inline fun getMainTabActivity() = activity as MainTabActivity

    fun initToolBar() = homeToolbar.run {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this@HomeLiveFragment), 0, 0)
        attach(this@HomeLiveFragment, getMainTabActivity(), viewModel)
        ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            getMainTabActivity().showLeftFrament(0, 1)
        }
    }

    private fun initObservable() {
        if (viewModel == null) {
            return
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                homeLiveAdapter.oddsType = oddsType
            }
        }
        viewModel.liveRoundHall.observe(viewLifecycleOwner) { it ->
            if (it.isEmpty()) return@observe //推薦賽事為empty不顯示
            it.forEach { matchLiveData ->
                // 將儲存的賠率表指定的賽事列表裡面
                val leagueOddFromMap = matchOddMap[matchLiveData.matchInfo?.id]
                leagueOddFromMap?.let {
                    matchLiveData.oddsMap = leagueOddFromMap.oddsMap
                }
            }
            homeLiveAdapter.data = it
            rv_live?.firstVisibleRange(homeLiveAdapter, activity ?: requireActivity())
            //先解除全部賽事訂
            unSubscribeChannelHallAll()
            subscribeQueryData(it)
            it.firstOrNull()?.let {
                if (it.matchInfo.isLive == 1 && !it.matchInfo.roundNo.isNullOrEmpty()) {
                    viewModel.getLiveInfo(it.matchInfo.roundNo, 1)
                }
            }
        }

        viewModel.betInfoList.observe(viewLifecycleOwner) { event ->
            event.peekContent().let { betInfoList ->
                homeLiveAdapter.betInfoList = betInfoList
            }
        }
        viewModel.matchLiveInfo.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { matchRound ->
                homeLiveAdapter.data.forEachIndexed { index, matchLiveData ->
                    if (matchLiveData.matchInfo.roundNo == matchRound.roundNo) {
                        matchLiveData.matchInfo.pullRtmpUrl = matchRound.pullRtmpUrl
                        matchLiveData.matchInfo.pullFlvUrl = matchRound.pullFlvUrl
                        if (isVisible) {
                            homeLiveAdapter.notifyItemChanged(index)
                            homeLiveAdapter.expandMatchId = matchLiveData.matchInfo.id
                            rv_live.smoothScrollToPosition(index)
                        }
                    }
                }
            }
        }
    }

    //用户缓存最新赔率，方便当从api拿到新赛事数据时，赋值赔率信息给新赛事
    private val matchOddMap = HashMap<String, MatchLiveData>()
    private fun initSocketObservers() {
        receiver.serviceConnectStatus.observe(viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    subscribeSportChannelHall()
                    viewModel.getRecommend()
                }
            }
        }

        receiver.matchStatusChange.observe(viewLifecycleOwner) { event ->
            event?.let { matchStatusChangeEvent ->
                val targetList = homeLiveAdapter.data
                targetList.forEachIndexed { index, matchLiveData ->
                    val matchList = mutableListOf(matchLiveData)
                    if (SocketUpdateUtil.updateMatchStatus(
                            matchLiveData.matchInfo.gameType,
                            matchList as MutableList<org.cxct.sportlottery.network.common.MatchOdd>,
                            matchStatusChangeEvent,
                            context
                        )
                    ) {
                        homeLiveAdapter.notifyItemChanged(index)
                    }
                }
            }
        }

        receiver.matchClock.observe(viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val targetList = homeLiveAdapter.data
                targetList.forEachIndexed { index, recommend ->
                    if (
                        SocketUpdateUtil.updateMatchClock(
                            recommend,
                            matchClockEvent
                        )
                    ) {
                        homeLiveAdapter.notifyItemChanged(index)
                    }
                }

            }
        }

        setupOddsChangeListener()

        receiver.matchOddsLock.observe(viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                val targetList = homeLiveAdapter.data

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(recommend, matchOddsLockEvent)
                    ) {
                        homeLiveAdapter.notifyItemChanged(index)
                    }
                }

            }
        }


        receiver.globalStop.observe(viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                homeLiveAdapter.data.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(
                            recommend,
                            globalStopEvent
                        )
                    ) {
                        homeLiveAdapter.notifyItemChanged(index)
                    }
                }

//                if (needUpdate) {
//                    homeRecommendAdapter.data = targetList
//                }
            }
        }

        receiver.producerUp.observe(viewLifecycleOwner) {
            it?.let {
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(homeLiveAdapter.data)
            }
        }

        receiver.closePlayCate.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                homeLiveAdapter.data.forEach { matchLiveData ->
                    if (matchLiveData.matchInfo.gameType == it.gameType) {
                        matchLiveData.oddsMap?.forEach { map ->
                            if (map.key == it.playCateCode) {
                                map.value?.forEach { odd ->
                                    odd?.status = BetStatus.DEACTIVATED.code
                                }
                            }
                        }
                    }
                }
                homeLiveAdapter.notifyDataSetChanged()
            }
        }
    }

    fun setupOddsChangeListener() {
        receiver.oddsChangeListener = mOddsChangeListener
    }

    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            val targetList = homeLiveAdapter.data
            targetList.forEachIndexed { index, matchLiveData ->
                if (matchLiveData.matchInfo.id == oddsChangeEvent.eventId) {
                    matchLiveData.sortOddsMap()
                    //region 翻譯更新
                    oddsChangeEvent.playCateNameMap?.let { playCateNameMap ->
                        matchLiveData.playCateNameMap?.putAll(playCateNameMap)
                    }
                    oddsChangeEvent.betPlayCateNameMap?.let { betPlayCateNameMap ->
                        matchLiveData.betPlayCateNameMap?.putAll(betPlayCateNameMap)
                    }
                    //endregion
                    if (SocketUpdateUtil.updateMatchOdds(context,
                            matchLiveData,
                            oddsChangeEvent)
                    ) {
                        updateBetInfo(matchLiveData, oddsChangeEvent)
                        matchOddMap[matchLiveData.league.id] = matchLiveData
                        homeLiveAdapter.notifyItemChanged(index)
                    }
                }
            }
        }
    }

    private fun MatchLiveData.sortOddsMap() {
        this.oddsMap?.forEach { (_, value) ->
            if ((value?.size
                    ?: 0) > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)
            ) {
                value?.sortBy {
                    it?.marketSort
                }
            }
        }
    }


    private fun initTabView() {
        with(rv_tab_home) {
            if (layoutManager == null) {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
            if (adapter == null) {
                adapter = homeTabAdapter
            }
        }
    }

    private fun initListView() {
        with(rv_live) {
            val pool = RecycledViewPool()
            pool.setMaxRecycledViews(0, 10)
            setRecycledViewPool(pool)
            itemAnimator?.changeDuration = 0
            setHasFixedSize(true);
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false;
            if (layoutManager == null) {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            }

            if (adapter == null) {
                adapter = homeLiveAdapter
            }
            addScrollWithItemVisibility(
                onScrolling = {
                    unSubscribeChannelHallAll()
                },
                onVisible = {
                    when (adapter) {
                        is HomeLiveAdapter -> {
                            if (homeLiveAdapter.data.isNotEmpty()) {
                                it.forEach { p ->
                                    Log.d(
                                        "[subscribe]",
                                        "訂閱 ${homeLiveAdapter.data[p.first].league.name} -> " +
                                                "${homeLiveAdapter.data[p.first].matchInfo?.homeName} vs " +
                                                "${homeLiveAdapter.data[p.first].matchInfo?.awayName}"
                                    )
                                    subscribeChannelHall(
                                        homeLiveAdapter.data[p.first].matchInfo?.gameType,
                                        homeLiveAdapter.data[p.first].matchInfo?.id
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        rv_live.setupBackTop(iv_top, 180.dp)
    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(matchLiveData: MatchLiveData, oddsChangeEvent: OddsChangeEvent) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (matchLiveData.matchInfo?.id == oddsChangeEvent.eventId && matchLiveData.oddsMap?.values?.any { oddList ->
                    oddList?.any { odd ->
                        odd?.isSelected == true
                    } == true
                } == true
            ) {
                viewModel.updateMatchOdd(oddsChangeEvent)
            }
        }
    }


    private fun addOddsDialog(
        gameTypeCode: String,
        matchType: MatchType,
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        playCateMenuCode: String?,
    ) {
        val gameType = GameType.getGameType(gameTypeCode)
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
                    playCateMenuCode
                )
                when (val fragmentActivity = activity) {
                    is MainTabActivity -> fragmentActivity.setupBetData(fastBetDataBean)
                }
            }
        }
    }


    private fun navOddsDetailFragment(
        matchType: MatchType,
        matchInfo: MatchInfo,
    ) {
        SportDetailActivity.startActivity(requireContext(),
            matchInfo = matchInfo,
            matchType = matchType,
            intoLive = true)
    }


    private fun subscribeQueryData(list: List<MatchLiveData>) {
        list.forEach { subscribeChannelHall(it) }
    }

    private fun subscribeChannelHall(matchLiveData: MatchLiveData) {
        subscribeChannelHall(matchLiveData.matchInfo.gameType, matchLiveData.matchInfo.id)
    }

    /**
     * 檢查信用盤狀態下是否已登入
     * @param eventFun 處於信用盤時若已登入則執行該function, 若非信用盤則直接執行
     */
    private fun checkCreditSystemLogin(eventFun: () -> Unit) {
        if (isCreditSystem()) {
            if (viewModel.checkLoginStatus()) {
                eventFun()
            }
        } else {
            eventFun()
        }
    }

}
