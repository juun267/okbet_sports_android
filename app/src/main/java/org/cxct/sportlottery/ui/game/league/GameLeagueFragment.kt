package org.cxct.sportlottery.ui.game.league

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_game_league.*
import kotlinx.android.synthetic.main.fragment_game_league.view.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.OddsListData
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueListener
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryListener
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.SpaceItemDecoration


class GameLeagueFragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameLeagueFragmentArgs by navArgs()

    private val playCategoryAdapter by lazy {

        PlayCategoryAdapter().apply {
            playCategoryListener = PlayCategoryListener {
                if (it.selectionType == SelectionType.SELECTABLE.code) {
                    when {
                        //這個是沒有點選過的狀況 第一次進來 ：開啟選單
                        !it.isSelected && it.isLocked == null -> {
                            showPlayCateBottomSheet(it)
                        }
                        //當前被點選的狀態
                        it.isSelected -> {
                            showPlayCateBottomSheet(it)
                        }
                        //之前點選過然後離開又回來 要預設帶入
                        !it.isSelected && it.isLocked == false -> {
                            unSubscribeChannelSwitchPlayCate()
                            viewModel.switchPlay(
                                args.matchType,
                                args.leagueId.toList(),
                                args.matchId.toList(),
                                it
                            )
                            loading()
                        }
                    }
                } else {
                    unSubscribeChannelSwitchPlayCate()
                    viewModel.switchPlay(
                        args.matchType,
                        args.leagueId.toList(),
                        args.matchId.toList(),
                        it
                    )
                    upDateSelectPlay(it)
                    loading()
                }
            }
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter(args.matchType).apply {
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
                { matchId, matchInfoList ->
                    when (args.matchType) {
                        MatchType.IN_PLAY -> {
                            matchId?.let {
                                navOddsDetailLive(it)
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
                    addOddsDialog(matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap)
                    hideKeyboard()
                },
                { matchId ->
                    matchId?.let {
                        viewModel.getQuickList(it)
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
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
            this.adapter = playCategoryAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_play_category
                )
            )
        }
    }

    private fun setupLeagueOddList(view: View) {
        view.game_league_odd_list.apply {
            this.layoutManager =
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

            addItemDecoration(
                SpaceItemDecoration(context, R.dimen.item_spacing_league)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initObserve()
            initSocketObserver()
            initBottomNavigation()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()

        subscribeSportChannelHall(args.gameType.key)

        viewModel.getLeagueOddsList(
            args.matchType,
            args.leagueId.toList(),
            args.matchId.toList(),
            isReloadPlayCate = true
        )
        loading()
    }

    private fun initObserve() {
        viewModel.userInfo.observe(this.viewLifecycleOwner) {
            leagueAdapter.discount = it?.discount ?: 1.0F
        }

        viewModel.playList.observe(this.viewLifecycleOwner) {
            playCategoryAdapter.data = it
        }

        viewModel.playCate.observe(this.viewLifecycleOwner) {
            playCategoryAdapter.apply {
                data.find { it.isSelected }?.playCateList?.forEach { playCate ->
                    playCate.isSelected = (playCate.code == it)
                }
                notifyDataSetChanged()
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
                        adapter = leagueAdapter.apply {
                            data = leagueOdds.onEach { leagueOdd ->
                                leagueOdd.gameType = args.gameType
                            }.toMutableList()
                        }
                    }

                    leagueOdds.forEach { leagueOdd ->
                        subscribeChannelHall(leagueOdd)
                    }
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
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let {
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.oddsMap?.values?.forEach { oddList ->
                            oddList?.forEach { odd ->
                                odd?.isSelected = it.any { betInfoListData ->
                                    betInfoListData.matchOdd.oddsId == odd?.id
                                }
                            }
                        }

                        matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                            quickPlayCate.quickOdds?.forEach { map ->
                                map.value?.forEach { odd ->
                                    odd?.isSelected = it.any { betInfoListData ->
                                        betInfoListData.matchOdd.oddsId == odd?.id
                                    }
                                }
                            }
                        }
                    }
                }

                leagueAdapter.notifyDataSetChanged()
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
                game_toolbar_match_type.text =""
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

                        leagueAdapter.notifyItemChanged(index)
                    }
                }
            }
        }

        receiver.oddsChange.observe(this.viewLifecycleOwner) {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.updateOddsSelectedState()
                oddsChangeEvent.filterMenuPlayCate()
                oddsChangeEvent.sortOddsMap()

                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateMatchOdds(
                                context, matchOdd, oddsChangeEvent
                            )
                        } &&
                        leagueOdd.unfold == FoldState.UNFOLD.code
                    ) {
                        leagueAdapter.notifyItemChanged(index)
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
                    subscribeChannelHall(leagueOdd)
                }
            }
        }

        receiver.leagueChange.observe(this.viewLifecycleOwner) {
            it?.let { leagueChangeEvent ->

                val hasLeagueIdList =
                    leagueAdapter.data.any { leagueOdd -> leagueOdd.league.id == leagueChangeEvent.leagueIdList?.firstOrNull() }

                if (args.gameType.key == leagueChangeEvent.gameType && hasLeagueIdList) { //聯賽數量固定
                    unSubscribeChannelHall(args.gameType.key, getPlaySelectedCode(), leagueChangeEvent.matchIdList?.firstOrNull())
                    subscribeChannelHall(args.gameType.key, getPlaySelectedCode(), leagueChangeEvent.matchIdList?.firstOrNull())
                }
            }
        }
    }

    private fun OddsChangeEvent.updateOddsSelectedState(): OddsChangeEvent {
        this.odds?.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value.onEach { odd ->
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
            if (value?.size > 3 && value.first()?.marketSort != 0 && (value.first()?.odds != value.first()?.malayOdds)) {
                value.sortBy {
                    it?.marketSort
                }
            }
        }
    }


    private fun updateSportBackground(sportCode: String?) {
        Glide.with(requireContext()).load(
            when (sportCode) {
                GameType.FT.key -> R.drawable.soccer48
                GameType.BK.key -> R.drawable.basketball48
                GameType.TN.key -> R.drawable.tennis48
                GameType.VB.key -> R.drawable.volleyball48
                else -> null
            }
        ).into(game_league_toolbar_bg)
    }

    private fun showPlayCateBottomSheet(play: Play) {
        showBottomSheetDialog(
            play.name,
            play.playCateList?.map { playCate -> StatusSheetData(playCate.code, playCate.name) }
                ?: listOf(),
            StatusSheetData(
                (play.playCateList?.find { it.isSelected } ?: play.playCateList?.first())?.code,
                (play.playCateList?.find { it.isSelected } ?: play.playCateList?.first())?.name
            ),
            StatusSheetAdapter.ItemCheckedListener { _, playCate ->
                unSubscribeChannelSwitchPlayCate()
                viewModel.switchPlayCategory(
                    args.matchType,
                    args.leagueId.toList(),
                    args.matchId.toList(),
                    play,
                    playCate.code
                )
                upDateSelectPlay(play)
                (activity as BaseActivity<*>).bottomSheet.dismiss()
                loading()
            })
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

    private fun navOddsDetailLive(matchId: String) {
        val action = GameLeagueFragmentDirections.actionGameLeagueFragmentToOddsDetailLiveFragment(
            args.matchType,
            args.gameType,
            matchId
        )

        findNavController().navigate(action)
    }

    private fun navStatistics(matchId: String?) {
        StatisticsDialog.newInstance(matchId).show(childFragmentManager, StatisticsDialog::class.java.simpleName)
    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    ) {
        matchInfo?.let {
            viewModel.updateMatchBetList(
                args.matchType,
                args.gameType,
                playCateCode,
                playCateName,
                matchInfo,
                odd,
                ChannelType.HALL,
                betPlayCateNameMap,
                getPlayCateMenuCode()
            )
        }
    }

    private fun getPlaySelectedCode(): String? {
        return playCategoryAdapter.data.find { it.isSelected }?.code
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
                        getPlaySelectedCode(),
                        matchOdd.matchInfo?.id
                    )

                    if (matchOdd.matchInfo?.eps == 1) {
                        subscribeChannelHall(
                            leagueOdd.gameType?.key,
                            PlayCate.EPS.value,
                            matchOdd.matchInfo.id
                        )
                    }

                    matchOdd.quickPlayCateList?.forEach {
                        when (it.isSelected) {
                            true -> {
                                subscribeChannelHall(
                                    leagueOdd.gameType?.key,
                                    it.code,
                                    matchOdd.matchInfo?.id
                                )
                            }
                            false -> {
                                unSubscribeChannelHall(
                                    leagueOdd.gameType?.key,
                                    it.code,
                                    matchOdd.matchInfo?.id
                                )
                            }
                        }
                    }
                }

                false -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        getPlayCateMenuCode(),
                        matchOdd.matchInfo?.id
                    )

                    if (matchOdd.matchInfo?.eps == 1) {
                        unSubscribeChannelHall(
                            leagueOdd.gameType?.key,
                            PlayCate.EPS.value,
                            matchOdd.matchInfo.id
                        )
                    }

                    matchOdd.quickPlayCateList?.forEach {
                        unSubscribeChannelHall(
                            leagueOdd.gameType?.key,
                            it.code,
                            matchOdd.matchInfo?.id
                        )
                    }
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
                    getPlaySelectedCode(),
                    matchOdd.matchInfo?.id
                )

                if (matchOdd.matchInfo?.eps == 1) {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        PlayCate.EPS.value,
                        matchOdd.matchInfo.id
                    )
                }
            }
        }
    }

    private fun unSubscribeLeagueChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            unSubscribeChannelHall(
                leagueOdd.gameType?.key,
                getPlayCateMenuCode(),
                matchOdd.matchInfo?.id
            )

            if (matchOdd.matchInfo?.eps == 1) {
                unSubscribeChannelHall(
                    leagueOdd.gameType?.key,
                    PlayCate.EPS.value,
                    matchOdd.matchInfo.id
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()

        unSubscribeChannelHallAll()
        unSubscribeChannelHallSport()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        game_league_odd_list.adapter = null
    }
}