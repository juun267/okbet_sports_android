package org.cxct.sportlottery.ui.game.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.itemview_match_category_v4.*
import kotlinx.android.synthetic.main.view_game_tab_match_type_v4.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentHomeBinding
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MenuCode
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.matchCategory.result.MatchCategoryResult
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.data.SpecialEntranceSource
import org.cxct.sportlottery.ui.game.hall.adapter.SportTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.SportTypeListener
import org.cxct.sportlottery.ui.game.home.gameTable4.*
import org.cxct.sportlottery.ui.game.home.highlight.RvHighlightAdapter
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.util.GameConfigManager


/**
 * TODO:
 * 1. 上下滑動 ToolBar 固定
 * 2. 賽事精選
 * 3. 賽事推薦
 * 4. 賠率刷新 滾球盤 viewPager 會回到第一頁
 */
class HomeFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var homeBinding: FragmentHomeBinding

    private val mRvGameTable4Adapter = RvGameTable4Adapter()
    private var mSelectMatchType: MatchType = MatchType.IN_PLAY
    private var mInPlayResult: MatchPreloadResult? = null
    private var mAtStartResult: MatchPreloadResult? = null

    private val mHighlightSportTypeAdapter = SportTypeAdapter()
    private val mRvHighlightAdapter = RvHighlightAdapter()
    private var mHighlightResult: MatchCategoryResult? = null

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

        //20210712 紀錄：沒設定 betInfoRepository._isParlayPage，BetListDialog 就不會初始化，下注不會有投注彈窗
        //賽事首頁 - 滾球盤、即將開賽盤、精選賽事、推薦賽事，都不屬於串關類型 切換
        viewModel.isParlayPage(false)

        initTable()
        initHighlight()
        initEvent()
        initObserve()
        observeSocketData()
    }

    override fun onStart() {
        super.onStart()

        queryData()
    }

    override fun onStop() {
        super.onStop()

        unsubscribeAllHallChannel()
    }

    private fun initTable() {
        rv_game_table.adapter = mRvGameTable4Adapter

        rb_in_play.setOnClickListener {
            mSelectMatchType = MatchType.IN_PLAY
            refreshTable(mSelectMatchType, mInPlayResult)
        }

        rb_as_start.setOnClickListener {
            mSelectMatchType = MatchType.AT_START
            refreshTable(mSelectMatchType, mAtStartResult)
        }

        if (mSelectMatchType == MatchType.IN_PLAY)
            rb_in_play.performClick()
        else
            rb_as_start.performClick()
    }

    private fun initHighlight() {
        rv_highlight_sport_type.adapter = mHighlightSportTypeAdapter
        mHighlightSportTypeAdapter.sportTypeListener = SportTypeListener { selectItem ->
            highlight_tv_game_name.text = selectItem.name
            highlight_iv_game_icon.setImageResource(GameConfigManager.getGameIcon(selectItem.code))
            highlight_titleBar.setBackgroundResource(
                GameConfigManager.getTitleBarBackground(
                    selectItem.code
                )
            )

            tv_play_type.text = when (SportType.getSportType(selectItem.code)) {
                SportType.FOOTBALL, SportType.BASKETBALL -> getText(R.string.ou_hdp_hdp_title)
                SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> getText(R.string.ou_hdp_1x2_title)
                else -> ""
            }

            unsubscribeHighlightHallChannel() //先取消訂閱當前的精選賽事

            mHighlightSportTypeAdapter.dataSport.forEach { item ->
                item.isSelected = item.code == selectItem.code
            }
            mHighlightSportTypeAdapter.notifyDataSetChanged()
            viewModel.getHighlightMatch(selectItem.code)
        }

        btn_display_all.setOnClickListener {
            mHighlightSportTypeAdapter.dataSport.find { it.isSelected }?.let { data ->
                val sportType = SportType.getSportType(data.code)
                viewModel.navSpecialEntrance(SpecialEntranceSource.HOME, MatchType.TODAY, sportType)
            }
        }

        rv_game_highlight.adapter = mRvHighlightAdapter
        mRvHighlightAdapter.onClickOddListener = object : OnClickOddListener {
            override fun onClickBet(
                matchOdd: MatchOdd,
                odd: Odd,
                playCateName: String,
                playName: String
            ) {
                addOddsDialog(matchOdd, odd, playCateName, playName)
            }
        }
        mRvHighlightAdapter.onClickMatchListener = object : OnSelectItemListener<MatchOdd> {
            override fun onClick(select: MatchOdd) {
                scroll_view.smoothScrollTo(0, 0)
                val code = select.matchInfo?.sportType?.code
                val matchId = select.matchInfo?.id
                navOddsDetailFragment(code, matchId, MatchType.TODAY)
            }
        }
    }

    private fun refreshTable(selectMatchType: MatchType, result: MatchPreloadResult?) {
        mRvGameTable4Adapter.matchType = selectMatchType
        mRvGameTable4Adapter.onClickOddListener = object : OnClickOddListener {
            override fun onClickBet(
                matchOdd: MatchOdd,
                odd: Odd,
                playCateName: String,
                playName: String
            ) {
                addOddsDialog(matchOdd, odd, playCateName, playName)
            }
        }
        mRvGameTable4Adapter.onClickMatchListener = object : OnSelectItemListener<MatchOdd> {
            override fun onClick(select: MatchOdd) {
                scroll_view.smoothScrollTo(0, 0)
                val code = select.matchInfo?.sportType?.code
                val matchId = select.matchInfo?.id
                navOddsDetailFragment(code, matchId, mSelectMatchType)
            }
        }
        mRvGameTable4Adapter.onClickTotalMatchListener =
            object : OnSelectItemListener<GameEntity> {
                override fun onClick(select: GameEntity) {
                    scroll_view.smoothScrollTo(0, 0)
                    viewModel.navSpecialEntrance(
                        SpecialEntranceSource.HOME,
                        selectMatchType,
                        SportType.getSportType(select.code)
                    )
                }
            }
        mRvGameTable4Adapter.setData(result?.matchPreloadData)
    }

    //TableBar 判斷是否隱藏
    private fun judgeTableBar() {
        val inPlayCount = mInPlayResult?.matchPreloadData?.num ?: 0
        val atStartCount = mAtStartResult?.matchPreloadData?.num ?: 0
        if (inPlayCount == 0) {
            mSelectMatchType = MatchType.AT_START
            rb_in_play.visibility = View.GONE
        } else {
            mSelectMatchType = MatchType.IN_PLAY
            rb_in_play.visibility = View.VISIBLE
        }

        rb_as_start.visibility = if (atStartCount == 0) View.GONE else View.VISIBLE

        rg_table_bar.visibility =
            if (rb_in_play.visibility == View.GONE && rb_as_start.visibility == View.GONE)
                View.GONE else View.VISIBLE
    }

    private fun refreshHighlight(result: MatchCategoryResult?) {
        val sportCode = mHighlightSportTypeAdapter.dataSport.find { it.isSelected }?.code ?: ""
        mRvHighlightAdapter.setData(sportCode, result?.t?.odds)
    }

    private fun addOddsDialog(
        matchOdd: MatchOdd,
        odd: Odd,
        playCateName: String,
        playName: String
    ) {
        matchOdd.matchInfo?.sportType?.let { sportType ->
            viewModel.updateMatchBetList(
                mSelectMatchType,
                sportType,
                playCateName,
                playName,
                matchOdd,
                odd
            )
        }
    }

    private fun initEvent() {
        card_football.setOnClickListener {
            viewModel.navSpecialEntrance(
                SpecialEntranceSource.HOME,
                MatchType.TODAY,
                SportType.FOOTBALL
            )
        }

        card_basketball.setOnClickListener {
            viewModel.navSpecialEntrance(
                SpecialEntranceSource.HOME,
                MatchType.TODAY,
                SportType.BASKETBALL
            )
        }

        card_tennis.setOnClickListener {
            viewModel.navSpecialEntrance(
                SpecialEntranceSource.HOME,
                MatchType.TODAY,
                SportType.TENNIS
            )
        }

        card_badminton.setOnClickListener {
            viewModel.navSpecialEntrance(
                SpecialEntranceSource.HOME,
                MatchType.TODAY,
                SportType.BADMINTON
            )
        }

        card_volleyball.setOnClickListener {
            viewModel.navSpecialEntrance(
                SpecialEntranceSource.HOME,
                MatchType.TODAY,
                SportType.VOLLEYBALL
            )
        }

        card_game_soon.setOnClickListener {
            viewModel.navSpecialEntrance(SpecialEntranceSource.HOME, MatchType.AT_START, null)
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
    private fun subscribeTableHallChannel() {
        if (mSelectMatchType == MatchType.IN_PLAY) {
            mInPlayResult?.matchPreloadData?.datas?.forEach { data ->
                data.matchs.forEach { match ->
                    service.subscribeHallChannel(
                        data.code,
                        MenuCode.HOME_INPLAY_MOBILE.code,
                        match.id
                    )
                }
            }
        } else if (mSelectMatchType == MatchType.AT_START) {
            mAtStartResult?.matchPreloadData?.datas?.forEach { data ->
                data.matchs.forEach { match ->
                    service.subscribeHallChannel(
                        data.code,
                        MenuCode.HOME_ATSTART_MOBILE.code,
                        match.id
                    )
                }
            }
        }
    }

    private fun unsubscribeTableHallChannel() {
        if (mSelectMatchType == MatchType.IN_PLAY) {
            mInPlayResult?.matchPreloadData?.datas?.forEach { data ->
                data.matchs.forEach { match ->
                    service.unsubscribeHallChannel(
                        data.code,
                        MenuCode.HOME_INPLAY_MOBILE.code,
                        match.id
                    )
                }
            }
        } else if (mSelectMatchType == MatchType.AT_START) {
            mAtStartResult?.matchPreloadData?.datas?.forEach { data ->
                data.matchs.forEach { match ->
                    service.unsubscribeHallChannel(
                        data.code,
                        MenuCode.HOME_ATSTART_MOBILE.code,
                        match.id
                    )
                }
            }
        }
    }

    //訂閱 精選賽事 賠率
    private fun subscribeHighlightHallChannel() {
        val code = mHighlightSportTypeAdapter.dataSport.find { it.isSelected }?.code ?: ""
        mHighlightResult?.t?.odds?.forEach { oddData ->
            service.subscribeHallChannel(
                code,
                MenuCode.SPECIAL_MATCH_MOBILE.code,
                oddData.matchInfo?.id
            )
        }
    }

    private fun unsubscribeHighlightHallChannel() {
        val code = mHighlightSportTypeAdapter.dataSport.find { it.isSelected }?.code ?: ""
        mHighlightResult?.t?.odds?.forEach { oddData ->
            service.unsubscribeHallChannel(
                code,
                MenuCode.SPECIAL_MATCH_MOBILE.code,
                oddData.matchInfo?.id
            )
        }
    }

    private fun unsubscribeAllHallChannel() {
        //離開畫面時取消訂閱所有賽事
        service.unsubscribeAllHallChannel()
    }

    private fun initObserve() {
        //第三方遊戲清單
        viewModel.gameCateDataList.observe(viewLifecycleOwner, {
            updateInPlayUI(it)
        })

        viewModel.matchPreloadInPlay.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                judgeTableBar()
                if (mSelectMatchType == MatchType.IN_PLAY)
                    refreshTable(mSelectMatchType, result)

                //先清除之前訂閱項目
                unsubscribeTableHallChannel()

                //訂閱所有滾球賽事
                mInPlayResult = result
                subscribeTableHallChannel()
            }
        })

        viewModel.matchPreloadAtStart.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                judgeTableBar()
                if (mSelectMatchType == MatchType.AT_START)
                    refreshTable(mSelectMatchType, result)

                //先清除之前訂閱項目
                unsubscribeTableHallChannel()

                //訂閱所有滾球賽事
                mAtStartResult = result
                subscribeTableHallChannel()
            }
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, {
            updateBetList(it.peekContent())
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            it?.let { oddsType ->
                mRvGameTable4Adapter.oddsType = oddsType
            }
        })

        viewModel.highlightMenuResult.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                updateHighlight(result)
            }
        })

        viewModel.highlightMatchResult.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                refreshHighlight(result)
                mHighlightResult = result
                subscribeHighlightHallChannel()
            }
        })
    }

    private fun observeSocketData() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, {
            it?.let { matchStatusChangeEvent ->
                matchStatusChangeEvent.matchStatusCO?.let { matchStatusCO ->
                    matchStatusCO.matchId?.let { matchId ->

                        val dataList = mRvGameTable4Adapter.getData()

                        dataList.forEachIndexed { index, gameEntity ->
                            val updateMatchOdd = gameEntity.matchOdds.find { matchOdd ->
                                matchOdd.matchInfo?.id == matchId
                            }
                            val indexMatchOdd = gameEntity.matchOdds.indexOf(updateMatchOdd)

                            updateMatchOdd?.let {
                                updateMatchOdd.matchInfo?.homeScore = matchStatusCO.homeScore
                                updateMatchOdd.matchInfo?.awayScore = matchStatusCO.awayScore
                                updateMatchOdd.matchInfo?.statusName = matchStatusCO.statusName

                                mRvGameTable4Adapter.notifySubItemChanged(index, indexMatchOdd)
                            }
                        }
                    }
                }
            }
        })

        receiver.matchClock.observe(this.viewLifecycleOwner, {
            it?.let { matchClockEvent ->
                matchClockEvent.matchClockCO?.let { matchClockCO ->
                    matchClockCO.matchId?.let { matchId ->

                        val dataList = mRvGameTable4Adapter.getData()

                        dataList.forEachIndexed { index, gameEntity ->
                            val updateMatchOdd = gameEntity.matchOdds.find { matchOdd ->
                                matchOdd.matchInfo?.id == matchId
                            }
                            val indexMatchOdd = gameEntity.matchOdds.indexOf(updateMatchOdd)

                            updateMatchOdd?.let {
                                updateMatchOdd.leagueTime = when (matchClockCO.gameType) {
                                    SportType.FOOTBALL.code -> {
                                        matchClockCO.matchTime
                                    }
                                    SportType.BASKETBALL.code -> {
                                        matchClockCO.remainingTimeInPeriod
                                    }
                                    else -> null
                                }

                                mRvGameTable4Adapter.notifyItemChanged(index, indexMatchOdd)
                            }
                        }
                    }
                }
            }
        })

        receiver.oddsChange.observe(this.viewLifecycleOwner, {
            it?.let {
                updateOdds(it)
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->

                val dataList = mRvGameTable4Adapter.getData()

                dataList.forEachIndexed { index, gameEntity ->
                    gameEntity.matchOdds.forEachIndexed { indexMatchOdd, matchOdd ->
                        matchOdd.odds.values.forEach { odds ->
                            odds.forEach { odd ->
                                when (globalStopEvent.producerId) {
                                    null -> {
                                        odd?.status = BetStatus.DEACTIVATED.code
                                    }
                                    else -> {
                                        odd?.producerId?.let { producerId ->
                                            if (producerId == globalStopEvent.producerId) {
                                                odd.status = BetStatus.DEACTIVATED.code
                                            }
                                        }
                                    }
                                }

                                mRvGameTable4Adapter.notifySubItemChanged(index, indexMatchOdd)
                            }
                        }

                    }
                }
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            it?.let { _ ->
                unsubscribeAllHallChannel()

                subscribeTableHallChannel()

                subscribeHighlightHallChannel()
            }
        })
    }

    private fun queryData() {
        //滾球盤、即將開賽盤
        viewModel.getMatchPreload()

        //精選賽事
        viewModel.getHighlightMenu()
    }

    private fun updateInPlayUI(gameCateList: List<GameCateData>?) {
        val isShowThirdGame = sConfigData?.thirdOpen == FLAG_OPEN
        val lotteryCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.CGCP }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        val liveCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.LIVE }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        val pokerCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.QP }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        val slotCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.DZ }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        val fishingCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.BY }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0

        card_lottery.visibility =
            if (isShowThirdGame && lotteryCount > 0) View.VISIBLE else View.GONE
        card_live.visibility = if (isShowThirdGame && liveCount > 0) View.VISIBLE else View.GONE
        card_poker.visibility = if (isShowThirdGame && pokerCount > 0) View.VISIBLE else View.GONE
        card_slot.visibility = if (isShowThirdGame && slotCount > 0) View.VISIBLE else View.GONE
        card_fishing.visibility =
            if (isShowThirdGame && fishingCount > 0) View.VISIBLE else View.GONE
    }

    private fun navOddsDetailFragment(
        sportTypeCode: String?,
        matchId: String?,
        matchType: MatchType
    ) {
        val sportType = SportType.getSportType(sportTypeCode)
        if (sportType != null && matchId != null) {
            val action = if (matchType == MatchType.IN_PLAY) {
                HomeFragmentDirections
                    .actionHomeFragmentToOddsDetailLiveFragment(sportType, matchId)
            } else {
                HomeFragmentDirections
                    .actionHomeFragmentToOddsDetailFragment(
                        matchType,
                        sportType,
                        matchId,
                        arrayOf()
                    )
            }
            findNavController().navigate(action)
        }
    }

    private fun updateHighlight(result: MatchCategoryResult) {
        mHighlightSportTypeAdapter.dataSport = result.t?.menu?.map { menu ->
            Item(menu.code ?: "", menu.name ?: "", 0, null, menu.sortNum ?: 0)
        } ?: listOf()

        //default 選擇第一個
        mHighlightSportTypeAdapter.dataSport.firstOrNull()?.let {
            mHighlightSportTypeAdapter.sportTypeListener?.onClick(it)
        }
    }

    private fun updateBetList(result: List<BetInfoListData>) {
        //滾球盤、即將開賽盤
        mRvGameTable4Adapter.getData().forEachIndexed { index, gameEntity ->
            gameEntity.matchOdds.forEachIndexed { indexMatchOdd, matchOdd ->
                matchOdd.odds.values.forEach { oddList ->
                    oddList.forEach { odd ->
                        odd?.isSelected = result.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                    }
                }

                mRvGameTable4Adapter.notifySubItemChanged(index, indexMatchOdd)
            }
        }


        //精選賽事
        mRvHighlightAdapter.getData().forEach { matchOdd ->
            matchOdd.odds.values.forEach { oddList ->
                oddList.forEach { odd ->
                    odd?.isSelected = result.any { betInfoListData ->
                        betInfoListData.matchOdd.oddsId == odd?.id
                    }
                }
            }
        }

        mRvHighlightAdapter.notifyDataSetChanged()
    }

    private fun updateOdds(result: OddsChangeEvent) {
        //mapping 下注單裡面項目 & 賠率按鈕 選擇狀態
        result.odds?.forEach { map ->
            map.value.forEach { odd ->
                odd?.isSelected = viewModel.betInfoList.value?.peekContent()?.any {
                    it.matchOdd.oddsId == odd?.id
                }
            }
        }


        //滾球盤、即將開賽盤
        val dataList = mRvGameTable4Adapter.getData()
        val oddsType = mRvGameTable4Adapter.oddsType

        dataList.forEachIndexed { index, gameEntity ->
            //先找出要更新的 賽事
            val updateMatchOdd = gameEntity.matchOdds.find { matchOdd ->
                matchOdd.matchInfo?.id == result.eventId
            }
            val indexMatchOdd = gameEntity.matchOdds.indexOf(updateMatchOdd)

            //mapping 要更新的賠率
            if (updateMatchOdd?.odds.isNullOrEmpty()) {
                updateMatchOdd?.odds =
                    result.odds?.mapValues { it.value.toMutableList() }?.toMutableMap()
                        ?: mutableMapOf()

            } else {
                result.odds?.forEach { map ->
                    val key = map.key
                    val newOddList = map.value
                    val oldOddList = updateMatchOdd?.odds?.get(key)

                    oldOddList?.forEach { oldOdd ->
                        val newOdd = newOddList.find { newOdd -> oldOdd?.id == newOdd?.id }
                        newOdd?.oddState = when (oddsType) {
                            OddsType.EU -> judgeOddState(oldOdd?.odds, newOdd?.odds).state
                            OddsType.HK -> judgeOddState(oldOdd?.hkOdds, newOdd?.hkOdds).state
                        }

                        oldOdd?.odds = newOdd?.odds
                        oldOdd?.hkOdds = newOdd?.hkOdds
                        newOdd?.status?.let {
                            oldOdd?.status = it
                        }
                    }
                }
            }

            //20210713 紀錄：只刷新內層 viewPager 的 sub Item，才不會導致每次刷新，viewPager 都會跑到第一頁
            mRvGameTable4Adapter.notifySubItemChanged(index, indexMatchOdd)
        }
    }

    private fun judgeOddState(oldOdd: Double?, newOdd: Double?): OddState {
        return when {
            oldOdd ?: 0.0 > newOdd ?: 0.0 -> OddState.SMALLER
            oldOdd ?: 0.0 < newOdd ?: 0.0 -> OddState.LARGER
            else -> OddState.SAME
        }
    }

}