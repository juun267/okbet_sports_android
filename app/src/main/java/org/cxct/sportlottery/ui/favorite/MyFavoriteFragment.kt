package org.cxct.sportlottery.ui.favorite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_my_favorite.*
import kotlinx.android.synthetic.main.fragment_my_favorite.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueListener
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.PlayCateMenuFilterUtils
import org.cxct.sportlottery.util.QuickListManager
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.SpaceItemDecoration


class MyFavoriteFragment : BaseSocketFragment<MyFavoriteViewModel>(MyFavoriteViewModel::class) {

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {
                viewModel.switchGameType(it)
            }
        }
    }

    private val playCategoryAdapter by lazy {
        PlayCategoryAdapter().apply {
            playCategoryListener = PlayCategoryListener(
                onClickSetItemListener = {
                    viewModel.switchPlay(it)
                    leagueAdapter.data.updateOddsSort()
                    //leagueAdapter.notifyDataSetChanged()
                    updateAllGameList()
                },
                onClickNotSelectableListener = {
                    viewModel.switchPlay(it)
                    upDateSelectPlay(it)
                    leagueAdapter.data.updateOddsSort()
                    //leagueAdapter.notifyDataSetChanged()
                    updateAllGameList()
                },
                onSelectPlayCateListener = { play, playCate ->
                    viewModel.switchPlayCategory(play, playCate.code)
                    upDateSelectPlay(play)
                    leagueAdapter.data.updateOddsSort()
                    //leagueAdapter.notifyDataSetChanged()
                    updateAllGameList()
                }
            )
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

    private val leagueAdapter by lazy {
        LeagueAdapter(MatchType.MY_EVENT, getPlaySelectedCodeSelectionType(), getPlaySelectedCode()).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            leagueListener = LeagueListener ({
                subscribeChannelHall(it)
            }, {})

            leagueOddListener = LeagueOddListener(
                { matchId, matchInfoList, gameMatchType ->
                    if (gameMatchType == MatchType.IN_PLAY) {
                        matchId?.let {
                            navOddsDetailLive(matchId, gameMatchType)
                        }
                    } else {
                        matchId?.let {
                            navOddsDetail(matchId, matchInfoList)
                        }
                    }
                },
                { matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap ->
                    addOddsDialog(matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap)
                },
                { matchOdd, quickPlayCate ->
                    viewModel.getQuickList(matchOdd.matchInfo?.id)
                },
                {
                    viewModel.clearQuickPlayCateSelected()
                },
                { matchId ->
                    viewModel.pinFavorite(FavoriteType.MATCH, matchId)
                    loading()
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_favorite, container, false).apply {
            setupToolbar(this)
            setupGameTypeList(this)
            setupPlayCategory(this)
            setupLeagueOddList(this)
        }
    }

    private fun setupToolbar(view: View) {
        (activity as AppCompatActivity).setSupportActionBar(view.favorite_toolbar)

        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupGameTypeList(view: View) {
        view.favorite_game_type_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            this.adapter = gameTypeAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_sport_type
                )
            )
        }
    }

    private fun setupPlayCategory(view: View) {
        view.favorite_play_category.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

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
        view.favorite_game_list.apply {
            adapter = leagueAdapter
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_sport_type
                )
            )
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        initSocketObserver()
    }

    private fun initSocketObserver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            it?.let { matchStatusChangeEvent ->
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (SocketUpdateUtil.updateMatchStatus(
                            gameTypeAdapter.dataSport.find { gameType -> gameType.isSelected }?.code,
                            leagueOdd.matchOdds.toMutableList(),
                            matchStatusChangeEvent,
                            context
                        ) &&
                        leagueOdd.unfold == FoldState.UNFOLD.code
                    ) {
                        if (leagueOdd.matchOdds.isNullOrEmpty()) {
                            leagueAdapter.data.remove(leagueOdd)
                        }

                        //leagueAdapter.notifyItemChanged(index)
                        updateGameList(index, leagueOdd)
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

                        //leagueAdapter.notifyItemChanged(index)
                        updateGameList(index, leagueOdd)
                    }
                }
            }
        }

        receiver.oddsChange.observe(this.viewLifecycleOwner) {
            it?.let { oddsChangeEvent ->
                SocketUpdateUtil.updateMatchOdds(oddsChangeEvent)
                oddsChangeEvent.updateOddsSelectedState()
                oddsChangeEvent.filterMenuPlayCate()

                val playSelected = playCategoryAdapter.data.find { play -> play.isSelected }
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
                                context,
                                matchOdd.apply {
                                    this.oddsMap?.filter { odds -> playSelected?.code == MenuCode.MAIN.code || odds.key == playSelected?.playCateList?.firstOrNull()?.code }
                                },
                                oddsChangeEvent
                            )
                        } &&
                        leagueOdd.unfold == FoldState.UNFOLD.code
                    ) {
                        //leagueAdapter.notifyItemChanged(index)
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
                        //leagueAdapter.notifyItemChanged(index)
                        updateGameList(index, leagueOdd)
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
                        //leagueAdapter.notifyItemChanged(index)
                        updateGameList(index, leagueOdd)
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

        receiver.leagueChange.observe(this.viewLifecycleOwner) { event ->
            event?.let {
                viewModel.getSportQuery(getLastPick = true) //而收到事件之后, 重新调用/api/front/sport/query用以加载上方球类选单

                val nowGameType = gameTypeAdapter.dataSport.find { gameType -> gameType.isSelected }?.code
                if (nowGameType == it.gameType) //收到的gameType与用户当前页面所选球种相同, 则需额外调用/myFavorite/match/query
                    viewModel.getFavoriteMatch()
                loading()
            }
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

    private fun updateOddsSelectedState() {
        leagueAdapter.data.forEach {
            it.matchOdds.forEach { matchOdd ->
                matchOdd.oddsMap?.forEach {  oddMap ->
                    oddMap.value?.forEach { odd ->
                        odd?.isSelected = viewModel.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                    }
                }
            }
        }
    }

    /**
     * 若當前選擇PlayCate可刷新, 則進行篩選, 只保留選擇的玩法
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

    override fun onStart() {
        super.onStart()
        viewModel.getSportQuery(getLastPick = true)
        viewModel.getSportMenuFilter()
//        loading()
    }

    private fun initObserver() {
        viewModel.userInfo.observe(this.viewLifecycleOwner) {
            leagueAdapter.discount = it?.discount ?: 1.0F
        }

        viewModel.myFavoriteLoading.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { show ->
                if (show) loading() else hideLoading()
            }
        }

        viewModel.sportQueryData.observe(this.viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { sportQueryData ->

                updateGameTypeList(sportQueryData.items?.map { item ->
                    Item(
                        code = item.code ?: "",
                        name = item.name ?: "",
                        num = item.num ?: 0,
                        play = null,
                        sortNum = item.sortNum ?: 0
                    ).apply {
                        this.isSelected = item.isSelected
                    }
                })

                updatePlayCategory(sportQueryData.items?.find { item ->
                    item.isSelected
                }?.play)
            }
        }

        viewModel.favorMatchOddList.observe(this.viewLifecycleOwner) { leagueOddList ->
            hideLoading()
            leagueOddList.filterMenuPlayCate()

            //favorite_game_list.adapter = leagueAdapter
            leagueAdapter.data = leagueOddList.toMutableList()
            leagueAdapter.playSelectedCodeSelectionType = getPlaySelectedCodeSelectionType()
            try {
                leagueAdapter.data.forEach { leagueOdd ->
                    subscribeChannelHall(leagueOdd)
                }
                leagueAdapter.limitRefresh()
                //leagueAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

                //leagueAdapter.notifyDataSetChanged()
                updateAllGameList()
            }
        }

        viewModel.favorMatchList.observe(this.viewLifecycleOwner) { favorMatchList ->
            if (favorMatchList.isNullOrEmpty()) {
                favorite_toolbar.visibility = View.VISIBLE
                fl_no_game.visibility = View.VISIBLE
            } else {
                favorite_toolbar.visibility = View.GONE
                fl_no_game.visibility = View.GONE
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                leagueAdapter.oddsType = oddsType
            }
        }
    }

    private fun updateGameTypeList(items: List<Item>?) {
        gameTypeAdapter.dataSport = items ?: listOf()

        favorite_game_type.text = when (items?.find {
            it.isSelected
        }?.code) {
            GameType.FT.key -> getString(GameType.FT.string)
            GameType.BK.key -> getString(GameType.BK.string)
            GameType.TN.key -> getString(GameType.TN.string)
            GameType.VB.key -> getString(GameType.VB.string)
            GameType.BM.key -> getString(GameType.BM.string)
            GameType.TT.key -> getString(GameType.TT.string)
            GameType.BX.key -> getString(GameType.BX.string)
            GameType.CB.key -> getString(GameType.CB.string)
            GameType.CK.key -> getString(GameType.CK.string)
            GameType.BB.key -> getString(GameType.BB.string)
            GameType.RB.key -> getString(GameType.RB.string)
            GameType.AFT.key -> getString(GameType.AFT.string)
            GameType.IH.key -> getString(GameType.IH.string)
            GameType.MR.key -> getString(GameType.MR.string)
            GameType.GF.key -> getString(GameType.GF.string)
            else -> ""
        }

        Glide.with(this).load(
            when (items?.find {
                it.isSelected
            }?.code) {
                GameType.FT.key -> R.drawable.soccer108
                GameType.BK.key -> R.drawable.basketball108
                GameType.TN.key -> R.drawable.tennis108
                GameType.VB.key -> R.drawable.volleyball108
                GameType.BM.key -> R.drawable.badminton_100
                GameType.TT.key -> R.drawable.pingpong_100
                GameType.BX.key -> R.drawable.boxing_100
                GameType.CB.key -> R.drawable.snooker_100
                GameType.CK.key -> R.drawable.cricket_100
                GameType.BB.key -> R.drawable.baseball_100
                GameType.RB.key -> R.drawable.rugby_100
                GameType.AFT.key -> R.drawable.amfootball_100
                GameType.IH.key -> R.drawable.icehockey_100
                GameType.MR.key -> R.drawable.rancing_100
                GameType.GF.key -> R.drawable.golf_108
                else -> null
            }
        ).into(favorite_bg_layer2)
    }

    private fun updatePlayCategory(plays: List<Play>?) {
        playCategoryAdapter.data = plays ?: listOf()
    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        if (gameType == null || matchInfo == null) {
            return
        }

        val fastBetDataBean = FastBetDataBean(
            matchType = MatchType.MY_EVENT,
            gameType = gameType,
            playCateCode = playCateCode,
            playCateName = playCateName,
            matchInfo = matchInfo,
            matchOdd = null,
            odd = odd,
            subscribeChannelType = ChannelType.HALL,
            betPlayCateNameMap = betPlayCateNameMap,
        )
        (activity as GameActivity).showFastBetFragment(fastBetDataBean)

//        viewModel.updateMatchBetList(
//            MatchType.MY_EVENT,
//            gameType,
//            playCateCode,
//            playCateName,
//            matchInfo,
//            odd,
//            ChannelType.HALL,
//            betPlayCateNameMap
//        )//TODO 訂閱HALL需傳入CateMenuCode
    }

    private fun subscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfold == FoldState.UNFOLD.code) {
                true -> {
                    subscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )
                }
                false -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )
                }
            }
        }
    }

    /**
     * 解除訂閱當前所選擇的PlayCate(MAIN, MATCH, 1ST ...)
     */
    private fun unSubscribePlayCateChannel() {
        leagueAdapter.data.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                unSubscribeChannelHall(
                    leagueOdd.gameType?.key,
                    matchOdd.matchInfo?.id
                )
            }
        }
    }

    /**
     * 取得當前所選擇的PlayCate(MAIN, MATCH, 1ST ...)
     */
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

    /**
     * 篩選玩法
     * 更新翻譯、排序
     * */

    private fun MutableList<LeagueOdd>.updateOddsSort() {
        val nowGameType = GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key
        val playCateMenuCode =
            if (getPlaySelectedCodeSelectionType() == SelectionType.SELECTABLE.code) getPlayCateMenuCode() else getPlaySelectedCode()
        val oddsSortFilter = if (getPlaySelectedCodeSelectionType() == SelectionType.SELECTABLE.code) getPlayCateMenuCode() else PlayCateMenuFilterUtils.filterOddsSort(nowGameType, playCateMenuCode)
        val playCateNameMapFilter = if (getPlaySelectedCodeSelectionType() == SelectionType.SELECTABLE.code) PlayCateMenuFilterUtils.filterSelectablePlayCateNameMap(nowGameType,getPlaySelectedCode(), playCateMenuCode) else PlayCateMenuFilterUtils.filterPlayCateNameMap(nowGameType, playCateMenuCode)

        this.forEach { LeagueOdd ->
            LeagueOdd.matchOdds.forEach { MatchOdd ->
                MatchOdd.oddsSort = oddsSortFilter
                MatchOdd.playCateNameMap = playCateNameMapFilter
            }
        }
    }

    /**
     * 可以下拉的PlayCate, 進行oddsMap的篩選, 只留下選擇的玩法
     */
    private fun List<LeagueOdd>.filterMenuPlayCate() {
        val playSelected = playCategoryAdapter.data.find { it.isSelected }
        when (playSelected?.selectionType) {
            SelectionType.SELECTABLE.code -> {
                this.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.oddsMap?.entries?.retainAll { oddMap -> oddMap.key == getPlayCateMenuCode() }
                    }
                }
            }
        }
    }

    private fun navOddsDetail(matchId: String, matchInfoList: List<MatchInfo>) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            val action =
                MyFavoriteFragmentDirections.actionMyFavoriteFragmentToOddsDetailFragment(
                    MatchType.MY_EVENT,
                    gameType,
                    matchId,
                    matchInfoList.toTypedArray()
                )

            findNavController().navigate(action)
        }
    }

    private fun navOddsDetailLive(matchId: String, gameMatchType: MatchType) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            val action =
                MyFavoriteFragmentDirections.actionMyFavoriteFragmentToOddsDetailLiveFragment(
                    gameMatchType,
                    gameType,
                    matchId
                )

            findNavController().navigate(action)
        }
    }

    private fun navStatistics(matchId: String?) {
        StatisticsDialog.newInstance(matchId).show(childFragmentManager, StatisticsDialog::class.java.simpleName)
    }

    private fun updateGameList(index: Int, leagueOdd: LeagueOdd) {
        leagueAdapter.data[index] = leagueOdd
        if (favorite_game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !favorite_game_list.isComputingLayout) {
            leagueAdapter.updateLeague(index, leagueOdd)
        }
    }

    private fun updateAllGameList() {
        if (favorite_game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !favorite_game_list.isComputingLayout) {
            leagueAdapter.data.forEachIndexed { index, leagueOdd ->  leagueAdapter.updateLeague(index, leagueOdd) }
        }
    }
}