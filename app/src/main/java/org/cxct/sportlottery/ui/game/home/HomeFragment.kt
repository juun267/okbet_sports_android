package org.cxct.sportlottery.ui.game.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentHomeBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeString
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
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener
import org.cxct.sportlottery.ui.game.home.gameTable4.*
import org.cxct.sportlottery.ui.game.home.highlight.RvHighlightAdapter
import org.cxct.sportlottery.ui.game.home.recommend.RecommendGameEntity
import org.cxct.sportlottery.ui.game.home.recommend.RvRecommendAdapter
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.OddsSortUtil.recommendSortOddsMap
import org.cxct.sportlottery.util.PlayCateMenuFilterUtils
import org.cxct.sportlottery.util.SocketUpdateUtil
import timber.log.Timber
import java.util.*


/**
 * TODO simon test:
 * 1. 上下滑動 ToolBar 固定
 * 2. 賽事精選: icon 顯示 review
 * 3. 賽事推薦 - 冠軍樣式
 * 4. 賽事推薦 投注
 */
class HomeFragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var homeBinding: FragmentHomeBinding

//    private val mRvGameTable4Adapter = RvGameTable4Adapter()
    private var mSelectMatchType: MatchType = MatchType.MAIN
    private var mHomeGameTableBarItemData = HomeListAdapter.HomeGameTableBarItemData()
    /*private var mInPlayResult: MatchPreloadResult? = null
    private var mAtStartResult: MatchPreloadResult? = null*/

    /*private val mHighlightGameTypeAdapter = GameTypeAdapter()
    private val mRvHighlightAdapter = RvHighlightAdapter()*/

//    private val mRecommendAdapter = RvRecommendAdapter()

    private val mHomeListAdapter = HomeListAdapter()

    private var tableInPlayMap = mutableMapOf<String, String>()
    private var tableSoonMap = mutableMapOf<String, String>()

    private var isInPlayResult = false
    private var isSoonResult = false

    //TODO 檢查 mSubscribeInPlayGameID、mSubscribeAtStartGameID 與tableInPlayMap、tableSoonMap 的功用
    private var mSubscribeInPlayGameID : MutableList<String> = mutableListOf()
    private var mSubscribeAtStartGameID : MutableList<String> = mutableListOf()
    private var mSubscribeRecommendGameID : MutableList<String> = mutableListOf()
    private var mSubscribeHighlightGameID : MutableList<String> = mutableListOf()

    private val mOnClickOddListener = object : OnClickOddListener {
        override fun onClickBet(
            matchOdd: MatchOdd,
            odd: Odd,
            playCateCode: String,
            playCateName: String?,
            betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
        ) {
            addOddsDialog(matchOdd, odd, playCateCode, playCateName, betPlayCateNameMap)
        }
    }

    private val mOnSubscribeChannelHallListener = object : OnSubscribeChannelHallListener {
        override fun subscribeChannel(
            gameType: String?,
            cateMenuCode: String?,
            eventId: String?
        ) {
            if (gameType.isNullOrEmpty()) return
            val id = if (mSelectMatchType == MatchType.IN_PLAY) tableInPlayMap[gameType] else tableSoonMap[gameType]
            if (id == eventId) return
            if (!id.isNullOrEmpty()) {
                unSubscribeChannelHall(gameType, cateMenuCode, id)
            }
            if (mSelectMatchType == MatchType.IN_PLAY) {
                mSubscribeInPlayGameID.remove(id)
                eventId?.let {
                    tableInPlayMap[gameType] = it
                    mSubscribeInPlayGameID.add(it)
                }
            }
            else {
                mSubscribeAtStartGameID.remove(id)
                eventId?.let {
                    tableSoonMap[gameType] = it
                    mSubscribeAtStartGameID.add(it)
                }
            }
            subscribeChannelHall(gameType, eventId)
        }
    }

    private val isShowThirdGame = sConfigData?.thirdOpen == FLAG_OPEN
    private var lotteryCount = 0
    private var liveCount = 0
    private var pokerCount = 0
    private var slotCount = 0
    private var fishingCount = 0
    private var isCreditAccount = false
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
            initGameRecommendBar()
//            queryData()
            initDiscount()
            initTable()
            initRecommend()
            initHighLightBar()
            initHighLightTitle()
            initHighlight()
            initEvent()
            initObserve()
            initSocketObserver()
            initBottomNavigation()
