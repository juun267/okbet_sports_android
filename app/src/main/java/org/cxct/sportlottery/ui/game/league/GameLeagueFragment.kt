package org.cxct.sportlottery.ui.game.league

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_game_league.*
import kotlinx.android.synthetic.main.fragment_game_league.view.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.OddsListData
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueListener
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.*

/**
 * @app_destination 聯賽列表(今日、早盤、串關點擊某項聯賽)
 */
class GameLeagueFragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class), Animation.AnimationListener {

    private val args: GameLeagueFragmentArgs by navArgs()

    private var isReloadPlayCate: Boolean? = null //是否重新加載玩法篩選Layout

    private val playCategoryAdapter by lazy {

        PlayCategoryAdapter().apply {
            playCategoryListener = PlayCategoryListener(onClickSetItemListener = {
                viewModel.switchPlay(
                    args.matchType,
                    args.leagueId.toList(),
                    args.matchId.toList(),
                    it
                )
                leagueAdapter.data.updateOddsSort()
                leagueAdapter.updateLeagueByPlayCate()
            },
                onClickNotSelectableListener = {
                    viewModel.switchPlay(
                        args.matchType,
                        args.leagueId.toList(),
                        args.matchId.toList(),
                        it
                    )
                    upDateSelectPlay(it)
                    leagueAdapter.data.updateOddsSort()
                    leagueAdapter.updateLeagueByPlayCate()
                },
                onSelectPlayCateListener = { play, playCate ->
                    viewModel.switchPlayCategory(
                        args.matchType,
                        args.leagueId.toList(),
                        args.matchId.toList(),
                        play,
                        playCate.code
                    )
                    upDateSelectPlay(play)
                    leagueAdapter.data.updateOddsSort()
                    leagueAdapter.updateLeagueByPlayCate()
                })
        }
    }

