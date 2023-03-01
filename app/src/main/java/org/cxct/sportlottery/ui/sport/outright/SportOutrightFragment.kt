package org.cxct.sportlottery.ui.sport.outright

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.chad.library.adapter.base.entity.node.BaseNode
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_sport_list.*
import kotlinx.android.synthetic.main.fragment_sport_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.extentions.rotationAnimation
import org.cxct.sportlottery.extentions.setViewGone
import org.cxct.sportlottery.extentions.setViewVisible
import org.cxct.sportlottery.extentions.showLoading
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsLeagueOddsItem
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.common.SocketGridManager
import org.cxct.sportlottery.ui.game.hall.adapter.*
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.*
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectActivity
import org.cxct.sportlottery.util.*
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
class SportOutrightFragment: BaseBottomNavigationFragment<SportListViewModel>(SportListViewModel::class) {
    companion object {
        fun newInstance(
            gameType: String? = GameType.FT.key,
            outrightLeagueId: String? = null): SportOutrightFragment {

            val args = Bundle()
            args.putString("gameType", gameType)
            outrightLeagueId?.let { args.putString("outrightLeagueId", outrightLeagueId) }
            val fragment = SportOutrightFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //    private val args: GameV3FragmentArgs by navArgs()
    private val matchType = MatchType.OUTRIGHT
    private var gameType: String = GameType.FT.key
    private var mView: View? = null
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
                viewModel.switchGameType(it)
                iv_arrow.isSelected = true
            }

            thirdGameListener = ThirdGameListener { navThirdGame(it) }
        }
    }


    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            if (game_list == null || context == null || oddsChangeEvent.oddsList.isNullOrEmpty()) {
                return@OddsChangeListener
            }

            sportOutrightAdapter2.onMatchOdds(subscribedMatchOdd.toMutableList(), oddsChangeEvent)
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
                addOutRightOddsDialog(item.parentNode.matchOdd, item, item.outrightCateKey ?: "")
            } else { // 展开或收起
                setOutrightLeagueAdapter(200)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBusUtil.targetLifecycle(this)
        //打开指定球类
        viewModel.matchType = matchType
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
            visibility = when (matchType) {
                MatchType.EARLY -> View.VISIBLE
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

        iv_arrow.setOnClickListener {
            val selected = !iv_arrow.isSelected
            iv_arrow.isSelected = selected
            if (selected) {
                sportOutrightAdapter2.collapseAll()
                iv_arrow.rotationAnimation(180f)
            } else {
                sportOutrightAdapter2.expandAll()
                iv_arrow.rotationAnimation(0f)
            }
        }
    }


    private fun setupGameRow() {
        game_filter_type_list.apply {
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

            val layoutManager = game_list.layoutManager as GridLayoutManager
            val first = layoutManager.findFirstVisibleItemPosition()
            if (first < 0 || first >= sportOutrightAdapter2.getCount()) {
                return@Runnable
            }
            val last = layoutManager.findLastVisibleItemPosition()
            if (last < 0 || last >= sportOutrightAdapter2.getCount()) {
                return@Runnable
            }
            val matchOdds = mutableSetOf<org.cxct.sportlottery.network.outright.odds.MatchOdd>()
            for (i in first..last) {
                val item = sportOutrightAdapter2.getItem(i)
                if(item is Odd) {
                    matchOdds.add(item.parentNode.matchOdd)
                }
            }

            matchOdds.forEach { subscribeChannelHall(it) }
        }
    }

    private fun initObserve() {
        viewModel.notifyLogin.observe(this.viewLifecycleOwner) {
            (activity as MainTabActivity).showLoginNotify()
        }
        viewModel.showErrorDialogMsg.observe(this.viewLifecycleOwner) {
            if (requireContext() == null || TextUtils.isEmpty(it)) {
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

        viewModel.curDate.observe(this.viewLifecycleOwner) {
            dateAdapter.data = it
        }

        viewModel.curDatePosition.observe(this.viewLifecycleOwner) {
            var position = viewModel.tempDatePosition
            position = if (position != 0) position else it
        }

//        viewModel.userInfo.observe(this.viewLifecycleOwner) { userInfo ->
//
//        }

        viewModel.outrightList.observe(viewLifecycleOwner) {
            if (gameType != it.tag) {
                return@observe
            }
            val data = it?.getContentIfNotHandled()?.outrightOddsListData?.leagueOdds ?: return@observe
            val list = mutableListOf<MatchOdd>()
            data.forEach { it.matchOdds?.let { list.addAll(it) } }
            sportOutrightAdapter2.setNewInstance(list as MutableList<BaseNode>)
            setOutrightLeagueAdapter(120)
        }

        //KK要求，當球類沒有資料時，自動選取第一個有賽事的球種
//        viewModel.isNoHistory.observe(this.viewLifecycleOwner) {
//            when {
//                //當前MatchType有玩法數量，只是目前的球種沒有
//                it && curMatchTypeHasMatch() -> {
//                    unSubscribeAll()
//                    if (matchType != MatchType.OTHER) {
//                        viewModel.switchMatchType(matchType)
//                    }
//                }
//            }
//
//            hideLoading()
//        }

        //當前玩法無賽事
        viewModel.isNoEvents.distinctUntilChanged().observe(this.viewLifecycleOwner) {
            sport_type_list.isVisible = !it
            hideLoading()
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let {
                sportOutrightAdapter2.updateOddsSelectedStatus(it)
            }
        }

//        viewModel.oddsType.observe(this.viewLifecycleOwner) {
//
//        }


//        viewModel.favorLeagueList.observe(this.viewLifecycleOwner) {
//
//        }
//
//        viewModel.favorMatchList.observe(this.viewLifecycleOwner) {
//
//        }

        viewModel.leagueFilterList.observe(this.viewLifecycleOwner) { leagueList ->
            mLeagueIsFiltered = leagueList.isNotEmpty()
            sport_type_list.visibility = if (mLeagueIsFiltered) View.GONE else View.VISIBLE
        }

//        viewModel.checkInListFromSocket.observe(this.viewLifecycleOwner) { leagueChangeEvent ->
//            if (leagueChangeEvent != null) {
//                CoroutineScope(Dispatchers.IO).launch {
//                    if (!isUpdatingLeague) {
//                        isUpdatingLeague = true
//
//                        //收到的gameType与用户当前页面所选球种相同, 则需额外调用/match/odds/simple/list & /match/odds/eps/list
//                        val nowGameType =
//                            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key
//
//                        isUpdatingLeague = false
//                    }
//                }
//            }
//        }
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

//        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
//            it?.let { matchStatusChangeEvent ->
//                when (game_list.adapter) {
//
//                }
//            }
//        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                when (game_list.adapter) {
                    is SportLeagueAdapter -> {

                    }
                }
            }
        }
        setupOddsChangeListener()

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                when (game_list.adapter) {
                    is SportLeagueAdapter -> {
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->

                when (game_list.adapter) {
                    is SportLeagueAdapter -> {

                    }

                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeAll()

                when (game_list.adapter) {
                    is SportLeagueAdapter -> {

                    }
                }
            }
        }

        //distinctUntilChanged -> 短時間內收到相同leagueChangeEvent僅會執行一次
//        receiver.leagueChange.distinctUntilChanged().observe(this.viewLifecycleOwner) {
//            it?.let { leagueChangeEvent ->
//                viewModel.checkGameInList(
//                    matchType = matchType,
//                    leagueChangeEvent = leagueChangeEvent,
//                )
//                //待優化: 應有個暫存leagueChangeEvent的機制，確認後續流程更新完畢，再處理下一筆leagueChangeEvent，不過目前後續操作並非都是suspend，需重構後續流程
//            }
//        }
//
//        receiver.closePlayCate.observe(this.viewLifecycleOwner) { event ->
//
//        }
    }

    private fun updateGameList(index: Int, leagueOdd: LeagueOdd) {

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
            sport_type_list.isVisible = true
            iv_calendar.isVisible = matchType == MatchType.EARLY
            hideLoading()
            return
        }
        //处理默认不选中的情况
        if (gameType.isNullOrEmpty()) {
            gameTypeList.find {
                it.num > 0
            }?.let {
                it.isSelected = true
                gameType = it.code
                viewModel.switchGameType(it)
            }
        } else {
            (gameTypeList.find { it.code == gameType } ?: gameTypeList.first()).let {
                gameType = it.code
                if (!it.isSelected) {
                    it.isSelected = true
                    viewModel.switchGameType(it)
                }
            }
        }
        gameTypeAdapter.apply {
            dataSport = gameTypeList
        }
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

    private fun navThirdGame(thirdGameCategory: ThirdGameCategory) {
//        val intent = Intent(activity, MainActivity::class.java)
//            .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
//        startActivity(intent)
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
                playCateName = odd.parentNode.name,
                matchInfo = matchOdd.matchInfo!!,
                matchOdd = matchOdd,
                odd = odd,
                subscribeChannelType = ChannelType.HALL,
                betPlayCateNameMap = null))
        }

    }

    private val subscribedMatchOdd = mutableSetOf<org.cxct.sportlottery.network.outright.odds.MatchOdd>()

    private fun subscribeChannelHall(matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd) {
        val gameType = GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)
        gameType?.let {
            subscribedMatchOdd.add(matchOdd)
            subscribeChannelHall(it.key, matchOdd?.matchInfo?.id)
            matchOdd?.matchInfo?.let { Log.e("[subscribe]","訂閱 ${it.name} ${it.id} -> " + "${it.homeName} vs " + "${it.awayName}") }
        }
    }

    private fun unSubscribeAll() {
        subscribedMatchOdd.clear()
        unSubscribeChannelHallAll()
    }

    private fun unSubscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
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

    private fun subscribeChannelHall(epsLeagueOddsItem: EpsLeagueOddsItem) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        epsLeagueOddsItem.leagueOdds?.matchOdds?.forEach { matchOddsItem ->
            when (epsLeagueOddsItem.isClose) {
                true -> {
                    unSubscribeChannelHall(
                        gameType?.key,
                        matchOddsItem.matchInfo?.id
                    )
                }
                false -> {
                    subscribeChannelHall(
                        gameType?.key,
                        matchOddsItem.matchInfo?.id
                    )
                }
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
        offsetScrollListener = null
        game_list.adapter = null
        game_list?.removeCallbacks(subscribeVisibleRange)
        stopTimer()
        unSubscribeAll()
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

    // 赛选联赛
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