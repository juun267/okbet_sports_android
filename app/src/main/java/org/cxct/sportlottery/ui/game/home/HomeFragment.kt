package org.cxct.sportlottery.ui.game.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.view_game_tab_match_type_v4.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentHomeBinding
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.match.Match
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.matchCategory.result.MatchCategoryResult
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
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

    private val mSportTypeAdapter = SportTypeAdapter()
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
            viewModel.switchMatchTypeByHome(mSelectMatchType)
        }

        rb_soon.setOnClickListener {
            mSelectMatchType = MatchType.AT_START
            refreshTable(mSelectMatchType, mAtStartResult)
            viewModel.switchMatchTypeByHome(mSelectMatchType)
        }
    }

    private fun initHighlight() {
        rv_highlight_sport_type.adapter = mSportTypeAdapter
        mSportTypeAdapter.sportTypeListener = SportTypeListener { selectItem ->
            highlight_tv_game_name.text = selectItem.name
            highlight_iv_game_icon.setImageResource(GameConfigManager.getGameIcon(selectItem.code))
            highlight_titleBar.setBackgroundResource(GameConfigManager.getTitleBarBackground(selectItem.code))

            tv_play_type.text = when (SportType.getSportType(selectItem.code)) {
                SportType.FOOTBALL, SportType.BASKETBALL -> getText(R.string.ou_hdp_hdp_title)
                SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> getText(R.string.ou_hdp_1x2_title)
                else -> ""
            }

            unsubscribeHighlightHallChannel() //先取消訂閱當前的精選賽事

            mSportTypeAdapter.dataSport.forEach { item ->
                item.isSelected = item.code == selectItem.code
            }
            mSportTypeAdapter.notifyDataSetChanged()
            viewModel.getHighlightMatch(selectItem.code)
        }

        btn_display_all.setOnClickListener {
            mSportTypeAdapter.dataSport.find { it.isSelected }?.let{ data ->
                val sportType = SportType.getSportType(data.code)
                viewModel.navSpecialEntrance(SpecialEntranceSource.HOME, MatchType.TODAY, sportType)
            }
        }

        rv_game_highlight.adapter = mRvHighlightAdapter
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

        rb_soon.visibility = if (atStartCount == 0) View.GONE else View.VISIBLE

        rg_table_bar.visibility =
            if (rb_in_play.visibility == View.GONE && rb_soon.visibility == View.GONE)
                View.GONE else View.VISIBLE
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

    private fun subscribeHallChannel(code: String, matchId: String?) {
        service.subscribeHallChannel(code, CateMenuCode.HDP_AND_OU.code, matchId)
    }

    private fun unsubscribeHallChannel(code: String, matchId: String?) {
        service.unsubscribeHallChannel(code, CateMenuCode.HDP_AND_OU.code, matchId)
    }

    //訂閱 滾球盤 or 即將開賽 賠率
    private fun subscribeTableHallChannel() {
        val result = if (mSelectMatchType == MatchType.IN_PLAY) mInPlayResult else mAtStartResult
        result?.matchPreloadData?.datas?.forEach { data ->
            data.matchs.forEach { match ->
                subscribeHallChannel(data.code, match.id)
            }
        }
    }

    private fun unsubscribeTableHallChannel() {
        val result = if (mSelectMatchType == MatchType.IN_PLAY) mInPlayResult else mAtStartResult
        result?.matchPreloadData?.datas?.forEach { data ->
            data.matchs.forEach { match ->
                unsubscribeHallChannel(data.code, match.id)
            }
        }
    }

    //訂閱 精選賽事 賠率
    private fun subscribeHighlightHallChannel() {
        val code = mSportTypeAdapter.dataSport.find { it.isSelected }?.code?: ""
        mHighlightResult?.t?.odds?.forEach { oddData ->
            subscribeHallChannel(code, oddData.matchInfo?.id)
        }
    }

    private fun unsubscribeHighlightHallChannel() {
        val code = mSportTypeAdapter.dataSport.find { it.isSelected }?.code?: ""
        mHighlightResult?.t?.odds?.forEach { oddData ->
            unsubscribeHallChannel(code, oddData.matchInfo?.id)
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
            it.peekContent().let {
                val dataList = mRvGameTable4Adapter.getData()

                dataList.forEach { gameEntity ->
                    gameEntity.matchOdds.forEach { matchOdd ->
                        matchOdd.odds.values.forEach { oddList ->
                            oddList.forEach { odd ->
                                odd?.isSelected = it.any { betInfoListData ->
                                    betInfoListData.matchOdd.oddsId == odd?.id
                                }
                            }
                        }
                    }
                }

                mRvGameTable4Adapter.notifyDataSetChanged()
            }
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

                            updateMatchOdd?.let {
                                updateMatchOdd.matchInfo?.homeScore = matchStatusCO.homeScore
                                updateMatchOdd.matchInfo?.awayScore = matchStatusCO.awayScore
                                updateMatchOdd.matchInfo?.statusName = matchStatusCO.statusName

                                mRvGameTable4Adapter.notifyItemChanged(index)
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

                                mRvGameTable4Adapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        })

        receiver.oddsChange.observe(this.viewLifecycleOwner, {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->

                    @Suppress("NAME_SHADOWING")
                    val oddTypeSocketMap = oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                        oddTypeSocketMapEntry.value.toMutableList().onEach { odd ->
                            odd?.isSelected =
                                viewModel.betInfoList.value?.peekContent()?.any { betInfoListData ->
                                    betInfoListData.matchOdd.oddsId == odd?.id
                                }
                        }
                    }

                    val dataList = mRvGameTable4Adapter.getData()
                    val oddsType = mRvGameTable4Adapter.oddsType

                    dataList.forEachIndexed { index, gameEntity ->
                        val updateMatchOdd = gameEntity.matchOdds.find { matchOdd ->
                            matchOdd.matchInfo?.id == oddsChangeEvent.eventId
                        }

                        if (updateMatchOdd?.odds.isNullOrEmpty()) {
                            updateMatchOdd?.odds = oddTypeSocketMap.toMutableMap()

                        } else {
                            updateMatchOdd?.odds?.forEach { oddTypeMap ->

                                val oddsSocket = oddTypeSocketMap[oddTypeMap.key]
                                val odds = oddTypeMap.value

                                odds.forEach { odd ->
                                    odd?.let { oddNonNull ->
                                        val oddSocket = oddsSocket?.find { oddSocket ->
                                            oddSocket?.id == odd.id
                                        }

                                        oddSocket?.let { oddSocketNonNull ->
                                            when (oddsType) {
                                                OddsType.EU -> {
                                                    oddNonNull.odds?.let { oddValue ->
                                                        oddSocketNonNull.odds?.let { oddSocketValue ->
                                                            when {
                                                                oddValue > oddSocketValue -> {
                                                                    oddNonNull.oddState =
                                                                        OddState.SMALLER.state
                                                                }
                                                                oddValue < oddSocketValue -> {
                                                                    oddNonNull.oddState =
                                                                        OddState.LARGER.state
                                                                }
                                                                oddValue == oddSocketValue -> {
                                                                    oddNonNull.oddState =
                                                                        OddState.SAME.state
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                OddsType.HK -> {
                                                    oddNonNull.hkOdds?.let { oddValue ->
                                                        oddSocketNonNull.hkOdds?.let { oddSocketValue ->
                                                            when {
                                                                oddValue > oddSocketValue -> {
                                                                    oddNonNull.oddState =
                                                                        OddState.SMALLER.state
                                                                }
                                                                oddValue < oddSocketValue -> {
                                                                    oddNonNull.oddState =
                                                                        OddState.LARGER.state
                                                                }
                                                                oddValue == oddSocketValue -> {
                                                                    oddNonNull.oddState =
                                                                        OddState.SAME.state
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            oddNonNull.odds = oddSocketNonNull.odds
                                            oddNonNull.hkOdds = oddSocketNonNull.hkOdds

                                            oddNonNull.status = oddSocketNonNull.status

                                            mRvGameTable4Adapter.notifyItemChanged(index)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->

                val dataList = mRvGameTable4Adapter.getData()

                dataList.forEachIndexed { index, gameEntity ->
                    gameEntity.matchOdds.forEach { matchOdd ->
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

                                mRvGameTable4Adapter.notifyItemChanged(index)
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
        mSportTypeAdapter.dataSport = result.t?.menu?.map { menu ->
            Item(menu.code ?: "", menu.name ?: "", 0, null, menu.sortNum ?: 0)
        } ?: listOf()

        //default 選擇第一個
        mSportTypeAdapter.dataSport.firstOrNull()?.let {
            mSportTypeAdapter.sportTypeListener?.onClick(it)
        }
    }
}