    private var mSelectedMatchInfo: MatchInfo? = null
    private val leagueAdapter by lazy {
        LeagueAdapter(args.matchType, getPlaySelectedCodeSelectionType(), getPlaySelectedCode()).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            leagueListener = LeagueListener({
                subscribeChannelHall(it)
            }, {
                viewModel.refreshGame(
                    args.matchType,
                    listOf(it.league.id),
                    listOf()
                )
            })

            leagueOddListener = LeagueOddListener(
                { matchId, matchInfoList, gameMatchType, liveVideo ->
                    when (gameMatchType) {
                        MatchType.IN_PLAY -> {
                            matchId?.let {
                                navOddsDetailLive(it, gameMatchType)
                            }
                        }
                        else -> {
                            matchId?.let {
                                navOddsDetail(it, matchInfoList)
                            }
                        }
                    }
                },
                { matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap ->
                    mSelectedMatchInfo = matchInfo
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
                    hideKeyboard()
                },
                { matchOdd, quickPlayCate ->
                    matchOdd.matchInfo?.let {
                        viewModel.getQuickList(it.id)
                    }
                },
                {
                    viewModel.clearQuickPlayCateSelected()
                },
                { matchId ->
                    matchId?.let {
                        viewModel.pinFavorite(FavoriteType.MATCH, it)
                    }
                },
                { matchId ->
                    navStatistics(matchId)
                },
                {}
            )
        }
    }

    init {
        afterAnimateListener = AfterAnimateListener {
            try {
                initObserve()
                initSocketObserver()
                initBottomNavigation()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_league, container, false).apply {
            setupToolbar(this)
            setupPlayCategory(this)
            setupLeagueOddList(this)
        }
    }

    private fun setupToolbar(view: View) {
        view.game_toolbar_back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setupPlayCategory(view: View) {
        view.game_league_play_category.apply {
            if (this.layoutManager == null || isReloadPlayCate != false) {
                this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            if (this.adapter == null || isReloadPlayCate != false) {
                this.adapter = playCategoryAdapter
            }

            removeItemDecorations()
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_play_category
                )
            )
        }
    }

    @SuppressLint("LogNotTimber")
    private fun setupLeagueOddList(view: View) {
        view.game_league_odd_list.apply {
            this.layoutManager =
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = leagueAdapter
            addScrollWithItemVisibility(
                onScrolling = {
                    unSubscribeChannelHallAll()
                },
                onVisible = {
                    if (leagueAdapter.data.isNotEmpty()) {
                        it.forEach { p ->
                            Log.d(
                                "[subscribe]",
                                "訂閱 ${leagueAdapter.data[p.first].league.name} -> " +
                                        "${leagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.homeName} vs " +
                                        "${leagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.awayName}"
                            )
                            subscribeChannelHall(
                                leagueAdapter.data[p.first].gameType?.key,
                                leagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.id
                            )
                        }
                    }
                }
            )
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

    override fun onResume() {
        super.onResume()

        viewModel.getSportMenuFilter()
    }

    private fun initObserve() {
        viewModel.userInfo.observe(this.viewLifecycleOwner) {
            leagueAdapter.discount = it?.discount ?: 1.0F
        }

        viewModel.playList.observe(this.viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                playCategoryAdapter.data = it
                if (isReloadPlayCate != false) {
                    view?.let { notNullView ->
                        setupPlayCategory(notNullView)
                        isReloadPlayCate = false
                    }
                }
            }
        }

        viewModel.playCate.observe(this.viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                playCategoryAdapter.apply {
                    data.find { it.isSelected }?.playCateList?.forEach { playCate ->
                        playCate.isSelected = (playCate.code == it)
                    }
                    for (index in data.indices) {
                        notifyItemChanged(index)
                    }
                }
            }
        }

        viewModel.oddsListResult.observe(this.viewLifecycleOwner) {
            hideLoading()

            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success) {
                    val leagueOdds = oddsListResult.oddsListData?.leagueOdds ?: listOf()

                    updateToolbar(oddsListResult.oddsListData)

                    updateSportBackground(oddsListResult.oddsListData?.sport?.code)

                    game_league_odd_list.apply {
                        leagueAdapter.playSelectedCodeSelectionType = getPlaySelectedCodeSelectionType()
                        leagueAdapter.apply {
                            data = leagueOdds.onEach { leagueOdd ->
                                leagueOdd.gameType = args.gameType
                            }.toMutableList()
                        }
                    }

                    game_league_odd_list?.firstVisibleRange(leagueAdapter, activity?:requireActivity())
                }
            }
        }

        viewModel.oddsListIncrementResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { leagueListIncrementResult ->
                val leagueListIncrement = leagueListIncrementResult.oddsListResult

                leagueListIncrementResult.leagueIdList?.forEach { leagueId ->
                    //判斷此聯賽是否已經於畫面顯示
                    leagueAdapter.data.find { adapterLeagueOdd -> adapterLeagueOdd.league.id == leagueId }
                        ?.let { onScreenLeague ->

                            val targetIndex = leagueAdapter.data.indexOf(onScreenLeague)
                            val changedLeague =
                                leagueListIncrement?.oddsListData?.leagueOdds?.find { leagueOdd -> leagueOdd.league.id == onScreenLeague.league.id }

                            //若api response沒有該聯賽資料則不顯示該聯賽
                            changedLeague?.let { changedLeagueOdd ->
                                unSubscribeLeagueChannelHall(leagueAdapter.data[targetIndex])
                                val targetLeagueOdd = leagueAdapter.data[targetIndex]
                                leagueAdapter.data[targetIndex] = changedLeagueOdd.apply {
                                    this.unfold = targetLeagueOdd.unfold
                                    this.gameType = targetLeagueOdd.gameType
                                    this.searchMatchOdds = targetLeagueOdd.searchMatchOdds
                                }
                                subscribeChannelHall(leagueAdapter.data[targetIndex])
                            } ?: run {
                                leagueAdapter.data.removeAt(targetIndex)
                                leagueAdapter.notifyItemRemoved(targetIndex)
                            }
                        }
                }
            }
        }

        viewModel.leagueListSearchResult.observe(this.viewLifecycleOwner) {
            leagueAdapter.data = it.toMutableList()
            leagueAdapter.playSelectedCodeSelectionType = getPlaySelectedCodeSelectionType()
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let { betInfoList ->

                leagueAdapter.betInfoList = betInfoList

                //leagueAdapter.notifyDataSetChanged()
                leagueAdapter.data.forEachIndexed { index, leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        if (matchOdd.matchInfo?.id == mSelectedMatchInfo?.id) {
                            leagueAdapter.updateLeague(index, leagueOdd)
                            mSelectedMatchInfo = null
                        }
                    }
                }
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                leagueAdapter.oddsType = oddsType
            }
        }

        viewModel.favorMatchList.observe(this.viewLifecycleOwner) {
            leagueAdapter.data.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.isFavorite = it.contains(matchOdd.matchInfo?.id)
                }
            }

            leagueAdapter.notifyDataSetChanged()
        }
    }

    private fun updateToolbar(oddsListData: OddsListData?) {
        when {
            (oddsListData?.leagueOdds?.size ?: 0 == 1) -> {
                game_toolbar_match_type.text = ""
                game_toolbar_sport_type.text = args.matchCategoryName
                    ?: (oddsListData?.leagueOdds?.firstOrNull()?.league?.name)
            }

            (oddsListData?.leagueOdds?.size ?: 0 > 1) -> {
                game_toolbar_match_type.text = ""
                game_toolbar_sport_type.text = args.matchCategoryName ?: when (args.matchType) {
                    MatchType.TODAY -> getString(R.string.home_tab_today)
                    MatchType.EARLY -> getString(R.string.home_tab_early)
                    MatchType.PARLAY -> getString(R.string.home_tab_parlay)
                    MatchType.AT_START -> getString(R.string.home_tab_at_start_2)
                    else -> ""
                }
            }
        }
    }

    private fun initSocketObserver() {
        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) { status ->
            status?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
//                    loading()
                    subscribeSportChannelHall()
                    viewModel.getLeagueOddsList(
                        args.matchType,
                        args.leagueId.toList(),
                        args.matchId.toList(),
                        isReloadPlayCate = (isReloadPlayCate != false)
                    )
                }
            }
        }
        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            it?.let { matchStatusChangeEvent ->
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (SocketUpdateUtil.updateMatchStatus(
                            args.gameType.key,
                            leagueOdd.matchOdds.toMutableList(),
                            matchStatusChangeEvent,
                            context
                        ) &&
                        leagueOdd.unfold == FoldState.UNFOLD.code
                    ) {
                        if (leagueOdd.matchOdds.isNullOrEmpty()) {
                            leagueAdapter.data.remove(leagueOdd)
                        }

                        leagueAdapter.notifyItemChanged(index)
                    }
                }
            }
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateMatchClock(
                                matchOdd,
                                matchClockEvent
                            )
                        } &&
                        leagueOdd.unfold == FoldState.UNFOLD.code) {

//                        leagueAdapter.notifyItemChanged(index)
                    }
                }
            }
        }

        receiver.oddsChange.observe(this.viewLifecycleOwner) {
            it?.let { oddsChangeEvent ->
                SocketUpdateUtil.updateMatchOdds(oddsChangeEvent)
                oddsChangeEvent.updateOddsSelectedState()
                oddsChangeEvent.filterMenuPlayCate()
                oddsChangeEvent.sortOddsMap()

                val leagueOdds = leagueAdapter.data

                leagueOdds.updateOddsSort() //篩選玩法

                //翻譯更新
                leagueOdds.forEach { LeagueOdd ->
                    LeagueOdd.matchOdds.forEach { MatchOdd ->
                        if (MatchOdd.matchInfo?.id == oddsChangeEvent.eventId) {
                            //馬克說betPlayCateNameMap還是由socket更新
                            oddsChangeEvent.betPlayCateNameMap?.let {
                                MatchOdd.betPlayCateNameMap?.putAll(oddsChangeEvent.betPlayCateNameMap!!)
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
                        updateGameList(index, leagueOdd)
                    }
                }
            }
        }

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->

                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                        } &&
                        leagueOdd.unfold == FoldState.UNFOLD.code
                    ) {
                        leagueAdapter.notifyItemChanged(index)
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->

                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateOddStatus(
                                matchOdd,
                                globalStopEvent
                            )
                        } &&
                        leagueOdd.unfold == FoldState.UNFOLD.code
                    ) {
                        leagueAdapter.notifyItemChanged(index)
                    }
                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeChannelHallAll()

                leagueAdapter.data.forEach { leagueOdd ->
                    if (leagueOdd.unfold == FoldState.UNFOLD.code)
                        subscribeChannelHall(leagueOdd)
                }
            }
        }

        receiver.leagueChange.observe(this.viewLifecycleOwner) {
            it?.let { leagueChangeEvent ->

                val hasLeagueIdList =
                    leagueAdapter.data.any { leagueOdd -> leagueOdd.league.id == leagueChangeEvent.leagueIdList?.firstOrNull() }

                if (args.gameType.key == leagueChangeEvent.gameType && hasLeagueIdList) { //聯賽數量固定
                    unSubscribeChannelHall(args.gameType.key, leagueChangeEvent.matchIdList?.firstOrNull())
                    subscribeChannelHall(args.gameType.key, leagueChangeEvent.matchIdList?.firstOrNull())
                }
            }
        }
    }

    private fun updateGameList(index: Int, leagueOdd: LeagueOdd) {
        leagueAdapter.data[index] = leagueOdd
        if (game_league_odd_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_league_odd_list.isComputingLayout) {
            leagueAdapter.updateLeague(index, leagueOdd)
        }
    }

    private fun OddsChangeEvent.updateOddsSelectedState(): OddsChangeEvent {
        this.odds?.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value?.onEach { odd ->
                    odd?.isSelected =
                        viewModel.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                }
            }
        }
        return this
    }

    /**
     * 篩選玩法
     * 更新翻譯、排序
     * */

    private fun MutableList<LeagueOdd>.updateOddsSort() {
        val nowGameType = args.gameType.key
        val playCateMenuCode =
            if (getPlaySelectedCodeSelectionType() == SelectionType.SELECTABLE.code) getPlayCateMenuCode() else getPlaySelectedCode()
        val oddsSortFilter = if (getPlaySelectedCodeSelectionType() == SelectionType.SELECTABLE.code) getPlayCateMenuCode() else PlayCateMenuFilterUtils.filterOddsSort(
            nowGameType,
            playCateMenuCode
        )
        val playCateNameMapFilter = if (getPlaySelectedCodeSelectionType() == SelectionType.SELECTABLE.code) PlayCateMenuFilterUtils.filterSelectablePlayCateNameMap(
            nowGameType,
            getPlaySelectedCode(),
            playCateMenuCode
        ) else PlayCateMenuFilterUtils.filterPlayCateNameMap(nowGameType, playCateMenuCode)

        this.forEach { LeagueOdd ->
            LeagueOdd.matchOdds.forEach { MatchOdd ->
                MatchOdd.oddsSort = oddsSortFilter
                MatchOdd.playCateNameMap = playCateNameMapFilter
            }
        }
    }

    /**
     * 只有有下拉篩選玩法的才需要過濾odds
     */
    private fun OddsChangeEvent.filterMenuPlayCate() {
        val playSelected = playCategoryAdapter.data.find { it.isSelected }

        when (playSelected?.selectionType) {
            SelectionType.SELECTABLE.code -> {
                val playCateMenuCode = playSelected.playCateList?.find { it.isSelected }?.code
                this.odds?.entries?.retainAll { oddMap -> oddMap.key == playCateMenuCode }
            }
        }
    }

    /**
     * 賠率排序
     */
    private fun OddsChangeEvent.sortOddsMap() {
        this.odds?.forEach { (_, value) ->
            if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                value?.sortBy {
                    it?.marketSort
                }
            }
        }
    }


    private fun updateSportBackground(sportCode: String?) {
        GameConfigManager.getTitleBarBackgroundInPublicPage(sportCode ,MultiLanguagesApplication.isNightMode)?.let { titleRes ->
            game_league_toolbar_bg.setImageResource(titleRes)
        }
    }

    //更新isLocked狀態
    private fun upDateSelectPlay(play: Play) {
        val platData = playCategoryAdapter.data.find { it == play }
        if (platData?.selectionType == SelectionType.SELECTABLE.code) {
            platData.isLocked = when {
                platData.isLocked == null || platData.isSelected -> false
                else -> true
            }
        }
    }

    private fun navOddsDetail(matchId: String, matchInfoList: List<MatchInfo>) {
        val action =
            GameLeagueFragmentDirections.actionGameLeagueFragmentToOddsDetailFragment(
                args.matchType,
                args.gameType,
                matchId,
                matchInfoList.toTypedArray()
            )

        findNavController().navigate(action)
    }

    private fun navOddsDetailLive(matchId: String, gameMatchType: MatchType) {
        val action = GameLeagueFragmentDirections.actionGameLeagueFragmentToOddsDetailLiveFragment(
            gameMatchType,
            args.gameType,
            matchId
        )

        findNavController().navigate(action)
    }

    private fun navStatistics(matchId: String?) {
        StatisticsDialog.newInstance(matchId, StatisticsDialog.StatisticsClickListener { clickMenu() })
            .show(childFragmentManager, StatisticsDialog::class.java.simpleName)
    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    ) {
        matchInfo?.let {
            val fastBetDataBean = FastBetDataBean(
                matchType = args.matchType,
                gameType = args.gameType,
                playCateCode = playCateCode,
                playCateName = playCateName ?: "",
                matchInfo = matchInfo,
                matchOdd = null,
                odd = odd,
                subscribeChannelType = ChannelType.HALL,
                betPlayCateNameMap = betPlayCateNameMap,
                getPlayCateMenuCode()
            )
            (activity as GameActivity).showFastBetFragment(fastBetDataBean)

//            viewModel.updateMatchBetList(
//                args.matchType,
//                args.gameType,
//                playCateCode,
//                playCateName,
//                matchInfo,
//                odd,
//                ChannelType.HALL,
//                betPlayCateNameMap,
//                getPlayCateMenuCode()
//            )
        }
    }

    private fun getPlaySelectedCode(): String? {
        return playCategoryAdapter.data.find { it.isSelected }?.code
    }

    /**
     * 取得當前篩選玩法是否可下拉
     * */
    private fun getPlaySelectedCodeSelectionType(): Int? {
        return playCategoryAdapter.data.find { it.isSelected }?.selectionType
    }

    private fun getPlayCateMenuCode(): String? {
        val playSelected = playCategoryAdapter.data.find { it.isSelected }

        return when (playSelected?.selectionType) {
            SelectionType.SELECTABLE.code -> {
                playSelected.playCateList?.find { it.isSelected }?.code
            }
            SelectionType.UN_SELECTABLE.code -> {
                playSelected.code
            }
            else -> null
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

//                    matchOdd.quickPlayCateList?.forEach {
//                        when (it.isSelected) {
//                            true -> {
//                                subscribeChannelHall(
//                                    leagueOdd.gameType?.key,
//                                    matchOdd.matchInfo?.id
//                                )
//                            }
//                            false -> {
//                                unSubscribeChannelHall(
//                                    leagueOdd.gameType?.key,
//                                    it.code,
//                                    matchOdd.matchInfo?.id
//                                )
//                            }
//                        }
//                    }
                }

                false -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )

//                    matchOdd.quickPlayCateList?.forEach {
//                        unSubscribeChannelHall(
//                            leagueOdd.gameType?.key,
//                            it.code,
//                            matchOdd.matchInfo?.id
//                        )
//                    }
                }
            }
        }
    }

    /**
     * 切換playCateMenu時, 先將原本訂閱的類別解除訂閱
     */
    private fun unSubscribeChannelSwitchPlayCate() {
        leagueAdapter.data.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                unSubscribeChannelHall(
                    leagueOdd.gameType?.key,
                    matchOdd.matchInfo?.id
                )
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

    override fun onStop() {
        super.onStop()

        unSubscribeChannelHallAll()
        unSubscribeChannelHallSport()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        isReloadPlayCate = null
        game_league_odd_list.adapter = null
    }
}