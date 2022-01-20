package org.cxct.sportlottery.ui.game.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.itemview_match_category_v4.*
import kotlinx.android.synthetic.main.view_game_tab_match_type_v4.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentHomeBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MenuCode
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.matchCategory.result.MatchCategoryResult
import org.cxct.sportlottery.network.matchCategory.result.MatchRecommendResult
import org.cxct.sportlottery.network.matchCategory.result.RECOMMEND_OUTRIGHT
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
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
import org.cxct.sportlottery.ui.statistics.KEY_MATCH_ID
import org.cxct.sportlottery.ui.statistics.StatisticsActivity
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.SocketUpdateUtil
import timber.log.Timber


/**
 * TODO simon test:
 * 1. 上下滑動 ToolBar 固定
 * 2. 賽事精選: icon 顯示 review
 * 3. 賽事推薦 - 冠軍樣式
 * 4. 賽事推薦 投注
 */
class HomeFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var homeBinding: FragmentHomeBinding

    private val mRvGameTable4Adapter = RvGameTable4Adapter()
    private var mSelectMatchType: MatchType = MatchType.IN_PLAY
    private var mInPlayResult: MatchPreloadResult? = null
    private var mAtStartResult: MatchPreloadResult? = null

    private val mHighlightGameTypeAdapter = GameTypeAdapter()
    private val mRvHighlightAdapter = RvHighlightAdapter()

    private val mRecommendAdapter = RvRecommendAdapter()

    private val mOnClickOddListener = object : OnClickOddListener {
        override fun onClickBet(
            matchOdd: MatchOdd,
            odd: Odd,
            playCateCode: String,
            playCateName: String?,
            betPlayCateNameMap: Map<String?, Map<String?, String?>?>?
        ) {
            addOddsDialog(matchOdd, odd, playCateCode, playCateName, betPlayCateNameMap)
        }
    }

    private val isShowThirdGame = sConfigData?.thirdOpen == FLAG_OPEN
    private var lotteryCount = 0
    private var liveCount = 0
    private var pokerCount = 0
    private var slotCount = 0
    private var fishingCount = 0
    private var isCreditAccount = false

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initDiscount()
            initTable()
            initRecommend()
            initHighlight()
            initEvent()
            initObserve()
            initSocketObserver()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()

        queryData()
    }

    override fun onStop() {
        super.onStop()

        unSubscribeChannelHallAll()
        mRvGameTable4Adapter.stopAllTimer()
        mRvHighlightAdapter.stopAllTimer()
    }

    private fun initDiscount() {
        val discount = viewModel.userInfo.value?.discount ?: 1.0F
        mRvGameTable4Adapter.discount = discount
        mRecommendAdapter.discount = discount
        mRvHighlightAdapter.discount = discount
    }

    private fun initTable() {
        judgeTableBar()
        rv_game_table.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        rv_game_table.adapter = mRvGameTable4Adapter
        mRvGameTable4Adapter.onClickOddListener = object : OnClickOddListener {
            override fun onClickBet(
                matchOdd: MatchOdd,
                odd: Odd,
                playCateCode: String,
                playCateName: String?,
                betPlayCateNameMap: Map<String?, Map<String?, String?>?>?
            ) {
                addOddsDialog(matchOdd, odd, playCateCode, playCateName, betPlayCateNameMap)
            }
        }
        mRvGameTable4Adapter.onClickMatchListener = object : OnSelectItemListener<MatchOdd> {
            override fun onClick(select: MatchOdd) {
                scroll_view.smoothScrollTo(0, 0)
                val code = select.matchInfo?.gameType
                val matchId = select.matchInfo?.id
                navOddsDetailFragment(code, matchId, mSelectMatchType)
            }
        }
        mRvGameTable4Adapter.onClickTotalMatchListener =
            object : OnSelectItemListener<GameEntity> {
                override fun onClick(select: GameEntity) {
                    scroll_view.smoothScrollTo(0, 0)
                    viewModel.navSpecialEntrance(
                        mSelectMatchType,
                        GameType.getGameType(select.code)
                    )
                }
            }
        mRvGameTable4Adapter.onClickSportListener =
            object : OnSelectItemListener<OtherMatch> {
                override fun onClick(select: OtherMatch) {
                    scroll_view.smoothScrollTo(0, 0)
                    viewModel.navSpecialEntrance(
                        MatchType.TODAY,
                        GameType.getGameType(select.code)
                    )
                }
            }

        mRvGameTable4Adapter.onClickFavoriteListener =
            object : OnClickFavoriteListener {
                override fun onClickFavorite(matchId: String?) {
                    viewModel.pinFavorite(FavoriteType.MATCH, matchId)
                }
            }

        mRvGameTable4Adapter.onClickStatisticsListener = object : OnClickStatisticsListener {
            override fun onClickStatistics(matchId: String?) {
                navStatisticsPage(matchId)
            }
        }

        rb_in_play.setOnClickListener {
            mSelectMatchType = MatchType.IN_PLAY
            refreshTable(mSelectMatchType, mInPlayResult)
        }

        rb_as_start.setOnClickListener {
            mSelectMatchType = MatchType.AT_START
            refreshTable(mSelectMatchType, mAtStartResult)
        }
    }

    private fun initRecommend() {
        updateRecommendVisibility(false)
        rv_recommend.adapter = mRecommendAdapter
        mRecommendAdapter.onClickOddListener = mOnClickOddListener
        mRecommendAdapter.onClickOutrightOddListener = object : OnClickOddListener {
            override fun onClickBet(
                matchOdd: MatchOdd,
                odd: Odd,
                playCateCode: String,
                playCateName: String?,
                betPlayCateNameMap: Map<String?, Map<String?, String?>?>?
            ) {
                GameType.getGameType(matchOdd.matchInfo?.gameType)?.let { gameType ->
                    //[Martin]把Dialog畫面提前開啟 體感上會比較順暢
//                    if(viewModel.betInfoList.value?.peekContent()?.size == 0){
//                        BetInfoCarDialog.launch()
//                    }
                    viewModel.updateMatchBetListForOutRight(
                        matchType = MatchType.OUTRIGHT,
                        gameType = gameType,
                        playCateCode = playCateCode,
                        matchOdd = org.cxct.sportlottery.network.outright.odds.MatchOdd(
                            matchInfo = matchOdd.matchInfo,
                            oddsMap = matchOdd.oddsMap,
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
        mRecommendAdapter.onClickMoreListener = object : OnClickMoreListener {
            override fun onClickMore(oddsKey: String, matchOdd: MatchOdd) {
                scroll_view.smoothScrollTo(0, 0)

                val action =
                    HomeFragmentDirections.actionHomeFragmentToGameOutrightMoreFragment(
                        oddsKey,
                        org.cxct.sportlottery.network.outright.odds.MatchOdd(
                            matchInfo = matchOdd.matchInfo,
                            oddsMap = matchOdd.oddsMap,
                            dynamicMarkets = matchOdd.dynamicMarkets ?: mapOf(),
                            oddsList = listOf(),
                            quickPlayCateList = matchOdd.quickPlayCateList,
                            betPlayCateNameMap= matchOdd.betPlayCateNameMap,
                            playCateNameMap = matchOdd.playCateNameMap
                        )
                    )
                findNavController().navigate(action)
            }
        }
        mRecommendAdapter.onClickMatchListener =
            object : OnSelectItemListener<RecommendGameEntity> {
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
            }
    }

    private fun initHighlight() {
        updateHighlightVisibility(false)
        rv_highlight_sport_type.adapter = mHighlightGameTypeAdapter
        mHighlightGameTypeAdapter.gameTypeListener = GameTypeListener { selectItem ->
            highlight_tv_game_name.text = selectItem.name

            GameConfigManager.getGameIcon(selectItem.code)?.let {
                highlight_iv_game_icon.setImageResource(it)
            }

            GameConfigManager.getTitleBarBackground(selectItem.code)?.let {
                highlight_titleBar.setBackgroundResource(it)
            }

            unsubscribeHighlightHallChannel() //先取消訂閱當前的精選賽事

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
        }
    }

    private fun refreshTable(selectMatchType: MatchType, result: MatchPreloadResult?) {
        //先清除之前訂閱項目
        unsubscribeTableHallChannel()
        subscribeTableHallChannel(selectMatchType)

        mRvGameTable4Adapter.setData(result?.matchPreloadData, selectMatchType)
    }

    //TableBar 判斷是否隱藏
    private fun judgeTableBar() {
        val inPlayCount = mInPlayResult?.matchPreloadData?.num ?: 0
        val atStartCount = mAtStartResult?.matchPreloadData?.num ?: 0

        rg_table_bar.visibility =
            if (inPlayCount == 0 && atStartCount == 0) View.GONE else View.VISIBLE
        rb_in_play.visibility = if (inPlayCount == 0) View.GONE else View.VISIBLE
        rb_as_start.visibility = if (atStartCount == 0) View.GONE else View.VISIBLE

    }

    private fun setDefaultRb() {
        val inPlayCount = mInPlayResult?.matchPreloadData?.num ?: 0
        if (inPlayCount != 0) {
            rb_in_play.performClick()
        } else {
            rb_as_start.performClick()
        }
    }

    private fun refreshHighlight(result: MatchCategoryResult?) {
        val sportCode = mHighlightGameTypeAdapter.dataSport.find { it.isSelected }?.code ?: ""
        mRvHighlightAdapter.setData(sportCode, result?.t?.odds)
    }

    private fun addOddsDialog(
        matchOdd: MatchOdd,
        odd: Odd,
        playCateCode: String,
        playCateName: String?,
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?
    ) {
        //[Martin]把Dialog畫面提前開啟 體感上會比較順暢
//        if(viewModel.betInfoList.value?.peekContent()?.size == 0){
//            BetInfoCarDialog.launch()
//        }
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
                    if (mSelectMatchType == MatchType.IN_PLAY) MenuCode.HOME_INPLAY_MOBILE.code else MenuCode.HOME_ATSTART_MOBILE.code
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

    //訂閱 滾球盤 or 即將開賽 賠率
    private fun subscribeTableHallChannel(selectMatchType: MatchType) {
        when (selectMatchType) {
            MatchType.IN_PLAY -> {
                mInPlayResult?.matchPreloadData?.datas?.forEach { data ->
                    data.matchOdds.forEach { match ->
                        subscribeChannelHall(
                            data.code,
                            MenuCode.HOME_INPLAY_MOBILE.code,
                            match.matchInfo?.id
                        )
                    }
                }
            }
            MatchType.AT_START -> {
                mAtStartResult?.matchPreloadData?.datas?.forEach { data ->
                    data.matchOdds.forEach { match ->
                        subscribeChannelHall(
                            data.code,
                            MenuCode.HOME_ATSTART_MOBILE.code,
                            match.matchInfo?.id
                        )
                    }
                }
            }
            else -> {

            }
        }

    }

    private fun unsubscribeTableHallChannel() {
        mInPlayResult?.matchPreloadData?.datas?.forEach { data ->
            data.matchs?.forEach { match ->
                unSubscribeChannelHall(
                    data.code,
                    MenuCode.HOME_INPLAY_MOBILE.code,
                    match.id
                )
            }
        }

        mAtStartResult?.matchPreloadData?.datas?.forEach { data ->
            data.matchs?.forEach { match ->
                unSubscribeChannelHall(
                    data.code,
                    MenuCode.HOME_ATSTART_MOBILE.code,
                    match.id
                )
            }
        }
    }

    //訂閱 推薦賽事 賠率
    private fun subscribeRecommendHallChannel() {
        mRecommendAdapter.getData().forEach { entity ->
            subscribeChannelHall(
                entity.code,
                MenuCode.RECOMMEND.code,
                entity.matchInfo?.id
            )
        }
    }

    private fun unsubscribeRecommendHallChannel() {
        mRecommendAdapter.getData().forEach { entity ->
            unSubscribeChannelHall(
                entity.code,
                MenuCode.RECOMMEND.code,
                entity.matchInfo?.id
            )
        }
    }

    //訂閱 精選賽事 賠率
    private fun subscribeHighlightHallChannel() {
        val code = mHighlightGameTypeAdapter.dataSport.find { it.isSelected }?.code ?: ""
        mRvHighlightAdapter.getData().forEach { matchOdd ->
            subscribeChannelHall(
                code,
                MenuCode.SPECIAL_MATCH_MOBILE.code,
                matchOdd.matchInfo?.id
            )
        }.apply {
            setDefaultRb()
        }
    }

    private fun unsubscribeHighlightHallChannel() {
        val code = mHighlightGameTypeAdapter.dataSport.find { it.isSelected }?.code ?: ""
        mRvHighlightAdapter.getData().forEach { matchOdd ->
            unSubscribeChannelHall(
                code,
                MenuCode.SPECIAL_MATCH_MOBILE.code,
                matchOdd.matchInfo?.id
            )
        }
    }

    private fun initObserve() {
        viewModel.userInfo.observe(viewLifecycleOwner) {
            it?.discount?.let { newDiscount ->
                mRvGameTable4Adapter.discount = newDiscount
                mRecommendAdapter.discount = newDiscount
                mRvHighlightAdapter.discount = newDiscount
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
                }else{
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
                            0 -> {
                                setupFirstGame(sportMenu)
                            }
                            1 -> {
                                setupSecondGame(sportMenu)
                            }
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
                            0 -> {
                                setupFirstGame(sportMenu)
                            }
                            1 -> {
                                setupSecondGame(sportMenu)
                            }
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
                    mInPlayResult = result
                    judgeTableBar()
                }
            }

            matchPreloadAtStart.observe(viewLifecycleOwner) {
                it.getContentIfNotHandled()?.let { result ->
                    mAtStartResult = result
                    judgeTableBar()
                }
            }
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            //updateOdds(it.peekContent())
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                mRvGameTable4Adapter.oddsType = oddsType
                mRecommendAdapter.oddsType = oddsType
                mRvHighlightAdapter.oddsType = oddsType
            }
        }

        viewModel.recommendMatchResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                unsubscribeRecommendHallChannel() //先取消訂閱當前的推薦賽事
                refreshRecommend(result)
                subscribeRecommendHallChannel()
            }
        }

        viewModel.highlightMenuResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                refreshHighlightMenu(result)
            }
        }

        viewModel.highlightMatchResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                refreshHighlight(result)
                subscribeHighlightHallChannel()
            }
        }

        viewModel.favorMatchList.observe(viewLifecycleOwner) { favorMatchList ->
            mRvGameTable4Adapter.getData().forEach {
                it.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.isFavorite = favorMatchList.contains(matchOdd.matchInfo?.id)
                }
            }

            mRvHighlightAdapter.getData().forEach {
                it.matchInfo?.isFavorite = favorMatchList.contains(it.matchInfo?.id)
            }

            mRvHighlightAdapter.notifyDataSetChanged()
        }

        viewModel.isCreditAccount.observe(viewLifecycleOwner) {
            isCreditAccount = it
            updateThirdGameCard()
        }

        viewModel.isLogin.observe(viewLifecycleOwner) {
            mRvGameTable4Adapter.isLogin = it
        }
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

    private fun setupHomeCard(homeGameCard: HomeGameCard, sportMenu: SportMenu) {
        homeGameCard.apply {
            setTitle(sportMenu.sportName)
            sportMenu.icon?.let { setIcon(sportMenu.icon) }
            setCount(sportMenu.gameCount)

            setOnClickListener {
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
    }

    private fun initSocketObserver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            it?.let { matchStatusChangeEvent ->
                //滾球盤、即將開賽盤
                val dataList = mRvGameTable4Adapter.getData()
                val hideGameList = mutableListOf<GameEntity>()
                var hideFirstPosition: Int? = null

                val statusValue =
                    matchStatusChangeEvent.matchStatusCO?.statusNameI18n?.get(
                        LanguageManager.getSelectLanguage(
                            context
                        ).key
                    )
                        ?: matchStatusChangeEvent.matchStatusCO?.statusName
                dataList.forEachIndexed { index, gameEntity ->
                    gameEntity.matchOdds.forEachIndexed { indexMatchOdd, updateMatchOdd ->
                        if (updateMatchOdd.matchInfo?.id == matchStatusChangeEvent.matchStatusCO?.matchId) {
                            updateMatchOdd.matchInfo?.homeTotalScore =
                                matchStatusChangeEvent.matchStatusCO?.homeTotalScore
                            updateMatchOdd.matchInfo?.awayTotalScore =
                                matchStatusChangeEvent.matchStatusCO?.awayTotalScore
                            updateMatchOdd.matchInfo?.homeScore =
                                matchStatusChangeEvent.matchStatusCO?.homeScore
                            updateMatchOdd.matchInfo?.awayScore =
                                matchStatusChangeEvent.matchStatusCO?.awayScore
                            updateMatchOdd.matchInfo?.homePoints =
                                matchStatusChangeEvent.matchStatusCO?.homePoints
                            updateMatchOdd.matchInfo?.awayPoints =
                                matchStatusChangeEvent.matchStatusCO?.awayPoints
                            updateMatchOdd.matchInfo?.statusName18n = statusValue

                            //賽事status為100, 隱藏該賽事
                            if (matchStatusChangeEvent.matchStatusCO?.status == 100) {
                                hideGameList.add(gameEntity)
                                if (hideFirstPosition == null) {
                                    hideFirstPosition = index
                                }
                            }

                            mRvGameTable4Adapter.notifySubItemChanged(index, indexMatchOdd)
                        }
                    }
                }

                //當前資料迴圈結束後才做移除
                hideGameList.forEach { removeGameEntity ->
                    dataList.remove(removeGameEntity)
                }
                hideFirstPosition?.let { startPosition ->
                    mRvGameTable4Adapter.apply { notifyItemRangeChanged(startPosition, itemCount) }
                }
            }
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                //滾球盤、即將開賽盤
                val dataList = mRvGameTable4Adapter.getData()
                dataList.forEachIndexed { index, gameEntity ->
                    gameEntity.matchOdds.forEachIndexed { indexMatchOdd, updateMatchOdd ->
                        if (updateMatchOdd.matchInfo?.id == matchClockEvent.matchClockCO?.matchId) {
                            updateMatchOdd.leagueTime =
                                when (matchClockEvent.matchClockCO?.gameType) {
                                    GameType.FT.key -> matchClockEvent.matchClockCO.matchTime
                                    GameType.BK.key -> matchClockEvent.matchClockCO.remainingTimeInPeriod
                                    else -> null
                                }

                            mRvGameTable4Adapter.notifySubItemChanged(index, indexMatchOdd)
                        }
                    }
                }
            }
        }

        receiver.oddsChange.observe(this.viewLifecycleOwner) {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.updateOddsSelectedState()
                when (oddsChangeEvent.getCateMenuCode()) {
                    MenuCode.HOME_INPLAY_MOBILE, MenuCode.HOME_ATSTART_MOBILE -> {
                        //滾球盤、即將開賽盤
                        val dataList = mRvGameTable4Adapter.getData()
                        dataList.forEachIndexed { index, gameEntity ->
                            //先找出要更新的 賽事
                            val updateMatchOdd = gameEntity.matchOdds.find { matchOdd ->
                                matchOdd.matchInfo?.id == it.eventId
                            }

                            updateMatchOdd?.let { updateMatchOddNonNull ->
                                val indexMatchOdd = gameEntity.matchOdds.indexOf(updateMatchOdd)

                                if (SocketUpdateUtil.updateMatchOdds(
                                        context,
                                        updateMatchOddNonNull,
                                        oddsChangeEvent
                                    )
                                ) {
                                    mRvGameTable4Adapter.notifySubItemChanged(index, indexMatchOdd)
                                }
                            }
                        }
                    }
                    MenuCode.RECOMMEND -> {
                        //推薦賽事
                        val recommendDataList = mRecommendAdapter.getData()
                        recommendDataList.forEachIndexed { index, entity ->
                            if (entity.matchInfo?.id != it.eventId)
                                return@forEachIndexed

                            entity.oddBeans.forEachIndexed { indexOddBean, oddBean ->
                                if (SocketUpdateUtil.updateMatchOdds(oddBean, oddsChangeEvent)) {
                                    mRecommendAdapter.notifySubItemChanged(index, indexOddBean)
                                }
                            }
                        }
                    }

                    MenuCode.SPECIAL_MATCH_MOBILE -> {
                        //精選賽事
                        val highlightDataList = mRvHighlightAdapter.getData()
                        highlightDataList.forEachIndexed { index, updateMatchOdd ->
                            if (SocketUpdateUtil.updateMatchOdds(
                                    context,
                                    updateMatchOdd,
                                    oddsChangeEvent
                                )
                            ) {
                                mRvHighlightAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        receiver.leagueChange.observe(this.viewLifecycleOwner) {
            viewModel.getMatchPreload()
            
            it?.getContentIfNotHandled()?.let { leagueChangeEvent ->
                leagueChangeEvent.leagueIdList?.let { leagueIdList ->

                    viewModel.getLeagueOddsList( //收到事件之后, 重新调用/api/front/sport/query用以加载上方球类选单
                        mSelectMatchType,
                        leagueIdList,
                        listOf(),
                        isIncrement = true
                    )
                }
            }

        }

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLock ->
                //滾球盤、即將開賽盤
                val dataList = mRvGameTable4Adapter.getData()
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
                        mRvGameTable4Adapter.notifySubItemChanged(index, indexMatchOdd)
                    }
                }

                //推薦賽事
                val recommendDataList = mRecommendAdapter.getData()
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
                                mRecommendAdapter.notifySubItemChanged(
                                    index,
                                    indexOddBean
                                )
                            }
                        }
                    }
                }

                //精選賽事
                val highlightDataList = mRvHighlightAdapter.getData()
                highlightDataList.forEachIndexed { index, updateMatchOdd ->
                    if (!updateMatchOdd.oddsMap.isNullOrEmpty()) {
                        updateMatchOdd.oddsMap.forEach { oldOddMap ->
                            oldOddMap.value?.forEach oldOddList@{ oldOdd ->
                                if (oldOdd == null) return@oldOddList

                                oldOdd.status = BetStatus.LOCKED.code

                                mRvHighlightAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                //滾球盤、即將開賽盤
                mRvGameTable4Adapter.getData().forEachIndexed { index, gameEntity ->
                    gameEntity.matchOdds.forEachIndexed { indexMatchOdd, matchOdd ->
                        if (SocketUpdateUtil.updateOddStatus(matchOdd, globalStopEvent)) {
                            mRvGameTable4Adapter.notifySubItemChanged(index, indexMatchOdd)
                        }
                    }
                }

                //推薦賽事
                mRecommendAdapter.getData().forEachIndexed { index, entity ->
                    entity.oddBeans.forEachIndexed { indexOddBean, oddBean ->
                        if (SocketUpdateUtil.updateOddStatus(oddBean, globalStopEvent)) {
                            mRecommendAdapter.notifySubItemChanged(index, indexOddBean)
                        }
                    }
                }

                //精選賽事
                mRvHighlightAdapter.getData().forEachIndexed { index, matchOdd ->
                    if (SocketUpdateUtil.updateOddStatus(matchOdd, globalStopEvent)) {
                        mRvHighlightAdapter.notifyItemChanged(index)
                    }
                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeChannelHallAll()

                subscribeTableHallChannel(mSelectMatchType)

                subscribeRecommendHallChannel()

                subscribeHighlightHallChannel()
            }
        }
    }

    private fun OddsChangeEvent.updateOddsSelectedState(): OddsChangeEvent {
        val betInfoListArray = viewModel.betInfoList.value?.peekContent()
        if (betInfoListArray?.size == 0) {
            return this
        }

        this.odds?.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value.onEach { odd ->
                    odd?.isSelected =
                        betInfoListArray?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                }
            }
        }

        Timber.e("Bee@Home2")

        return this
    }

    /**
     * @description channel format : /ws/notify/hall/{platformId}/{gameType}/{cateMenuCode}/{eventId}
     */
    private fun OddsChangeEvent.getCateMenuCode(): MenuCode? {
        return try {
            this.channel?.split("/")?.getOrNull(6)?.let { cateMenu ->
                MenuCode.valueOf(cateMenu)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun queryData() {
        //滾球盤、即將開賽盤
        viewModel.getMatchPreload()

        //推薦賽事
        viewModel.getRecommendMatch()

        //精選賽事
        viewModel.getHighlightMenu()
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
        card_lottery.visibility =
            if (isShowThirdGame && lotteryCount > 0 && !isCreditAccount) View.VISIBLE else View.GONE
        card_live.visibility = if (isShowThirdGame && liveCount > 0 && !isCreditAccount) View.VISIBLE else View.GONE
        card_poker.visibility = if (isShowThirdGame && pokerCount > 0 && !isCreditAccount) View.VISIBLE else View.GONE
        card_slot.visibility = if (isShowThirdGame && slotCount > 0 && !isCreditAccount) View.VISIBLE else View.GONE
        card_fishing.visibility =
            if (isShowThirdGame && fishingCount > 0 && !isCreditAccount) View.VISIBLE else View.GONE
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
        activity?.apply {
            startActivity(Intent(requireContext(), StatisticsActivity::class.java).apply {
                putExtra(KEY_MATCH_ID, matchId)
            })

            overridePendingTransition(
                R.anim.push_bottom_to_top_enter,
                R.anim.push_bottom_to_top_exit
            )
        }
    }

    private fun refreshRecommend(result: MatchRecommendResult) {
        mRecommendAdapter.setData(result)
        updateRecommendVisibility((result.rows?.size ?: 0) > 0)
    }

    private fun refreshHighlightMenu(result: MatchCategoryResult) {
        mHighlightGameTypeAdapter.dataSport = result.t?.menu?.map { menu ->
            Item(menu.code ?: "", menu.name ?: "", 0, null, menu.sortNum ?: 0)
        } ?: listOf()

        if (mHighlightGameTypeAdapter.dataSport.isNotEmpty()) {
            //default 選擇第一個
            mHighlightGameTypeAdapter.dataSport.firstOrNull()?.let {
                mHighlightGameTypeAdapter.gameTypeListener?.onClick(it)
            }
            updateHighlightVisibility(true)
        } else {
            updateHighlightVisibility(false)
        }
    }

    private fun updateRecommendVisibility(show: Boolean) {
        recommend_bar.isVisible = show
    }

    private fun updateHighlightVisibility(show: Boolean) {
        highlight_bar.isVisible = show
        highlight_titleBar.isVisible = show
    }

    //mapping 下注單裡面項目，更新 賠率按鈕 選擇狀態
    private fun updateOdds(result: List<BetInfoListData>) {
        val oddsIdArray = mutableListOf<String>()
        result.forEach {
            oddsIdArray.add(it.matchOdd.oddsId)
        }

        Timber.e("Bee@Home")

        //滾球盤、即將開賽盤
        mRvGameTable4Adapter.getData().forEachIndexed { index, gameEntity ->
            gameEntity.matchOdds.forEachIndexed { indexMatchOdd, matchOdd ->
                matchOdd.oddsMap.values.forEach { oddList ->
                    oddList?.forEach { odd ->
                        odd?.isSelected = oddsIdArray.contains(odd?.id ?: "")
                    }
                }
//                mRvGameTable4Adapter.notifySubItemChanged(index, indexMatchOdd)
            }
        }
        mRvGameTable4Adapter.notifyDataSetChanged()

        //推薦賽事
        mRecommendAdapter.getData().forEachIndexed { index, entity ->
            entity.oddBeans.forEachIndexed { indexOddBean, oddBean ->
                oddBean.oddList.forEach { odd ->
                    odd?.isSelected = oddsIdArray.contains(odd?.id ?: "")
                }
//                mRecommendAdapter.notifySubItemChanged(index, indexOddBean)
            }
        }
        mRecommendAdapter.notifyDataSetChanged()

        //精選賽事
        mRvHighlightAdapter.getData().forEach { matchOdd ->
            matchOdd.oddsMap.values.forEach { oddList ->
                oddList?.forEach { odd ->
                    odd?.isSelected = oddsIdArray.contains(odd?.id ?: "")
                }
            }
        }
        mRvHighlightAdapter.notifyDataSetChanged()
    }
}