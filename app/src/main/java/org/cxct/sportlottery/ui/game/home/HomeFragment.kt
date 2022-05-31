package org.cxct.sportlottery.ui.game.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.itemview_league_v5.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentHomeBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MenuCode
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.matchCategory.result.MatchCategoryResult
import org.cxct.sportlottery.network.matchCategory.result.MatchRecommendResult
import org.cxct.sportlottery.network.matchCategory.result.RECOMMEND_OUTRIGHT
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener
import org.cxct.sportlottery.ui.game.home.gameTable4.*
import org.cxct.sportlottery.ui.game.home.recommend.RecommendGameEntity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.OddsSortUtil.recommendSortOddsMap
import org.cxct.sportlottery.util.PlayCateMenuFilterUtils
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.getVisibleRangePosition
import java.util.*


/**
 * TODO simon test:
 * 1. 上下滑動 ToolBar 固定
 * 2. 賽事精選: icon 顯示 review
 * 3. 賽事推薦 - 冠軍樣式
 * 4. 賽事推薦 投注
 * @app_destination 主頁
 */
@SuppressLint("LogNotTimber")
class HomeFragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var homeBinding: FragmentHomeBinding

    private var mSelectMatchType: MatchType = MatchType.MAIN
        set(value) {
            field = value

            setupTableSelected()
        }
    private var mHomeGameTableBarItemData = HomeListAdapter.HomeGameTableBarItemData()

    private val mHomeListAdapter = HomeListAdapter().apply {
        setBottomNavigation()
    }

    private var tableInPlayMap = mutableMapOf<String, String>()
    private var tableSoonMap = mutableMapOf<String, String>()

    private var isInPlayResult = false
    private var isSoonResult = false
    private var changeTime = System.currentTimeMillis()

    //TODO 檢查 mSubscribeInPlayGameID、mSubscribeAtStartGameID 與tableInPlayMap、tableSoonMap 的功用
    private var mSubscribeInPlayGameID: MutableList<String> = mutableListOf()
    private var mSubscribeAtStartGameID: MutableList<String> = mutableListOf()
    private var mSubscribeRecommendGameID: MutableList<String> = mutableListOf()
    private var mSubscribeHighlightGameID: MutableList<String> = mutableListOf()

    private val mOnClickOddListener = object : OnClickOddListener {
        override fun onClickBet(
            matchOdd: MatchOdd,
            odd: Odd,
            playCateCode: String,
            playCateName: String?,
            betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
        ) {
            if (mIsEnabled) {
                avoidFastDoubleClick()
                addOddsDialog(matchOdd, odd, playCateCode, playCateName, betPlayCateNameMap)
            }
        }
    }

    private val isShowThirdGame = sConfigData?.thirdOpen == FLAG_OPEN
    private var lotteryCount = 0
    private var liveCount = 0
    private var pokerCount = 0
    private var slotCount = 0
    private var fishingCount = 0
    private var selectedSportType: Item? = null
    private var mTimer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        homeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        homeBinding.apply {
            gameViewModel = this@HomeFragment.viewModel
            lifecycleOwner = this@HomeFragment
        }
        return homeBinding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.getSportMenuFilter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            selectedSportType = null

            initViews()
            initGameTableBar()
            initDiscount()
            initMenu()
            initTable()
            initRecommend()
            initHighlight()
            initObserve()
            initSocketObserver()
            mTimer = Timer()
            mTimer?.schedule(object : TimerTask() {
                override fun run() {
                    lifecycleScope.launch {
                        with(mHomeListAdapter) {
                            notifyTimeChanged(1)
                        }
                    }
                }
            }, 1000L, 1000L)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unSubscribeChannelHallAll()
        mSubscribeInPlayGameID.clear()
        mSubscribeAtStartGameID.clear()
        mSubscribeRecommendGameID.clear()
        mSubscribeHighlightGameID.clear()
        mTimer?.cancel()
        mTimer = null
    }

    override fun onStop() {
        super.onStop()
        unSubscribeChannelHallAll()
        mSubscribeInPlayGameID.clear()
        mSubscribeAtStartGameID.clear()
        mSubscribeRecommendGameID.clear()
        mSubscribeHighlightGameID.clear()
    }

    private fun initViews() {
        rvList.apply {
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = mHomeListAdapter
            itemAnimator = null
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        //停止
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            subscribeLogic()
                        }

                        //手指滾動
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            unSubscribeChannelHallAll()
                        }
                    }
                }
            })
        }
    }

    private fun initDiscount() {
        val discount = viewModel.userInfo.value?.discount ?: 1.0F
        with(mHomeListAdapter) {
            setDiscount(discount)
            notifyOddsDiscountChanged(discount)
            notifyHighLightOddsDiscountChanged(discount)
        }
    }

    private fun initGameTableBar() {
        mHomeListAdapter.onGameTableBarViewHolderListener = object : GameTableBarViewHolder.Listener {
            override fun onGameTableSelect(matchType: MatchType) {
                when (matchType) {
                    MatchType.IN_PLAY -> {
                        if (mSelectMatchType == MatchType.IN_PLAY) return
                        mSelectMatchType = MatchType.IN_PLAY
                        refreshTable(mHomeGameTableBarItemData.inPlayResult)

                        if (mSelectMatchType != MatchType.MAIN) {
                            unsubscribeUnSelectMatchTypeHallChannel()
                            viewModel.getMatchPreloadInPlay()
                        }
                    }
                    MatchType.AT_START -> {
                        if (mSelectMatchType == MatchType.AT_START) return
                        mSelectMatchType = MatchType.AT_START
                        refreshTable(mHomeGameTableBarItemData.atStartResult)

                        if (mSelectMatchType != MatchType.MAIN) {
                            unsubscribeUnSelectMatchTypeHallChannel()
                            viewModel.getMatchPreloadAtStart()
                        }
                    }
                }
            }
        }
    }

    private fun initMenu() {
        with(mHomeListAdapter) {
            onClickMenuListener = OnClickMenuListener(
                onGameSoon = { viewModel.navSpecialEntrance(MatchType.AT_START, null) },
                onLottery = { navThirdGame(ThirdGameCategory.CGCP) },
                onLive = { navThirdGame(ThirdGameCategory.LIVE) },
                onPoker = { navThirdGame(ThirdGameCategory.QP) },
                onSlot = { navThirdGame(ThirdGameCategory.DZ) },
                onFishing = { navThirdGame(ThirdGameCategory.BY) },
                onGameResult = { startActivity(Intent(activity, ResultsSettlementActivity::class.java)) },
                onUpdate = { startActivity(Intent(activity, VersionUpdateActivity::class.java)) },
                onFirstGame = { sportMenu ->
                    if (sportMenu.entranceType != null) {
                        sportMenu.entranceType?.let {
                            viewModel.navSpecialEntrance(
                                it,
                                sportMenu.gameType
                            )
                        }
                    } else {
                        viewModel.setSportClosePromptMessage(MultiLanguagesApplication.appContext.getString(sportMenu.gameType.string))
                    }
                },
                onSecondGame = { sportMenu ->
                    if (sportMenu.entranceType != null) {
                        sportMenu.entranceType?.let {
                            viewModel.navSpecialEntrance(it, sportMenu.gameType)
                        }
                    } else {
                        viewModel.setSportClosePromptMessage(MultiLanguagesApplication.appContext.getString(sportMenu.gameType.string))
                    }
                },
                onHomeCard = { sportMenu ->
                    if (sportMenu.entranceType != null) {
                        sportMenu.entranceType?.let {
                            viewModel.navSpecialEntrance(it, sportMenu.gameType)
                        }
                    } else {
                        viewModel.setSportClosePromptMessage(MultiLanguagesApplication.appContext.getString(sportMenu.gameType.string))
                    }
                },
                onCouponCard = { sportCouponMenuData ->
                    viewModel.navSpecialEntrance(
                        MatchType.OTHER,
                        null,
                        sportCouponMenuData.couponCode,
                        sportCouponMenuData.couponName
                    )
                }
            )

            initMenuBlock()
        }
    }

    private fun initTable() {
        mHomeListAdapter.onClickOddListener = object : OnClickOddListener {
            override fun onClickBet(
                matchOdd: MatchOdd, odd: Odd, playCateCode: String, playCateName: String?,
                betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
            ) {
                if (mIsEnabled) {
                    avoidFastDoubleClick()
                    addOddsDialog(matchOdd, odd, playCateCode, playCateName, betPlayCateNameMap)
                }
            }
        }

        mHomeListAdapter.onClickMatchListener = object : OnSelectItemListener<MatchInfo> {
            override fun onClick(select: MatchInfo) {
                val code = select.gameType
                val matchId = select.id
                navOddsDetailFragment(code, matchId, mSelectMatchType)
            }
        }

        mHomeListAdapter.onClickLiveListener = object : OnSelectItemListener<MatchInfo> {
            override fun onClick(select: MatchInfo) {
                if (viewModel.checkLoginStatus()){
                    val code = select.gameType
                    val matchId = select.id
                    navOddsDetailFragment(code, matchId, mSelectMatchType)
                }
            }
        }

        mHomeListAdapter.onClickAnimationListener = object : OnSelectItemListener<MatchInfo> {
            override fun onClick(select: MatchInfo) {
                if (viewModel.checkLoginStatus()){
                    val code = select.gameType
                    val matchId = select.id
                    navOddsDetailFragment(code, matchId, mSelectMatchType)
                }
            }
        }

        mHomeListAdapter.onClickTotalMatchListener = object : OnSelectItemListener<GameEntity> {
            override fun onClick(select: GameEntity) {
                viewModel.navSpecialEntrance(
                    mSelectMatchType,
                    GameType.getGameType(select.code)
                )
            }
        }

        mHomeListAdapter.onClickSportListener = object : OnSelectItemListener<OtherMatch> {
            override fun onClick(select: OtherMatch) {
                viewModel.navSpecialEntrance(
                    mSelectMatchType,
                    GameType.getGameType(select.code)
                )
            }
        }

        mHomeListAdapter.onClickFavoriteListener = object : OnClickFavoriteListener {
            override fun onClickFavorite(matchId: String?) {
                viewModel.pinFavorite(FavoriteType.MATCH, matchId)
            }
        }

        mHomeListAdapter.onClickStatisticsListener = object : OnClickStatisticsListener {
            override fun onClickStatistics(matchId: String?) {
                navStatisticsPage(matchId)
            }
        }
    }

    private fun initRecommend() {
        mHomeListAdapter.onRecommendClickOddListener = mOnClickOddListener
        mHomeListAdapter.onRecommendClickOutrightOddListener = object : OnClickOddListener {
            override fun onClickBet(
                matchOdd: MatchOdd, odd: Odd, playCateCode: String, playCateName: String?,
                betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
            ) {
                GameType.getGameType(matchOdd.matchInfo?.gameType)?.let { gameType ->
                    val fastBetDataBean = FastBetDataBean(
                        matchType = MatchType.OUTRIGHT,
                        gameType = gameType,
                        playCateCode = playCateCode,
                        playCateName = playCateName ?: "",
                        matchInfo = matchOdd.matchInfo!!,
                        matchOdd = org.cxct.sportlottery.network.outright.odds.MatchOdd(
                            matchInfo = matchOdd.matchInfo,
                            oddsMap = matchOdd.oddsMap ?: mutableMapOf(),
                            dynamicMarkets = matchOdd.dynamicMarkets ?: mapOf(),
                            oddsList = null,
                            quickPlayCateList = matchOdd.quickPlayCateList,
                            betPlayCateNameMap = matchOdd.betPlayCateNameMap,
                            playCateNameMap = matchOdd.playCateNameMap
                        ),
                        odd = odd,
                        subscribeChannelType = ChannelType.HALL,
                        betPlayCateNameMap = betPlayCateNameMap,
                    )
                    (activity as GameActivity).showFastBetFragment(fastBetDataBean)
                }
            }
        }

        mHomeListAdapter.onRecommendClickMoreListener = object : OnClickMoreListener {
            override fun onClickMore(oddsKey: String, matchOdd: MatchOdd) {
                val action = HomeFragmentDirections.actionHomeFragmentToGameOutrightMoreFragment(
                    oddsKey,
                    org.cxct.sportlottery.network.outright.odds.MatchOdd(
                        matchInfo = matchOdd.matchInfo,
                        oddsMap = matchOdd.oddsMap ?: mutableMapOf(),
                        dynamicMarkets = matchOdd.dynamicMarkets ?: mapOf(),
                        oddsList = listOf(),
                        quickPlayCateList = matchOdd.quickPlayCateList,
                        betPlayCateNameMap = matchOdd.betPlayCateNameMap,
                        playCateNameMap = matchOdd.playCateNameMap
                    )
                )
                findNavController().navigate(action)
            }
        }

        mHomeListAdapter.onRecommendClickMatchListener = object : OnSelectItemListener<RecommendGameEntity> {
            override fun onClick(select: RecommendGameEntity) {
//                scroll_view.smoothScrollTo(0, 0)
                val code = select.code
                val matchId = select.matchInfo?.id

                if (select.isOutright == RECOMMEND_OUTRIGHT) {
                    navGameOutright(select.code, select.leagueId)
                } else {
                    //TODO simon test review 推薦賽事是不是一定是 MatchType.TODAY
                    navOddsDetailFragment(code, matchId, MatchType.TODAY)
                }
            }
        }
    }

    private fun initHighlight() {
        mHomeListAdapter.gameTypeListener = GameTypeListener { selectItem ->
            selectedSportType = selectItem
            mHomeListAdapter.updateHightLightTitle(selectItem)
            mHomeListAdapter.getDataSport().forEach { item ->
                item.isSelected = item.code == selectItem.code
            }
            mHomeListAdapter.updateDataSport()
            viewModel.getHighlightMatch(selectItem.code)
        }

        mHomeListAdapter.onHighLightClickOddListener = mOnClickOddListener
        mHomeListAdapter.onHighLightClickMatchListener = object : OnSelectItemListener<MatchOdd> {
            override fun onClick(select: MatchOdd) {
                val code = select.matchInfo?.gameType
                val matchId = select.matchInfo?.id

                //TODO simon test review 精選賽事是不是一定是 MatchType.TODAY
                navOddsDetailFragment(code, matchId, MatchType.TODAY)
            }
        }

        mHomeListAdapter.onHighLightClickFavoriteListener = object : OnClickFavoriteListener {
            override fun onClickFavorite(matchId: String?) {
                viewModel.pinFavorite(FavoriteType.MATCH, matchId)
            }
        }

        mHomeListAdapter.onHighLightClickStatisticsListener = object : OnClickStatisticsListener {
            override fun onClickStatistics(matchId: String?) {
                navStatisticsPage(matchId)
            }
        }
    }

    private fun setupTableSelected() {
        with(mHomeGameTableBarItemData) {
            when (mSelectMatchType) {
                MatchType.IN_PLAY -> {
                    inPlayResult?.isSelected = true
                    atStartResult?.isSelected = false
                }
                MatchType.AT_START -> {
                    inPlayResult?.isSelected = false
                    atStartResult?.isSelected = true
                }
                else -> {
                    //do nothing
                }
            }
        }
    }

    private fun refreshTable(result: MatchPreloadResult?) {
        val gameDataList: MutableList<GameEntity> = mutableListOf()
        val otherMatchList: MutableList<OtherMatch> = mutableListOf()
        result?.matchPreloadData?.datas?.forEach { data ->
            if (data.matchOdds.isNotEmpty()) {

                mHomeListAdapter.mDataList.forEach { any ->
                    when (any) {
                        is GameEntity -> {

                            //刷新前將原資料放入新取得的物件內
                            any.matchOdds.forEach { curMatchOdd ->
                                data.matchOdds.find {
                                    it.matchInfo?.id == curMatchOdd.matchInfo?.id
                                }?.apply {
                                    matchInfo?.homeTotalScore = curMatchOdd.matchInfo?.homeTotalScore
                                    matchInfo?.awayTotalScore = curMatchOdd.matchInfo?.awayTotalScore
                                    matchInfo?.homeScore = curMatchOdd.matchInfo?.homeScore
                                    matchInfo?.awayScore = curMatchOdd.matchInfo?.awayScore
                                    matchInfo?.homePoints = curMatchOdd.matchInfo?.homePoints
                                    matchInfo?.awayPoints = curMatchOdd.matchInfo?.awayPoints
                                    matchInfo?.statusName18n = curMatchOdd.matchInfo?.statusName18n
                                    matchInfo?.homeCards = curMatchOdd.matchInfo?.homeCards
                                    matchInfo?.awayCards = curMatchOdd.matchInfo?.awayCards
                                    matchInfo?.scoreStatus = curMatchOdd.matchInfo?.status
                                    runningTime = curMatchOdd.runningTime
                                }
                            }
                        }
                    }
                }

                val gameEntity = GameEntity(
                    data.code,
                    data.name,
                    data.num,
                    data.matchOdds.toMutableList(),
                    data.playCateNameMap
                )
                gameDataList.add(gameEntity)
            } else {
                val otherMatch = OtherMatch(data.code, data.name, data.num)
                otherMatchList.add(otherMatch)
            }
        }
        if (!otherMatchList.isNullOrEmpty()) {
            val otherGameEntity =
                GameEntity(null, null, 0, mutableListOf(), mutableMapOf(), otherMatchList)
            gameDataList.add(otherGameEntity)
        }

        gameDataList.sortOddsMap()
        mHomeListAdapter.setGameTableData(
            gameDataList,
            mSelectMatchType,
            viewModel.betIDList.value?.peekContent() ?: mutableListOf()
        )
    }

    private fun refreshHighlight(result: MatchCategoryResult?) {
        val sportCode = mHomeListAdapter.getDataSport().find { it.isSelected }?.code ?: ""
        mHomeListAdapter.setMatchOdd(
            sportCode,
            result?.t?.odds,
            viewModel.betIDList.value?.peekContent() ?: mutableListOf(),
            result?.t?.playCateNameMap
        )
    }

    private fun addOddsDialog(
        matchOdd: MatchOdd,
        odd: Odd,
        playCateCode: String,
        playCateName: String?,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
    ) {
        GameType.getGameType(matchOdd.matchInfo?.gameType)?.let { gameType ->
            matchOdd.matchInfo?.let { matchInfo ->
                val fastBetDataBean = FastBetDataBean(
                    matchType = mSelectMatchType,
                    gameType = gameType,
                    playCateCode = playCateCode,
                    playCateName = playCateName ?: "",
                    matchInfo = matchInfo,
                    matchOdd = null,
                    odd = odd,
                    subscribeChannelType = ChannelType.HALL,
                    betPlayCateNameMap = betPlayCateNameMap,
                    playCateMenuCode = if (mSelectMatchType == MatchType.IN_PLAY || mSelectMatchType == MatchType.MAIN) MenuCode.HOME_INPLAY_MOBILE.code else MenuCode.HOME_ATSTART_MOBILE.code
                )
                (activity as GameActivity).showFastBetFragment(fastBetDataBean)
            }
        }
    }

    private fun navThirdGame(thirdGameCategory: ThirdGameCategory) {
        val intent = Intent(activity, MainActivity::class.java)
            .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
        startActivity(intent)
    }

    //重新訂閱 滾球盤 or 即將開賽 賠率
    private fun reSubscribeTableHallChannel(selectMatchType: MatchType) {
        lifecycleScope.launch {
            when (selectMatchType) {
                MatchType.IN_PLAY -> {
                    tableInPlayMap.forEach { (gameType, eventId) ->
                        subscribeChannelHall(gameType, eventId)
                        mSubscribeInPlayGameID.add(eventId)
                    }
                }
                MatchType.AT_START -> {
                    tableSoonMap.forEach { (gameType, eventId) ->
                        subscribeChannelHall(gameType, eventId)
                        mSubscribeAtStartGameID.add(eventId)
                    }
                }
                else -> {
                    //do nothing
                }
            }
        }
    }

    private fun unsubscribeTableHallChannel() {
        mSubscribeInPlayGameID.forEach {
            if (!mSubscribeRecommendGameID.contains(it) && !mSubscribeHighlightGameID.contains(it)) {
                unsubscribeHallChannel(it)
            }
        }
        mSubscribeInPlayGameID.clear()
        mSubscribeAtStartGameID.forEach {
            if (!mSubscribeRecommendGameID.contains(it) && !mSubscribeHighlightGameID.contains(it)) {
                unsubscribeHallChannel(it)

            }
        }
        mSubscribeAtStartGameID.clear()
    }

    private fun unsubscribeUnSelectMatchTypeHallChannel() {
        lifecycleScope.launch {
            unsubscribeTableHallChannel()
            tableInPlayMap.clear()
            tableSoonMap.clear()
        }
    }

    //訂閱 推薦賽事 賠率
    private fun subscribeRecommendHallChannel(result: MatchRecommendResult? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (result != null) {
                result.rows?.forEach { row ->
                    row.leagueOdds?.matchOdds?.forEach { oddData ->
                        val id = oddData.matchInfo?.id ?: ""
                        if (id.isNotEmpty()) {
                            subscribeChannelHall(
                                row.sport?.code,
                                id
                            )
                            mSubscribeRecommendGameID.add(id)
                        }
                    }
                }
            } else {
                mHomeListAdapter.getRecommendData().forEach { entity ->
                    val id = entity.matchInfo?.id ?: ""
                    if (id.isNotEmpty()) {
                        subscribeChannelHall(
                            entity.code,
                            id
                        )
                        mSubscribeRecommendGameID.add(id)
                    }
                }
            }
        }
    }

    private fun unsubscribeRecommendHallChannel() {
        mSubscribeRecommendGameID.forEach {
            if (!mSubscribeInPlayGameID.contains(it) && !mSubscribeAtStartGameID.contains(it) && !mSubscribeHighlightGameID.contains(
                    it
                )
            ) {
                unsubscribeHallChannel(it)
            }
        }
        mSubscribeRecommendGameID.clear()
    }

    //訂閱 精選賽事 賠率
    private fun subscribeHighlightHallChannel(result: MatchCategoryResult? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (result != null) {
                result.t?.odds?.forEach {
                    val id = it.matchInfo?.id ?: ""
                    if (id.isNotEmpty()) {
                        subscribeChannelHall(
                            selectedSportType?.code,
                            id
                        )
                        mSubscribeHighlightGameID.add(id)
                    }
                }
            } else {
                mHomeListAdapter.getMatchOdd().forEach { matchOdd ->
                    val id = matchOdd.matchInfo?.id ?: ""
                    if (id.isNotEmpty()) {
                        subscribeChannelHall(
                            selectedSportType?.code,
                            id
                        )
                        mSubscribeHighlightGameID.add(id)
                    }
                }
            }
        }
    }

    private fun unsubscribeHighlightHallChannel() {
        mSubscribeHighlightGameID.forEach {
            if (!mSubscribeInPlayGameID.contains(it) && !mSubscribeAtStartGameID.contains(it) && !mSubscribeRecommendGameID.contains(
                    it
                )
            ) {
                unsubscribeHallChannel(it)
            }
        }
        mSubscribeHighlightGameID.clear()
    }

    private fun initObserve() {
        viewModel.userInfo.observe(viewLifecycleOwner) {
            it?.discount?.let { newDiscount ->
                with(mHomeListAdapter) {
                    setDiscount(newDiscount)
                    notifyOddsDiscountChanged(newDiscount)
                    notifyHighLightOddsDiscountChanged(newDiscount)
                }
            }
        }

        viewModel.asStartCount.observe(viewLifecycleOwner) {
            mHomeListAdapter.updateAtStartCount(it)
        }

        viewModel.sportCouponMenuResult.observe(viewLifecycleOwner) {
            it.peekContent().let { data ->
                mHomeListAdapter.updateSportCouponMenuData(data.sportCouponMenuData)
            }
        }

        viewModel.sportMenuList.observe(viewLifecycleOwner) {
            hideLoading()
            it.peekContent().let { list ->
                mHomeListAdapter.updateSportMenuData(list)
            }
        }

        //第三方遊戲清單
        viewModel.gameCateDataList.observe(viewLifecycleOwner) {
            updateInPlayUI(it)
        }

        viewModel.matchPreloadInPlay.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                mHomeGameTableBarItemData.inPlayResult = result
                isInPlayResult = true

                //若選擇滾球或初始化時
                if (mSelectMatchType == MatchType.IN_PLAY || mSelectMatchType == MatchType.MAIN && (mHomeGameTableBarItemData.inPlayResult?.matchPreloadData?.num
                        ?: 0) > 0
                ) {
                    //滾球有資料時

                    //初始化時
                    if (mSelectMatchType == MatchType.MAIN) {
                        mSelectMatchType = MatchType.IN_PLAY
                    }

                    refreshTable(mHomeGameTableBarItemData.inPlayResult)
                } else if (mSelectMatchType == MatchType.MAIN) {
                    //滾球沒資料且初始化時
                    mSelectMatchType = MatchType.AT_START
                    if (isSoonResult) {
                        //若即將開賽已經取得資料
                        refreshTable(mHomeGameTableBarItemData.atStartResult)
                    }
                }

                setupTableSelected()
                setGameTableBar()
            }
        }

        viewModel.matchPreloadAtStart.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                mHomeGameTableBarItemData.atStartResult = result
                isSoonResult = true
                if (mSelectMatchType == MatchType.AT_START && (mHomeGameTableBarItemData.atStartResult?.matchPreloadData?.num
                        ?: 0) > 0
                ) {
                    refreshTable(mHomeGameTableBarItemData.atStartResult)
                }
                setupTableSelected()
                setGameTableBar()
            }
        }

        viewModel.betIDList.observe(this.viewLifecycleOwner) { event ->
            event.peekContent()?.let {
                lifecycleScope.launch {
                    with(mHomeListAdapter) {
                        notifySelectedOddsChanged(it)
                        notifyRecommendSelectedOddsChanged(it)
                        notifyHighLightSelectedOddsChanged(it)
                    }
                }
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                lifecycleScope.launch {
                    with(mHomeListAdapter) {
                        notifyOddsTypeChanged(oddsType)
                        recommendOddsType = oddsType
                        notifyHighLightOddsTypeChanged(oddsType)
                    }
                }
            }
        }

        viewModel.recommendMatchResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                unsubscribeRecommendHallChannel() //先取消訂閱當前的推薦賽事
                refreshRecommend(result)
                subscribeRecommendHallChannel(result)
            }
        }

        viewModel.highlightMenuResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                refreshHighlightMenu(result)
            }
        }

        viewModel.highlightMatchResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                unsubscribeHighlightHallChannel() //先取消訂閱當前的賽事
                refreshHighlight(result)
                subscribeHighlightHallChannel(result)
            }
        }

        viewModel.favorMatchList.observe(viewLifecycleOwner) { favorMatchList ->
            val gameEntityData = mHomeListAdapter.getGameEntityData()
            gameEntityData.forEachIndexed { index, it ->
                it.matchOdds.forEachIndexed { adapterIndex, matchOdd ->
                    matchOdd.matchInfo?.isFavorite = favorMatchList.contains(matchOdd.matchInfo?.id)
                    mHomeListAdapter.notifySubItemChanged(index, adapterIndex)
                }
            }

            val highlightMatchOdd = mHomeListAdapter.getMatchOdd()
            highlightMatchOdd.forEach {
                val isFavorite = favorMatchList.contains(it.matchInfo?.id)
                if (it.matchInfo?.isFavorite != isFavorite) {
                    it.matchInfo?.isFavorite = isFavorite
                    mHomeListAdapter.notifyHighLightItemChanged(it)
                }
            }
        }

        viewModel.isLogin.observe(viewLifecycleOwner) {
            mHomeListAdapter.isLogin = it
        }
    }

    private fun setGameTableBar() {
        if (!isInPlayResult || !isSoonResult) return
        mHomeListAdapter.setGameTableBar(mHomeGameTableBarItemData)
        unSubscribeChannelHallAll()
        rvList.post {
            rvList?.subscribeLogic()
        }
    }

    private fun RecyclerView.subscribeLogic() {
        getVisibleRangePosition().forEach { itemPosition ->
            val viewByPosition = layoutManager?.findViewByPosition(itemPosition)
            viewByPosition?.let {
                when (getChildViewHolder(it)) {
                    is ViewHolderHdpOu -> {
                        val viewHolder = getChildViewHolder(it) as ViewHolderHdpOu
                        subscribeChannelHall(
                            viewHolder.mMatchOdd?.matchInfo?.gameType,
                            viewHolder.mMatchOdd?.matchInfo?.id
                        )
                        Log.d(
                            "[subscribe]",
                            "訂閱 " +
                                    "${viewHolder.mMatchOdd?.matchInfo?.homeName} vs " +
                                    "${viewHolder.mMatchOdd?.matchInfo?.awayName}"
                        )
                    }
                    is RecommendViewHolder -> {
                        val viewHolder = getChildViewHolder(it) as RecommendViewHolder
                        subscribeChannelHall(
                            viewHolder.mMatchOdd?.matchInfo?.gameType,
                            viewHolder.mMatchOdd?.matchInfo?.id
                        )
                        Log.d(
                            "[subscribe]",
                            "訂閱 " +
                                    "${viewHolder.mMatchOdd?.matchInfo?.homeName} vs " +
                                    "${viewHolder.mMatchOdd?.matchInfo?.awayName}"
                        )
                    }
                    is GameTableViewHolder -> {
                        val viewHolder = getChildViewHolder(it) as GameTableViewHolder
                        subscribeChannelHall(
                            viewHolder.mMatchOdd?.matchInfo?.gameType,
                            viewHolder.mMatchOdd?.matchInfo?.id
                        )
                        Log.d(
                            "[subscribe]",
                            "訂閱 " +
                                    "${viewHolder.mMatchOdd?.matchInfo?.homeName} vs " +
                                    "${viewHolder.mMatchOdd?.matchInfo?.awayName}"
                        )
                    }
                }
            }
        }
    }

    private fun initSocketObserver() {
        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    queryData()
                    subscribeSportChannelHall()
                }
            }
        }

        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            lifecycleScope.launch(Dispatchers.IO) {
                it?.let { matchStatusChangeEvent ->
                    matchStatusChangeEvent.matchStatusCO?.let { matchStatus ->
                        val statusValue = matchStatus.statusNameI18n?.get(
                            LanguageManager.getSelectLanguage(context).key
                        ) ?: matchStatus.statusName
                        //滾球盤、即將開賽盤
                        mHomeListAdapter.notifyMatchStatusChanged(matchStatus, statusValue)
                    }
                }
            }
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.matchClockCO?.let { matchClockCO ->
                lifecycleScope.launch {
                    //滾球盤、即將開賽盤
                    mHomeListAdapter.notifyUpdateTime(matchClockCO)
                }
            }
        }

        receiver.oddsChange.observe(this.viewLifecycleOwner) {
            it?.let { oddsChangeEvent ->
                var needUpdateBetInfo = false
                SocketUpdateUtil.updateMatchOdds(oddsChangeEvent)
                //滾球盤、即將開賽盤
                val filterCode = when (mSelectMatchType) {
                    MatchType.IN_PLAY -> "HOME_INPLAY_MOBILE"
                    MatchType.AT_START -> "HOME_ATSTART_MOBILE"
                    else -> null
                }

                val dataList = mHomeListAdapter.getGameEntityData()
                dataList.sortOddsMap()
                dataList.forEachIndexed { index, gameEntity ->
                    if (oddsChangeEvent.gameType != gameEntity.code) return@forEachIndexed
                    //先找出要更新的 賽事
                    val updateMatchOdd = gameEntity.matchOdds.find { matchOdd ->
                        matchOdd.matchInfo?.id == oddsChangeEvent.eventId
                    }
                    updateMatchOdd?.let { updateMatchOddNonNull ->
                        if (SocketUpdateUtil.updateMatchOdds(
                                context,
                                updateMatchOddNonNull,
                                oddsChangeEvent
                            )
                        ) {
                            val playCateCode = PlayCateMenuFilterUtils.filterOddsSort(
                                gameEntity.code,
                                filterCode
                            )//之後建enum class
                            updateMatchOddNonNull.filterMenuPlayCate(playCateCode)

                            //判斷是否有加入注單的賠率項
                            if (updateMatchOddNonNull.matchInfo?.id == oddsChangeEvent.eventId && updateMatchOddNonNull.oddsMap?.values?.any { oddList ->
                                    oddList?.any { odd ->
                                        odd?.isSelected == true
                                    } == true
                                } == true) {
                                needUpdateBetInfo = true
                            }
                            mHomeListAdapter.notifySubItemChanged(
                                index,
                                gameEntity.matchOdds.indexOf(updateMatchOdd)
                            )
                        }
                    }
                }

                //推薦賽事
                val recommendDataList = mHomeListAdapter.getRecommendData()
                recommendDataList.recommendSortOddsMap()
                recommendDataList.forEach { entity ->
                    if (oddsChangeEvent.gameType != entity.code) return@forEach
                    if (entity.matchInfo?.id != it.eventId) return@forEach
                    entity.oddBeans.forEachIndexed { oddIndex, oddBean ->
                        if (SocketUpdateUtil.updateMatchOdds(oddBean, oddsChangeEvent)) {
                            //判斷是否有加入注單的賠率項
                            if (oddBean.oddList.any { odd ->
                                    odd?.isSelected == true
                                }) {
                                needUpdateBetInfo = true
                            }
                            mHomeListAdapter.notifyRecommendSubItemChanged(entity, oddIndex)
                        }
                    }
                }

                //精選賽事
                if (oddsChangeEvent.gameType == selectedSportType?.code) {
                    val highlightDataList = mHomeListAdapter.getMatchOdd()
                    highlightDataList.highlightSortOddsMap()
                    var isUpdate = false
                    highlightDataList.forEach { updateMatchOdd ->
                        if (SocketUpdateUtil.updateMatchOdds(context, updateMatchOdd, oddsChangeEvent)) {
                            val playCateCode = PlayCateMenuFilterUtils.filterOddsSort(
                                updateMatchOdd.matchInfo?.gameType,
                                "SPECIAL_MATCH_MOBILE"
                            )//之後建enum class
                            updateMatchOdd.highlightFilterMenuPlayCate(playCateCode)

                            //判斷是否有加入注單的賠率項
                            if (updateMatchOdd.matchInfo?.id == oddsChangeEvent.eventId && updateMatchOdd.oddsMap?.values?.any { oddList ->
                                    oddList?.any { odd ->
                                        odd?.isSelected == true
                                    } == true
                                } == true) {
                                needUpdateBetInfo = true
                            }
                            mHomeListAdapter.notifyHighLightItemChanged(updateMatchOdd)
                        }
                        isUpdate = true
                    }
                }

                //投注單處於未開啟狀態時才需要透過此處去更新投注單內資訊
                if (needUpdateBetInfo && !getBetListPageVisible()) {
                    viewModel.updateMatchOdd(oddsChangeEvent)
                }
            }
        }

        receiver.leagueChange.observe(this.viewLifecycleOwner) {
            it?.let { leagueChangeEvent ->
                if (System.currentTimeMillis() - changeTime < 1000) {
                    return@observe
                }

                changeTime = System.currentTimeMillis()
                unSubscribeChannelHallAll()
//                leagueChangeEvent.leagueIdList?.let { leagueIdList ->
//                    //收到事件之后, 重新调用/api/front/sport/query用以加载上方球类选单
//                    viewModel.getLeagueOddsList(
//                        mSelectMatchType,
//                        leagueIdList,
//                        listOf(),
//                        isIncrement = true
//                    )
//                }
//                queryData(leagueChangeEvent.gameType ?: "", leagueChangeEvent.leagueIdList)
                if (mSelectMatchType == MatchType.IN_PLAY) {
                    tableInPlayMap.clear()
                    //滾球盤
                    viewModel.getMatchPreloadInPlay()
                }
                else {
                    tableSoonMap.clear()
                    //即將開賽盤
                    viewModel.getMatchPreloadAtStart()
                }
            }
        }

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLock ->
                //滾球盤、即將開賽盤
                val dataList = mHomeListAdapter.getGameEntityData()
                dataList.forEachIndexed { index, gameEntity ->
                    //先找出要更新的 賽事
                    val updateMatchOdd = gameEntity.matchOdds.find { matchOdd ->
                        matchOdd.matchInfo?.id == matchOddsLock.matchId
                    }
                    val indexMatchOdd = gameEntity.matchOdds.indexOf(updateMatchOdd)

                    //mapping 要更新的賠率
                    if (!updateMatchOdd?.oddsMap.isNullOrEmpty()) {
                        updateMatchOdd?.oddsMap?.forEach oldOddList@{ oldOddMap ->
                            oldOddMap.value?.forEach { oldOdd ->
                                if (oldOdd == null) return@oldOddList

                                oldOdd.status = BetStatus.LOCKED.code
                            }
                        }
                        mHomeListAdapter.notifySubItemChanged(index, indexMatchOdd)
                    }
                }

                //推薦賽事
                val recommendDataList = mHomeListAdapter.getRecommendData()
                recommendDataList.forEachIndexed { index, entity ->
                    if (entity.matchInfo?.id != matchOddsLock.matchId)
                        return@forEachIndexed

                    //mapping 要更新的賠率
                    if (!entity.oddBeans.isNullOrEmpty()) {
                        entity.oddBeans.forEachIndexed { indexOddBean, oddBean ->
                            val oldOddList = oddBean.oddList
                            oldOddList.forEach { oldOdd ->
                                oldOdd?.status = BetStatus.LOCKED.code

                                //20210713 紀錄：只刷新內層 viewPager 的 sub Item，才不會導致每次刷新，viewPager 都會跑到第一頁
                                mHomeListAdapter.notifyRecommendSubItemChanged(
                                    entity,
                                    indexOddBean
                                )
                            }
                        }
                    }
                }

                //精選賽事
                val highlightDataList = mHomeListAdapter.getMatchOdd()
                highlightDataList.forEachIndexed { index, updateMatchOdd ->
                    if (!updateMatchOdd.oddsMap.isNullOrEmpty()) {
                        updateMatchOdd.oddsMap?.forEach { oldOddMap ->
                            oldOddMap.value?.forEach oldOddList@{ oldOdd ->
                                if (oldOdd == null) return@oldOddList

                                oldOdd.status = BetStatus.LOCKED.code

                                mHomeListAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                //滾球盤、即將開賽盤
                mHomeListAdapter.getGameEntityData().forEachIndexed { index, gameEntity ->
                    gameEntity.matchOdds.forEachIndexed { indexMatchOdd, matchOdd ->
                        if (SocketUpdateUtil.updateOddStatus(matchOdd, globalStopEvent)) {
                            mHomeListAdapter.notifySubItemChanged(index, indexMatchOdd)
                        }
                    }
                }

                //推薦賽事
                mHomeListAdapter.getRecommendData().forEachIndexed { index, entity ->
                    entity.oddBeans.forEachIndexed { indexOddBean, oddBean ->
                        if (SocketUpdateUtil.updateOddStatus(oddBean, globalStopEvent)) {
                            mHomeListAdapter.notifyRecommendSubItemChanged(entity, indexOddBean)
                        }
                    }
                }

                //精選賽事
                mHomeListAdapter.getMatchOdd().forEachIndexed { index, matchOdd ->
                    if (SocketUpdateUtil.updateOddStatus(matchOdd, globalStopEvent)) {
                        mHomeListAdapter.notifyItemChanged(index)
                    }
                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeChannelHallAll()
                mSubscribeInPlayGameID.clear()
                mSubscribeAtStartGameID.clear()
                mSubscribeRecommendGameID.clear()
                mSubscribeHighlightGameID.clear()
                reSubscribeTableHallChannel(mSelectMatchType)
                subscribeRecommendHallChannel()
                subscribeHighlightHallChannel()
            }
        }
    }

    private fun queryData(gameType: String = "", leagueIdList: List<String>? = null) {
        changeTime = System.currentTimeMillis()
        tableInPlayMap.clear()
        tableSoonMap.clear()
        viewModel.getSportMenu()

        //滾球盤、即將開賽盤
        viewModel.getMatchPreloadInPlay()
        tableSoonMap.clear()
        viewModel.getMatchPreloadAtStart()

        //推薦賽事
        viewModel.getRecommendMatch()

        if (gameType.isNullOrEmpty() || gameType == selectedSportType?.code) {
            //精選賽事
            viewModel.getHighlightMenu()
        }
    }

    private fun updateInPlayUI(gameCateList: List<GameCateData>?) {
        lotteryCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.CGCP }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        liveCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.LIVE }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        pokerCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.QP }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        slotCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.DZ }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        fishingCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.BY }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0

        updateThirdGameCard()
    }

    private fun updateThirdGameCard() {
        mHomeListAdapter.updateThirdGameCard(
            lotteryVisible = isShowThirdGame && lotteryCount > 0,
            liveVisible = isShowThirdGame && liveCount > 0 ,
            pokerVisible = isShowThirdGame && pokerCount > 0 ,
            slotVisible = isShowThirdGame && slotCount > 0 ,
            fishingVisible = isShowThirdGame && fishingCount > 0
        )
    }

    private fun navGameOutright(gameTypeCode: String?, matchId: String?) {
        val gameType = GameType.getGameType(gameTypeCode)

        if (gameType != null && matchId != null) {
            val action =
                HomeFragmentDirections.actionHomeFragmentToGameOutrightFragment(
                    gameType,
                    matchId
                )

            findNavController().navigate(action)
        }
    }

    private fun navOddsDetailFragment(
        gameTypeCode: String?,
        matchId: String?,
        matchType: MatchType
    ) {
        val gameType = GameType.getGameType(gameTypeCode)
        if (gameType != null && matchId != null) {
            findNavController().navigate(
                when (matchType) {
                    MatchType.IN_PLAY -> {
                        HomeFragmentDirections.actionHomeFragmentToOddsDetailLiveFragment(
                            matchType,
                            gameType,
                            matchId
                        )
                    }
                    else -> {
                        HomeFragmentDirections.actionHomeFragmentToOddsDetailFragment(
                            matchType,
                            gameType,
                            matchId,
                            emptyArray() //TODO 現在沒有在詳情頁切換賽事的功能, 先補空array
                        )
                    }
                }
            )
        }
    }

    private fun navStatisticsPage(matchId: String?) {
        StatisticsDialog.newInstance(matchId, StatisticsDialog.StatisticsClickListener {
            clickMenu()
        })
            .show(childFragmentManager, StatisticsDialog::class.java.simpleName)
    }

    private fun refreshRecommend(result: MatchRecommendResult) {
        mHomeListAdapter.setRecommendData(result, viewModel.betIDList.value?.peekContent() ?: mutableListOf())
    }

    private fun refreshHighlightMenu(result: MatchCategoryResult) {
        val ary = result.t?.menu?.map { menu ->
            Item(menu.code ?: "", menu.name ?: "", 0, null, menu.sortNum ?: 0)
        } as? ArrayList<Item>
        mHomeListAdapter.setDataSport(ary ?: arrayListOf())

        if (mHomeListAdapter.getDataSport().isNotEmpty()) {
            if (selectedSportType != null) {
                selectedSportType?.let {
                    mHomeListAdapter.gameTypeListener?.onClick(it)
                }
            } else {
                //default 選擇第一個
                mHomeListAdapter.getDataSport().firstOrNull()?.let {
                    mHomeListAdapter.gameTypeListener?.onClick(it)
                }
            }
        } else {
        }
    }

    /**
     * 滾球、即將開賽賠率排序
     */
    private fun MutableList<GameEntity>.sortOddsMap() {
        this.forEach { GameEntity ->
            GameEntity.matchOdds.forEach { MatchOdd ->
                MatchOdd.oddsMap?.forEach { (key, value) ->
                    if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                        value?.sortBy {
                            it?.marketSort
                        }
                    }
                }
            }
        }
    }

    /**
     * 滾球、即將開賽 篩選玩法
     */
    private fun MatchOdd.filterMenuPlayCate(code: String?) {
        this.oddsMap?.entries?.retainAll { oddMap ->
            oddMap.key == code?.split(",")?.get(0) ?: "HDP"
        }
    }

    /**
     * 精選賽事賠率排序
     */
    private fun MutableList<MatchOdd>.highlightSortOddsMap() {
        this.forEach { MatchOdd ->
            MatchOdd.oddsMap?.forEach { (key, value) ->
                if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                    value?.sortBy {
                        it?.marketSort
                    }
                }
            }
        }
    }

    /**
     * 精選賽事 篩選玩法
     */
    private fun MatchOdd.highlightFilterMenuPlayCate(playCateCode: String?) {
        this.oddsMap?.entries?.retainAll { oddMap ->
            oddMap.key == playCateCode?.split(",")?.get(0) ?: "HDP"
        }
    }

}