//            queryData()
            mTimer = Timer()
            mTimer?.schedule(object : TimerTask() {
                override fun run() {
                    lifecycleScope.launch {
                        with(mHomeListAdapter) {
                            notifyTimeChanged(1)
                            notifyHighLightTimeChanged(1)
                        }
                        /*mRvGameTable4Adapter.notifyTimeChanged(1)
                        mRvHighlightAdapter.notifyTimeChanged(1)*/
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
        rvList.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        rvList.adapter = mHomeListAdapter
        rvList.itemAnimator = null
    }

    private fun initDiscount() {
        val discount = viewModel.userInfo.value?.discount ?: 1.0F
        with(mHomeListAdapter) {
            setDiscount(discount)
            notifyOddsDiscountChanged(discount)
            notifyHighLightOddsDiscountChanged(discount)
        }
        /*mRvGameTable4Adapter.notifyOddsDiscountChanged(discount)
        mRecommendAdapter.discount = discount
        mRvHighlightAdapter.notifyOddsDiscountChanged(discount)*/
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

    private fun initGameRecommendBar() {
        mHomeListAdapter.setGameRecommendBar()
    }

    private fun initTable() {
//        judgeTableBar()
//        rv_game_table.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
//        rv_game_table.adapter = mRvGameTable4Adapter

        mHomeListAdapter.onSubscribeChannelHallListener = mOnSubscribeChannelHallListener
        mHomeListAdapter.onClickOddListener = object : OnClickOddListener {
            override fun onClickBet(matchOdd: MatchOdd, odd: Odd, playCateCode: String, playCateName: String?,
                                    betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?) {
                addOddsDialog(matchOdd, odd, playCateCode, playCateName, betPlayCateNameMap)
            }
        }

        /*mRvGameTable4Adapter.onSubscribeChannelHallListener = mOnSubscribeChannelHallListener
        mRvGameTable4Adapter.onClickOddListener = object : OnClickOddListener {
            override fun onClickBet(matchOdd: MatchOdd, odd: Odd, playCateCode: String, playCateName: String?,
                                    betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?) {
                addOddsDialog(matchOdd, odd, playCateCode, playCateName, betPlayCateNameMap)
            }
        }*/

        mHomeListAdapter.onClickMatchListener = object : OnSelectItemListener<MatchInfo> {
            override fun onClick(select: MatchInfo) {
//                scroll_view.smoothScrollTo(0, 0)
                val code = select.gameType
                val matchId = select.id
                navOddsDetailFragment(code, matchId, mSelectMatchType)
            }
        }

        /*mRvGameTable4Adapter.onClickMatchListener = object : OnSelectItemListener<MatchInfo> {
            override fun onClick(select: MatchInfo) {
                scroll_view.smoothScrollTo(0, 0)
                val code = select.gameType
                val matchId = select.id
                navOddsDetailFragment(code, matchId, mSelectMatchType)
            }
        }*/

        mHomeListAdapter.onClickTotalMatchListener = object : OnSelectItemListener<GameEntity> {
            override fun onClick(select: GameEntity) {
                viewModel.navSpecialEntrance(
                    mSelectMatchType,
                    GameType.getGameType(select.code)
                )
            }
        }

        /*mRvGameTable4Adapter.onClickTotalMatchListener = object : OnSelectItemListener<GameEntity> {
            override fun onClick(select: GameEntity) {
                scroll_view.smoothScrollTo(0, 0)
                viewModel.navSpecialEntrance(
                    mSelectMatchType,
                    GameType.getGameType(select.code)
                )
            }
        }*/

        mHomeListAdapter.onClickSportListener = object : OnSelectItemListener<OtherMatch> {
            override fun onClick(select: OtherMatch) {
//                scroll_view.smoothScrollTo(0, 0)
                viewModel.navSpecialEntrance(
                    mSelectMatchType,
                    GameType.getGameType(select.code)
                )
            }
        }

        /*mRvGameTable4Adapter.onClickSportListener = object : OnSelectItemListener<OtherMatch> {
            override fun onClick(select: OtherMatch) {
                scroll_view.smoothScrollTo(0, 0)
                viewModel.navSpecialEntrance(
                    mSelectMatchType,
                    GameType.getGameType(select.code)
                )
            }
        }*/

        mHomeListAdapter.onClickFavoriteListener = object : OnClickFavoriteListener {
            override fun onClickFavorite(matchId: String?) {
                viewModel.pinFavorite(FavoriteType.MATCH, matchId)
            }
        }

        /*mRvGameTable4Adapter.onClickFavoriteListener = object : OnClickFavoriteListener {
            override fun onClickFavorite(matchId: String?) {
                viewModel.pinFavorite(FavoriteType.MATCH, matchId)
            }
        }*/

        mHomeListAdapter.onClickStatisticsListener = object : OnClickStatisticsListener {
            override fun onClickStatistics(matchId: String?) {
                navStatisticsPage(matchId)
            }
        }

        /*mRvGameTable4Adapter.onClickStatisticsListener = object : OnClickStatisticsListener {
            override fun onClickStatistics(matchId: String?) {
                navStatisticsPage(matchId)
            }
        }*/

/*        rb_in_play.setOnClickListener {
            if (mSelectMatchType == MatchType.IN_PLAY) return@setOnClickListener
            if (mSelectMatchType != MatchType.MAIN) {
                unsubscribeUnSelectMatchTypeHallChannel()
                viewModel.getMatchPreloadInPlay()
            }
            mSelectMatchType = MatchType.IN_PLAY
            viewModel.getMatchPreloadInPlay()
            refreshTable(mInPlayResult)
        }

        rb_as_start.setOnClickListener {
            if (mSelectMatchType == MatchType.AT_START) return@setOnClickListener
            if (mSelectMatchType != MatchType.MAIN) {
                unsubscribeUnSelectMatchTypeHallChannel()
                viewModel.getMatchPreloadAtStart()
            }
            mSelectMatchType = MatchType.AT_START
            viewModel.getMatchPreloadAtStart()
            refreshTable(mAtStartResult)
        }*/
    }

    private fun initRecommend() {
        updateRecommendVisibility(false)
//        rv_recommend.adapter = mRecommendAdapter
        mHomeListAdapter.onRecommendClickOddListener = mOnClickOddListener
        mHomeListAdapter.onRecommendClickOutrightOddListener = object : OnClickOddListener {
            override fun onClickBet(
                matchOdd: MatchOdd, odd: Odd, playCateCode: String, playCateName: String?,
                betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
            ) {
                GameType.getGameType(matchOdd.matchInfo?.gameType)?.let { gameType ->
                    viewModel.updateMatchBetListForOutRight(
                        matchType = MatchType.OUTRIGHT,
                        gameType = gameType,
                        playCateCode = playCateCode,
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
                    )
                }
            }
        }

        /*mRecommendAdapter.onClickOddListener = mOnClickOddListener
        mRecommendAdapter.onClickOutrightOddListener = object : OnClickOddListener {
            override fun onClickBet(matchOdd: MatchOdd, odd: Odd, playCateCode: String, playCateName: String?,
                                    betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?) {
                GameType.getGameType(matchOdd.matchInfo?.gameType)?.let { gameType ->
                    viewModel.updateMatchBetListForOutRight(
                        matchType = MatchType.OUTRIGHT,
                        gameType = gameType,
                        playCateCode = playCateCode,
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
                    )
                }
            }
        }*/

        mHomeListAdapter.onRecommendClickMoreListener = object : OnClickMoreListener {
            override fun onClickMore(oddsKey: String, matchOdd: MatchOdd) {
//                scroll_view.smoothScrollTo(0, 0)

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

        /*mRecommendAdapter.onClickMoreListener = object : OnClickMoreListener {
            override fun onClickMore(oddsKey: String, matchOdd: MatchOdd) {
                scroll_view.smoothScrollTo(0, 0)

                val action = HomeFragmentDirections.actionHomeFragmentToGameOutrightMoreFragment(oddsKey,
                        org.cxct.sportlottery.network.outright.odds.MatchOdd(
                            matchInfo = matchOdd.matchInfo,
                            oddsMap = matchOdd.oddsMap ?: mutableMapOf(),
                            dynamicMarkets = matchOdd.dynamicMarkets ?: mapOf(),
                            oddsList = listOf(),
                            quickPlayCateList = matchOdd.quickPlayCateList,
                            betPlayCateNameMap= matchOdd.betPlayCateNameMap,
                            playCateNameMap = matchOdd.playCateNameMap
                        )
                    )
                findNavController().navigate(action)
            }
        }*/

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

        /*mRecommendAdapter.onClickMatchListener = object : OnSelectItemListener<RecommendGameEntity> {
                override fun onClick(select: RecommendGameEntity) {
                    scroll_view.smoothScrollTo(0, 0)
                    val code = select.code
                    val matchId = select.matchInfo?.id

                    if (select.isOutright == RECOMMEND_OUTRIGHT) {
                        navGameOutright(select.code, select.leagueId)
                    } else {
                        //TODO simon test review 推薦賽事是不是一定是 MatchType.TODAY
                        navOddsDetailFragment(code, matchId, MatchType.TODAY)
                    }
                }
            }*/
    }

    private fun initHighLightBar() {
        mHomeListAdapter.setGameHighLightBar()
    }

    private fun initHighLightTitle() {
        mHomeListAdapter.setGameHighLightTitle()
    }

    private fun initHighlight() {
        updateHighlightVisibility(false)
        mHomeListAdapter.gameTypeListener = GameTypeListener { selectItem ->
            selectedSportType = selectItem

            mHomeListAdapter.updateHightLightTitle(selectItem)

//            unsubscribeHighlightHallChannel() //先取消訂閱當前的精選賽事

            mHomeListAdapter.getDataSport().forEach { item ->
                item.isSelected = item.code == selectItem.code
            }
            mHomeListAdapter.updateDataSport()
            //mHomeListAdapter.notifyDataSetChanged()
            viewModel.getHighlightMatch(selectItem.code)
        }

        mHomeListAdapter.onHighLightClickOddListener = mOnClickOddListener
        mHomeListAdapter.onHighLightClickMatchListener = object : OnSelectItemListener<MatchOdd> {
            override fun onClick(select: MatchOdd) {
                //scroll_view.smoothScrollTo(0, 0)
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
        /*updateHighlightVisibility(false)
        rv_highlight_sport_type.adapter = mHighlightGameTypeAdapter
        mHighlightGameTypeAdapter.gameTypeListener = GameTypeListener { selectItem ->
            selectedSportType = selectItem
            highlight_tv_game_name.text = context?.let { getGameTypeString(it, selectItem.code) } ?: selectItem.name

            GameConfigManager.getGameIcon(selectItem.code)?.let {
                highlight_iv_game_icon.setImageResource(it)
            }

            GameConfigManager.getTitleBarBackground(selectItem.code)?.let {
                highlight_titleBar.setBackgroundResource(it)
            }

            mHighlightGameTypeAdapter.dataSport.forEach { item ->
                item.isSelected = item.code == selectItem.code
            }
            mHighlightGameTypeAdapter.notifyDataSetChanged()
            viewModel.getHighlightMatch(selectItem.code)
        }
        mHighlightGameTypeAdapter.isFromHome = true

        rv_game_highlight.adapter = mRvHighlightAdapter
        mRvHighlightAdapter.onClickOddListener = mOnClickOddListener
        mRvHighlightAdapter.onClickMatchListener = object : OnSelectItemListener<MatchOdd> {
            override fun onClick(select: MatchOdd) {
                scroll_view.smoothScrollTo(0, 0)
                val code = select.matchInfo?.gameType
                val matchId = select.matchInfo?.id

                //TODO simon test review 精選賽事是不是一定是 MatchType.TODAY
                navOddsDetailFragment(code, matchId, MatchType.TODAY)
            }
        }

        mRvHighlightAdapter.onClickFavoriteListener = object : OnClickFavoriteListener {
            override fun onClickFavorite(matchId: String?) {
                viewModel.pinFavorite(FavoriteType.MATCH, matchId)
            }
        }

        mRvHighlightAdapter.onClickStatisticsListener = object : OnClickStatisticsListener {
            override fun onClickStatistics(matchId: String?) {
                navStatisticsPage(matchId)
            }
        }*/
    }

    private fun refreshTable(result: MatchPreloadResult?) {


    //TODO 目前會訂閱當前看見的賽事，此處需在檢查訂閱、解除訂閱的邏輯
    /*//先清除之前訂閱項目
        unsubscribeTableHallChannel()*/

        val gameDataList: MutableList<GameEntity> = mutableListOf()
        val otherMatchList: MutableList<OtherMatch> = mutableListOf()
        result?.matchPreloadData?.datas?.forEach { data ->
            if (data.matchOdds.isNotEmpty()) {
                val gameEntity = GameEntity(data.code, data.name, data.num, data.matchOdds.toMutableList(), data.playCateNameMap)
                gameDataList.add(gameEntity)
            } else {
                val otherMatch = OtherMatch(data.code, data.name, data.num)
                otherMatchList.add(otherMatch)
            }
        }
        if(!otherMatchList.isNullOrEmpty()){
            val otherGameEntity = GameEntity(null, null, 0, mutableListOf(), mutableMapOf(), otherMatchList)
            gameDataList.add(otherGameEntity)
        }

        gameDataList.sortOddsMap()
        mHomeListAdapter.setGameTableData(gameDataList, mSelectMatchType, viewModel.betIDList.value?.peekContent() ?: mutableListOf())
//        mRvGameTable4Adapter.setData(gameDataList, mSelectMatchType, viewModel.betIDList.value?.peekContent() ?: mutableListOf())
        //TODO 目前會訂閱當前看見的賽事，此處需在檢查訂閱、解除訂閱的邏輯
//        subscribeTableHallChannel(mSelectMatchType)
    }

    //TableBar 判斷是否隱藏
    private fun judgeTableBar() {
        /*val inPlayCount = mInPlayResult?.matchPreloadData?.num ?: 0
        val atStartCount = mAtStartResult?.matchPreloadData?.num ?: 0
        rg_table_bar.visibility =
            if (inPlayCount == 0 && atStartCount == 0) View.GONE else View.VISIBLE
        rb_in_play.visibility = if (inPlayCount == 0) View.GONE else View.VISIBLE
        rb_as_start.visibility = if (atStartCount == 0) View.GONE else View.VISIBLE

        if (!isInPlayResult || !isSoonResult) return
        if (inPlayCount != 0 && atStartCount != 0) {
            if (mSelectMatchType == MatchType.IN_PLAY || mSelectMatchType == MatchType.MAIN) {
                rb_in_play?.performClick()
            }
            else if (mSelectMatchType == MatchType.AT_START) {
                rb_as_start?.performClick()
            }
        }
        else if (inPlayCount != 0) {
            if (mSelectMatchType != MatchType.IN_PLAY) rb_in_play?.performClick()
        }
        else {
            if (mSelectMatchType != MatchType.AT_START) rb_as_start?.performClick()
        }*/
    }

    /*private fun setDefaultRb() {
        val inPlayCount = mInPlayResult?.matchPreloadData?.num ?: 0
        if (inPlayCount != 0) {
            rb_in_play?.performClick()
        } else {
            rb_as_start?.performClick()
        }
    }*/

    private fun refreshHighlight(result: MatchCategoryResult?) {
        val sportCode = mHomeListAdapter.getDataSport().find { it.isSelected }?.code ?: ""
        mHomeListAdapter.setMatchOdd(
            sportCode,
            result?.t?.odds,
            viewModel.betIDList.value?.peekContent() ?: mutableListOf(),
            result?.t?.playCateNameMap
        )
//        mRvHighlightAdapter.setData(sportCode, result?.t?.odds, viewModel.betIDList.value?.peekContent() ?: mutableListOf(), result?.t?.playCateNameMap)
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
                viewModel.updateMatchBetList(
                    mSelectMatchType,
                    gameType,
                    playCateCode,
                    playCateName ?: "",
                    matchInfo,
                    odd,
                    ChannelType.HALL,
                    betPlayCateNameMap,
                    if (mSelectMatchType == MatchType.IN_PLAY || mSelectMatchType == MatchType.MAIN) MenuCode.HOME_INPLAY_MOBILE.code else MenuCode.HOME_ATSTART_MOBILE.code
                )
            }
        }
    }

    private fun initEvent() {
        card_game_soon.setOnClickListener {
            viewModel.navSpecialEntrance(MatchType.AT_START, null)
        }

        card_lottery.setOnClickListener {
            navThirdGame(ThirdGameCategory.CGCP)
        }

        card_live.setOnClickListener {
            navThirdGame(ThirdGameCategory.LIVE)
        }

        card_poker.setOnClickListener {
            navThirdGame(ThirdGameCategory.QP)
        }

        card_slot.setOnClickListener {
            navThirdGame(ThirdGameCategory.DZ)
        }

        card_fishing.setOnClickListener {
            navThirdGame(ThirdGameCategory.BY)
        }

        card_game_result.setOnClickListener {
            startActivity(Intent(activity, ResultsSettlementActivity::class.java))
        }

        card_update.setOnClickListener {
            startActivity(Intent(activity, VersionUpdateActivity::class.java))
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
            }
            else {
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
        /*lifecycleScope.launch {
            mHomeListAdapter.getRecommendData().forEach { entity ->
                unSubscribeChannelHall(
                    entity.code,
                    MenuCode.RECOMMEND.code,
                    entity.matchInfo?.id
                )*/
        mSubscribeRecommendGameID.forEach {
            if (!mSubscribeInPlayGameID.contains(it) && !mSubscribeAtStartGameID.contains(it) && !mSubscribeHighlightGameID.contains(it)) {
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
                }/*.apply {
                    withContext(Dispatchers.Main) {
                        setDefaultRb()
                    }
                }*/
            }
            else {
                mHomeListAdapter.getMatchOdd().forEach { matchOdd ->
                    val id = matchOdd.matchInfo?.id ?: ""
                    if (id.isNotEmpty()) {
                        subscribeChannelHall(
                            selectedSportType?.code,
                            id
                        )
                        mSubscribeHighlightGameID.add(id)
                    }
                }/*.apply {
                    withContext(Dispatchers.Main) {
                        setDefaultRb()
                    }
                }*/
            }
        }
    }

    private fun unsubscribeHighlightHallChannel() {
        mSubscribeHighlightGameID.forEach {
            if (!mSubscribeInPlayGameID.contains(it) && !mSubscribeAtStartGameID.contains(it) && !mSubscribeRecommendGameID.contains(it)) {
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
                /*mRvGameTable4Adapter.notifyOddsDiscountChanged(newDiscount)
                mRecommendAdapter.discount = newDiscount
                mRvHighlightAdapter.notifyOddsDiscountChanged(newDiscount)*/
            }
        }
        viewModel.sportCouponMenuResult.observe(viewLifecycleOwner) {
            it.peekContent().let { data ->
                if (special_block_game.size != data.sportCouponMenuData.size) {
                    special_block_game.removeAllViews()
                    data.sportCouponMenuData.forEach {sportCouponMenuData ->
                        special_block_game.addView(HomeGameCard(context ?: requireContext()).apply {
                            this.apply {
                                setTitle(sportCouponMenuData.couponName)
                                setIcon(R.drawable.ic_game_champ)
                                setOnClickListener {
                                    viewModel.navSpecialEntrance(MatchType.OTHER, null,sportCouponMenuData.couponCode,sportCouponMenuData.couponName)
                                }
                            }
                        })
                    }
                } else {
                    data.sportCouponMenuData.forEachIndexed { index, sportCouponMenuData ->
                        HomeGameCard(context ?: requireContext()).apply {
                            (special_block_game.getChildAt(index) as HomeGameCard).apply {
                                setTitle(sportCouponMenuData.couponName)
                                setIcon(R.drawable.ic_game_champ)
                                setOnClickListener {
                                    viewModel.navSpecialEntrance(MatchType.OTHER, null,sportCouponMenuData.couponCode,sportCouponMenuData.couponName)
                                }
                            }
                        }
                    }
                }
            }
        }

        viewModel.sportMenuList.observe(viewLifecycleOwner) {
            hideLoading()
            it.peekContent().let { list ->
                if (block_game.size != list.size) {
                    block_game.removeAllViews()

                    list.forEachIndexed { index, sportMenu ->
                        when (index) {
                            0 -> setupFirstGame(sportMenu)
                            1 -> setupSecondGame(sportMenu)
                            else -> {
                                if(sportMenu.gameCount > 0){
                                    block_game.addView(HomeGameCard(context ?: requireContext()).apply {
                                        setupHomeCard(this, sportMenu)
                                    })
                                }
                            }
                        }
                    }
                } else {
                    list.forEachIndexed { index, sportMenu ->
                        when (index) {
                            0 -> setupFirstGame(sportMenu)
                            1 -> setupSecondGame(sportMenu)
                            else -> {
                                if(sportMenu.gameCount > 0) {
                                    setupHomeCard(
                                        (block_game.getChildAt(index) as HomeGameCard),
                                        sportMenu
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        //第三方遊戲清單
        with(viewModel) {
            gameCateDataList.observe(viewLifecycleOwner) {
                updateInPlayUI(it)
            }

            matchPreloadInPlay.observe(viewLifecycleOwner) {
                it.getContentIfNotHandled()?.let { result ->
                    mHomeGameTableBarItemData.inPlayResult = result
                    isInPlayResult = true
                    setGameTableBar()
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
                    /*mInPlayResult = result
                    judgeTableBar()*/
                }
            }

            matchPreloadAtStart.observe(viewLifecycleOwner) {
                it.getContentIfNotHandled()?.let { result ->
                    mHomeGameTableBarItemData.atStartResult = result
                    isSoonResult = true
                    setGameTableBar()
                    if (mSelectMatchType == MatchType.AT_START && (mHomeGameTableBarItemData.atStartResult?.matchPreloadData?.num
                            ?: 0) > 0
                    ) {
                        refreshTable(mHomeGameTableBarItemData.atStartResult)
                    }
                    /*mAtStartResult = result
                    judgeTableBar()*/
                }
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
            /*lifecycleScope.launch {
                mRvGameTable4Adapter.notifySelectedOddsChanged(it.peekContent())
                mRecommendAdapter.notifySelectedOddsChanged(it.peekContent())
                mRvHighlightAdapter.notifySelectedOddsChanged(it.peekContent())
            }*/
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
                /*lifecycleScope.launch {
                    mRvGameTable4Adapter.notifyOddsTypeChanged(oddsType)
                    mRecommendAdapter.oddsType = oddsType
                    mRvHighlightAdapter.notifyOddsTypeChanged(oddsType)
                }*/
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
            mHomeListAdapter.getGameEntityData().forEach {
                it.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.isFavorite = favorMatchList.contains(matchOdd.matchInfo?.id)
                }
            }
            /*mRvGameTable4Adapter.getData().forEach {
                it.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.isFavorite = favorMatchList.contains(matchOdd.matchInfo?.id)
                }
            }*/

            mHomeListAdapter.getMatchOdd().forEach {
                it.matchInfo?.isFavorite = favorMatchList.contains(it.matchInfo?.id)
            }

//            mRvHighlightAdapter.notifyDataSetChanged()
        }

        viewModel.isCreditAccount.observe(viewLifecycleOwner) {
            isCreditAccount = it
            updateThirdGameCard()
        }

        viewModel.isLogin.observe(viewLifecycleOwner) {
            mHomeListAdapter.isLogin = it
//            mRvGameTable4Adapter.isLogin = it
        }
    }

    private fun setGameTableBar() {
        if (!isInPlayResult || !isSoonResult) return
        mHomeListAdapter.setGameTableBar(mHomeGameTableBarItemData)
    }

    private fun setupFirstGame(sportMenu: SportMenu) {
        label_en_first_game.text = context?.getString(R.string.goal_buster)
        label_first_game.text = sportMenu.sportName
        sportMenu.icon?.let { iv_first_game.setImageResource(sportMenu.icon) }
        tv_first_game_count.text = sportMenu.gameCount.toString()

        card_first_game.setOnClickListener {
            if (sportMenu.entranceType != null) {
                sportMenu.entranceType?.let {
                    viewModel.navSpecialEntrance(
                        it,
                        sportMenu.gameType
                    )
                }
            } else {
                viewModel.setSportClosePromptMessage(getString(GameType.TN.string))
            }
        }
    }

    private fun setupSecondGame(sportMenu: SportMenu) {
        label_en_second_game.text = context?.getString(R.string.top_games)
        label_second_game.text = sportMenu.sportName
        sportMenu.icon?.let { iv_second_game.setImageResource(sportMenu.icon) }
        tv_second_game_count.text = sportMenu.gameCount.toString()

        card_second_game.setOnClickListener {
            if (sportMenu.entranceType != null) {
                sportMenu.entranceType?.let {
                    viewModel.navSpecialEntrance(it, sportMenu.gameType)
                }
            } else {
                viewModel.setSportClosePromptMessage(getString(GameType.TN.string))
            }
        }
    }

    private fun setupHomeCard(homeGameCard: HomeGameCard, sportMenu: SportMenu) {
        homeGameCard.apply {
            setTitle(sportMenu.sportName)
            sportMenu.icon?.let { setIcon(sportMenu.icon) }
            setCount(sportMenu.gameCount)

            setOnClickListener {
                if (sportMenu.entranceType != null) {
                    sportMenu.entranceType?.let {
                        viewModel.navSpecialEntrance(it, sportMenu.gameType)
                    }
                } else {
                    viewModel.setSportClosePromptMessage(getString(GameType.TN.string))
                }
            }
        }
    }

    private fun initSocketObserver() {
        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    queryData()
                }
            }
        }

        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            lifecycleScope.launch(Dispatchers.IO) {
                it?.let { matchStatusChangeEvent ->
                    matchStatusChangeEvent.matchStatusCO?.let { matchStatus ->
                        val statusValue = matchStatus.statusNameI18n?.get(LanguageManager.getSelectLanguage(context).key) ?: matchStatus.statusName
                        //滾球盤、即將開賽盤
                        mHomeListAdapter.notifyMatchStatusChanged(matchStatus, statusValue)
                    }
                }
            }
            /*lifecycleScope.launch {
                it?.let { matchStatusChangeEvent ->
                    matchStatusChangeEvent.matchStatusCO?.let { matchStatus ->
                        val statusValue = matchStatus.statusNameI18n?.get(LanguageManager.getSelectLanguage(context).key) ?: matchStatus.statusName
                        //滾球盤、即將開賽盤
                        mRvGameTable4Adapter.notifyMatchStatusChanged(matchStatus, statusValue)
                    }
                }
            }*/
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.matchClockCO?.let { matchClockCO ->
                lifecycleScope.launch {
                    //滾球盤、即將開賽盤
                    mHomeListAdapter.notifyUpdateTime(matchClockCO)
//                    mRvGameTable4Adapter.notifyUpdateTime(matchClockCO)
                }
            }
        }

        receiver.oddsChange.observe(this.viewLifecycleOwner) {
            it?.let { oddsChangeEvent ->
                //滾球盤、即將開賽盤
                val filterCode = when (mSelectMatchType) {
                    MatchType.IN_PLAY -> "HOME_INPLAY_MOBILE"
                    MatchType.AT_START -> "HOME_ATSTART_MOBILE"
                    else -> null
                }

                val dataList = mHomeListAdapter.getGameEntityData()
                dataList.sortOddsMap()
                dataList.forEachIndexed { index, gameEntity ->
                    //先找出要更新的 賽事
                    val updateMatchOdd = gameEntity.matchOdds.find { matchOdd ->
                        matchOdd.matchInfo?.id == oddsChangeEvent.eventId
                    }
                    updateMatchOdd?.let { updateMatchOddNonNull ->
                        if (SocketUpdateUtil.updateMatchOdds(context, updateMatchOddNonNull, oddsChangeEvent)) {
                            val playCateCode = PlayCateMenuFilterUtils.filterOddsSort(gameEntity.code, filterCode)//之後建enum class
                            updateMatchOddNonNull.filterMenuPlayCate(playCateCode)
                        }

                        mHomeListAdapter.notifySubItemChanged(
                            index,
                            gameEntity.matchOdds.indexOf(updateMatchOdd)
                        )
//                                    gameEntity.vpTableAdapter?.notifyDataSetChanged()
                    }
                }

                //推薦賽事
                val recommendDataList = mHomeListAdapter.getRecommendData()
                recommendDataList.recommendSortOddsMap()
                recommendDataList.forEach { entity ->
                    if (entity.matchInfo?.id != it.eventId) return@forEach
                    var isUpdate = false
                    entity.oddBeans.forEach { oddBean ->
                        if (SocketUpdateUtil.updateMatchOdds(oddBean, oddsChangeEvent)) {
                            isUpdate = true
                        }
                    }
                    /*if (isUpdate) {
                        Handler(Looper.getMainLooper()).post {
                            entity.vpRecommendAdapter?.notifyDataSetChanged()
                        }
                    }*/
                }

                //精選賽事
                val highlightDataList = mHomeListAdapter.getMatchOdd()
                highlightDataList.highlightSortOddsMap()
                var isUpdate = false
                highlightDataList.forEach { updateMatchOdd ->
                    if (SocketUpdateUtil.updateMatchOdds(context, updateMatchOdd, oddsChangeEvent)) {
                        val playCateCode = PlayCateMenuFilterUtils.filterOddsSort(
                            updateMatchOdd.matchInfo?.gameType,
                            filterCode
                        )//之後建enum class
                        updateMatchOdd.highlightFilterMenuPlayCate(playCateCode)
                    }
                    isUpdate = true
                }
                /*if (isUpdate) {
                            Handler(Looper.getMainLooper()).post {
                                mRvHighlightAdapter.notifyDataSetChanged()
                            }
                        }*/
            }
        }

        receiver.leagueChange.observe(this.viewLifecycleOwner) {
            it?.let { leagueChangeEvent ->
                unSubscribeChannelHallAll()
                leagueChangeEvent.leagueIdList?.let { leagueIdList ->
                    //收到事件之后, 重新调用/api/front/sport/query用以加载上方球类选单
                    viewModel.getLeagueOddsList(mSelectMatchType, leagueIdList, listOf(), isIncrement = true)
                }
                queryData()
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
                                    index,
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
                            mHomeListAdapter.notifyRecommendSubItemChanged(index, indexOddBean)
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

    private fun queryData() {
        tableInPlayMap.clear()
        tableSoonMap.clear()
        viewModel.getSportMenu()

        //滾球盤、即將開賽盤
        viewModel.getMatchPreloadInPlay()
        viewModel.getMatchPreloadAtStart()

        //推薦賽事
        viewModel.getRecommendMatch()

        //精選賽事
        viewModel.getHighlightMenu()
    }

    private fun updateInPlayUI(gameCateList: List<GameCateData>?) {
        lotteryCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.CGCP }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        liveCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.LIVE }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        pokerCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.QP }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        slotCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.DZ }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        fishingCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.BY }?.tabDataList?.sumBy { it.gameList.size } ?: 0

        updateThirdGameCard()
    }

    private fun updateThirdGameCard() {
        card_lottery.visibility = if (isShowThirdGame && lotteryCount > 0 && !isCreditAccount) View.VISIBLE else View.GONE
        card_live.visibility = if (isShowThirdGame && liveCount > 0 && !isCreditAccount) View.VISIBLE else View.GONE
        card_poker.visibility = if (isShowThirdGame && pokerCount > 0 && !isCreditAccount) View.VISIBLE else View.GONE
        card_slot.visibility = if (isShowThirdGame && slotCount > 0 && !isCreditAccount) View.VISIBLE else View.GONE
        card_fishing.visibility = if (isShowThirdGame && fishingCount > 0 && !isCreditAccount) View.VISIBLE else View.GONE
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
                HomeFragmentDirections.actionHomeFragmentToOddsDetailLiveFragment(
                    matchType,
                    gameType,
                    matchId
                )
            )
        }
    }

    private fun navStatisticsPage(matchId: String?) {
        StatisticsDialog.newInstance(matchId).show(childFragmentManager, StatisticsDialog::class.java.simpleName)
    }

    private fun refreshRecommend(result: MatchRecommendResult) {
        mHomeListAdapter.setRecommendData(result, viewModel.betIDList.value?.peekContent() ?: mutableListOf())
//        mRecommendAdapter.setData(result, viewModel.betIDList.value?.peekContent() ?: mutableListOf())
        updateRecommendVisibility((result.rows?.size ?: 0) > 0)
    }

    private fun refreshHighlightMenu(result: MatchCategoryResult) {
        val ary = result.t?.menu?.map { menu ->
            Item(menu.code ?: "", menu.name ?: "", 0, null, menu.sortNum ?: 0)
        } as ArrayList<Item>
        mHomeListAdapter.setDataSport(ary)
        //mHomeListAdapter.notifyDataSetChanged()

        if (mHomeListAdapter.getDataSport().isNotEmpty()) {
            if (selectedSportType != null) {
                selectedSportType?.let {
                    mHomeListAdapter.gameTypeListener?.onClick(it)
                }
            }
            else {
                //default 選擇第一個
                mHomeListAdapter.getDataSport().firstOrNull()?.let {
                    mHomeListAdapter.gameTypeListener?.onClick(it)
                }
            }
            updateHighlightVisibility(true)
        } else {
            updateHighlightVisibility(false)
        }
        /*mHighlightGameTypeAdapter.dataSport = result.t?.menu?.map { menu ->
            Item(menu.code ?: "", menu.name ?: "", 0, null, menu.sortNum ?: 0)
        } ?: listOf()

        if (mHighlightGameTypeAdapter.dataSport.isNotEmpty()) {
            if (selectedSportType != null) {
                selectedSportType?.let {
                    mHighlightGameTypeAdapter.gameTypeListener?.onClick(it)
                }
            }
            else {
                //default 選擇第一個
                mHighlightGameTypeAdapter.dataSport.firstOrNull()?.let {
                    mHighlightGameTypeAdapter.gameTypeListener?.onClick(it)
                }
            }
            updateHighlightVisibility(true)
        } else {
            updateHighlightVisibility(false)
        }*/
    }

    private fun updateRecommendVisibility(show: Boolean) {
        //TODO recommend_bar.isVisible = show
    }

    private fun updateHighlightVisibility(show: Boolean) {
        //TODO highlight_bar.isVisible = show
        //TODO highlight_titleBar.isVisible = show
    }

    /**
     * 滾球、即將開賽賠率排序
     */
    private fun MutableList<GameEntity>.sortOddsMap() {
        this.forEach { GameEntity ->
            GameEntity.matchOdds.forEach { MatchOdd ->
                MatchOdd.oddsMap?.forEach { (key , value) ->
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
        this.oddsMap?.entries?.retainAll { oddMap -> oddMap.key == code?.split(",")?.get(0) ?: "HDP" }
    }

    /**
     * 推薦賽事賠率排序
     */
    //移動進OddsSortUtil
    /*private fun MutableList<RecommendGameEntity>.recommendSortOddsMap() {
        this.forEach { RecommendGameEntity ->
            RecommendGameEntity.oddBeans.forEach { OddBeans ->
                OddBeans.oddList.sortBy { it?.marketSort }
            }
        }
    }*/

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
         this.oddsMap?.entries?.retainAll { oddMap -> oddMap.key == playCateCode?.split(",")?.get(0) ?: "HDP" }
    }

}
