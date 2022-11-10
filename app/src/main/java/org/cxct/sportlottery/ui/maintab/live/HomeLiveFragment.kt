package org.cxct.sportlottery.ui.maintab.live

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_home_live.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
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
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.ui.maintab.HomeFragment
import org.cxct.sportlottery.ui.maintab.HomeTabAdapter
import org.cxct.sportlottery.ui.maintab.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.greenrobot.eventbus.EventBus

class HomeLiveFragment :
    BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    companion object {
        fun newInstance(): HomeLiveFragment {
            val args = Bundle()
            val fragment = HomeLiveFragment()
            fragment.arguments = args
            return fragment
        }
    }
    private val homeTabAdapter by lazy {
        HomeTabAdapter(HomeTabAdapter.getItems(), 1).apply {
            setOnItemClickListener { adapter, view, position ->
                (parentFragment as HomeFragment).onTabClickByPosition(position)
            }
        }
    }
    private val homeLiveAdapter by lazy {
        HomeLiveAdapter(
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
                onClickLiveListener = { matchId, roundNo ->
                    viewModel.getLiveInfo(roundNo)
                }
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_home_live, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.fitsSystemStatus()
        viewModel.getConfigData()
        initView()
        initObservable()
        initSocketObservers()
        viewModel.getLiveRoundHall()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getLiveRoundHall()
            setupOddsChangeListener()
        } else {
            homeLiveAdapter.expandMatchId = null
            //todo 待测试  Timber.d("视频播放静音")
            homeLiveAdapter.setVolumeMute()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun initView() {
        initToolBar()
        initTabView()
        initListView()

    }

    fun initToolBar() {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        iv_menu_left.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(true))
            (activity as MainTabActivity).showLeftFrament(0, 1)
        }
        iv_logo.setOnClickListener {
            (activity as MainTabActivity).jumpToHome(0)
        }
        btn_register.setOnClickListener {
            startActivity(Intent(requireActivity(), RegisterOkActivity::class.java))
        }
        btn_login.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
        iv_money_refresh.setOnClickListener {
            iv_money_refresh.startAnimation(RotateAnimation(0f,
                720f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f).apply {
                duration = 1000
            })
            viewModel.getMoney()
        }
//        lin_search.setOnClickListener {
//            startActivity(Intent(requireActivity(), SportSearchtActivity::class.java))
//        }
        setupLogin()
    }
    private fun initObservable() {
        if (viewModel == null) {
            return
        }
        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let {
                tv_home_money.text = "${sConfigData?.systemCurrencySign} ${TextUtil.format(it)}"
            }
        }
        viewModel.isLogin.observe(viewLifecycleOwner) {
            setupLogin()
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
            var needGetLive = homeLiveAdapter.data.isEmpty()
            homeLiveAdapter.data = it
            rv_live?.firstVisibleRange(homeLiveAdapter, activity ?: requireActivity())
            //先解除全部賽事訂
            unSubscribeChannelHallAll()
            subscribeQueryData(it)
            if (needGetLive) {
                it.first()?.matchInfo.roundNo?.let {
                    viewModel.getLiveInfo(it)
                }
            }

        }

        viewModel.betInfoList.observe(viewLifecycleOwner) { event ->
            event.peekContent().let { betInfoList ->
                homeLiveAdapter.betInfoList = betInfoList
            }
        }
        viewModel.matchLiveInfo.observe(viewLifecycleOwner) { event ->
            event?.peekContent()?.let { matchRound ->
                homeLiveAdapter.data.forEachIndexed { index, matchLiveData ->
                    if (matchLiveData.matchInfo.roundNo == matchRound.roundNo) {
                        matchLiveData.matchInfo.pullRtmpUrl = matchRound.pullRtmpUrl
                        matchLiveData.matchInfo.pullFlvUrl = matchRound.pullFlvUrl
                        homeLiveAdapter.notifyItemChanged(index, matchLiveData)
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
                var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

                targetList.forEachIndexed { index, matchLiveData ->
                    val matchList = listOf(matchLiveData).toMutableList()
                    if (SocketUpdateUtil.updateMatchStatus(
                            matchLiveData.matchInfo.gameType,
                            matchList as MutableList<org.cxct.sportlottery.network.common.MatchOdd>,
                            matchStatusChangeEvent,
                            context
                        )
                    ) {
                        needUpdate = true
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
                if (needUpdate) {
                    homeLiveAdapter.data = targetList
                }
            }
        }

        receiver.matchClock.observe(viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val targetList = homeLiveAdapter.data
                var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

                targetList.forEachIndexed { index, recommend ->
                    if (
                        SocketUpdateUtil.updateMatchClock(
                            recommend,
                            matchClockEvent
                        )
                    ) {
                        needUpdate = true
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }

                if (needUpdate) {
                    homeLiveAdapter.data = targetList
                }
            }
        }

        setupOddsChangeListener()

        receiver.matchOddsLock.observe(viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                val targetList = homeLiveAdapter.data
                var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(recommend, matchOddsLockEvent)
                    ) {
                        needUpdate = true
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }

//                if (needUpdate) {
//                    homeRecommendAdapter.data = targetList
//                }
            }
        }

//        receiver.leagueChange.observe(viewLifecycleOwner) {
//            it?.let { leagueChangeEvent ->
//                viewModel.publicityLeagueChange(leagueChangeEvent)
//            }
//        }

        receiver.globalStop.observe(viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                val targetList = homeLiveAdapter.data
                var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(
                            recommend,
                            globalStopEvent
                        )
                    ) {
                        needUpdate = true
                        //TODO 更新邏輯待補，跟進GameV3Fragment
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
                        homeLiveAdapter.notifyItemChanged(index, matchLiveData)
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
            if (layoutManager == null) {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            }
            if (adapter == null) {
                adapter = homeLiveAdapter
                addOnScrollListener(recyclerViewOnScrollListener)
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
        ll_come_back.setOnClickListener {
            rv_live.smoothScrollToPosition(0)
        }
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

    private fun goLoginPage() {
        startActivity(Intent(context, LoginActivity::class.java))
    }


    private fun goGamePage() {
        GameActivity.reStart(activity ?: requireActivity())
        activity?.finish()
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

    private fun setupLogin() {
        viewModel.isLogin.value?.let {
            btn_register.isVisible = !it
            btn_login.isVisible = !it
//            lin_search.visibility = if (it) View.VISIBLE else View.GONE
            ll_user_money.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }
    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
       //列表滑动距离

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            ll_come_back.isVisible = recyclerView.canScrollVertically(0)
        }
    }
}
