package org.cxct.sportlottery.ui.favorite

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
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
import kotlinx.android.synthetic.main.fragment_my_favorite.appbar_layout
import kotlinx.android.synthetic.main.fragment_my_favorite.view.*
import kotlinx.android.synthetic.main.include_my_favorite_empty.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueListener
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.*

/**
 * @app_destination 我的賽事
 */
@SuppressLint("LogNotTimber")
class MyFavoriteFragment : BaseBottomNavigationFragment<MyFavoriteViewModel>(MyFavoriteViewModel::class) {

    private var isReloadPlayCate: Boolean? = null //是否重新加載玩法篩選Layout

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {
                unSubscribeChannelHallAll()
                viewModel.switchGameType(it)
                isReloadPlayCate = true
                (favorite_game_type_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(favorite_game_type_list, RecyclerView.State(), dataSport.indexOfFirst { item -> TextUtils.equals(it.code,item.code) })
            }
        }
    }

    private val playCategoryAdapter by lazy {
        PlayCategoryAdapter().apply {
            playCategoryListener = PlayCategoryListener(
                onClickSetItemListener = {
                    unSubscribeChannelHallAll()
                    viewModel.switchPlay(it)
                },
                onClickNotSelectableListener = {
                    unSubscribeChannelHallAll()
                    viewModel.switchPlay(it)
                    upDateSelectPlay(it)
                },
                onSelectPlayCateListener = { play, playCate, hasItemSelect ->
                    if (!hasItemSelect) {
                        unSubscribeChannelHallAll()
                    }
                    viewModel.switchPlayCategory(play, playCate.code, hasItemSelect)
                    upDateSelectPlay(play)
                    if (hasItemSelect) {
                        leagueAdapter.data.updateOddsSort(
                            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key,
                            this
                        )
                        leagueAdapter.updateLeagueByPlayCate()
                    }
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

            leagueListener = LeagueListener {
                subscribeChannelHall(it)
            }

            leagueOddListener = LeagueOddListener(
                clickListenerPlayType = { matchId, _, gameMatchType, _ ->
                    navMatchDetailPage(matchId, gameMatchType)
                },
                clickListenerBet = { matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap ->
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
                },
                clickListenerQuickCateTab = { matchOdd, quickPlayCate ->
                    setQuickPlayCateSelected(matchOdd, quickPlayCate)
                },
                clickListenerQuickCateClose = {
                    clearQuickPlayCateSelected()
                },
                clickListenerFavorite = { matchId ->
                    viewModel.pinFavorite(FavoriteType.MATCH, matchId)
                },
                clickListenerStatistics = { matchId ->
                    navStatistics(matchId)
                },
                refreshListener = {},
                clickLiveIconListener = { matchId, _, gameMatchType, _ ->
                    if (viewModel.checkLoginStatus()) {
                        navMatchDetailPage(matchId, gameMatchType)
                    }
                },
                clickAnimationIconListener = { matchId, _, gameMatchType, _ ->
                    if (viewModel.checkLoginStatus()) {
                        navMatchDetailPage(matchId, gameMatchType)
                    }
                }
            )
        }
    }


    private fun navMatchDetailPage(matchId: String?, gameMatchType: MatchType) {
        matchId?.let {
            navOddsDetailLive(matchId, gameMatchType)
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
                ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            this.adapter = gameTypeAdapter

//            addItemDecoration(
//                SpaceItemDecoration(
//                    context,
//                    R.dimen.recyclerview_item_dec_spec_sport_type
//                )
//            )
        }
    }

    private fun setupPlayCategory(view: View) {
        view.favorite_play_category.apply {
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

    private fun setupLeagueOddList(view: View) {
        view.favorite_game_list.apply {
            adapter = leagueAdapter
            this.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_sport_type
                )
            )

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
            addScrollListenerForBottomNavBar {
                MultiLanguagesApplication.mInstance.setIsScrollDown(it)
            }
        }
        view.appbar_layout.addOffsetListenerForBottomNavBar {
            MultiLanguagesApplication.mInstance.setIsScrollDown(it)
        }

        view.scroll_view.addScrollListenerForBottomNavBar {
            MultiLanguagesApplication.mInstance.setIsScrollDown(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        initSocketObserver()
        initBottomNavigation()
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
                    }
                }
            }
        }

        receiver.oddsChange.observe(this.viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { oddsChangeEvent ->

                //該處篩選於viewModel內是不同寫法 暫時不同步於ServiceBroadcastReceiver內
                oddsChangeEvent.filterMenuPlayCate()

                val playSelected = playCategoryAdapter.data.find { play -> play.isSelected }
                val leagueOdds = leagueAdapter.data

                leagueOdds.updateOddsSort(
                    GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key,
                    playCategoryAdapter
                ) //篩選玩法

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
                        updateBetInfo(leagueOdd, oddsChangeEvent)
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

                if(event.gameType == gameTypeAdapter.dataSport.find { gameType -> gameType.isSelected }?.code){
                    val updateLeague = leagueAdapter.data.find { it.league.id == event?.matchIdList?.firstOrNull() }
                    if(updateLeague != null){
                        val updateMatch = updateLeague.matchOdds.find { it.matchInfo?.id == event?.matchIdList?.firstOrNull() }
                        if(updateMatch != null){
                            viewModel.getFavoriteMatch()
                        }
                    }
                }
            }
        }

        receiver.closePlayCate.observe(this.viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                if (gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code != it.gameType) return@observe
                leagueAdapter.data.closePlayCate(it)
                leagueAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(leagueOdd: LeagueOdd, oddsChangeEvent: OddsChangeEvent) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (leagueOdd.matchOdds.filter { matchOdd ->
                    matchOdd.matchInfo?.id == oddsChangeEvent.eventId
                }.any { matchOdd ->
                    matchOdd.oddsMap?.values?.any { oddList ->
                        oddList?.any { odd ->
                            odd?.isSelected == true
                        } == true
                    } == true
                }) {
                viewModel.updateMatchOdd(oddsChangeEvent)
            }
        }
    }

    private fun OddsChangeEvent.updateOddsSelectedState(): OddsChangeEvent {
        this.odds.let { oddTypeSocketMap ->
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
     * 若當前選擇PlayCate可刷新, 則進行篩選, 只保留選擇的玩法
     */
    private fun OddsChangeEvent.filterMenuPlayCate() {
        val playSelected = playCategoryAdapter.data.find { it.isSelected }

        when (playSelected?.selectionType) {
            SelectionType.SELECTABLE.code -> {
                val playCateMenuCode = playSelected.playCateList?.find { it.isSelected }?.code
                this.odds.entries.retainAll { oddMap -> oddMap.key == playCateMenuCode }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getSportQuery(getLastPick = true, isReloadPlayCate != false, getFavoriteMatch = true)
        viewModel.getSportMenuFilter()
    }

    private fun initObserver() {
        viewModel.userInfo.observe(this.viewLifecycleOwner) {
            leagueAdapter.discount = it?.discount ?: 1.0F
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

        viewModel.favorMatchOddList.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { leagueOddList ->
                hideLoading()

                favorite_game_list.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                val leagueData = leagueOddList.toMutableList()
                leagueData.updateOddsSort(
                    GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key,
                    playCategoryAdapter
                )

                //檢查是否有取得我的賽事資料, 對介面進行調整
                when {
                    leagueData.isNullOrEmpty() && gameTypeAdapter.dataSport.size > 1 -> {
                        unSubscribeChannelHallAll()
                        viewModel.getSportQuery(getLastPick = false, isReloadPlayCate = true, getFavoriteMatch = true)
                        return@observe
                    }
                    leagueData.isNullOrEmpty() -> noFavoriteMatchViewState()
                    else -> {
                        showFavoriteMatchViewState()
                    }
                }

                leagueAdapter.data = leagueData
                leagueAdapter.playSelectedCodeSelectionType = getPlaySelectedCodeSelectionType()
                try {
                    /*目前流程 需要先解除再綁定 socket流程下才會回傳內容*/
                    leagueAdapter.data.forEach { leagueOdd ->
                        unSubscribeChannelHall(leagueOdd)
                    }

                    favorite_game_list?.firstVisibleRange(leagueAdapter, activity ?: requireActivity())

                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
                            quickPlayCate.quickOdds.forEach { map ->
                                map.value?.forEach { odd ->
                                    odd?.isSelected = it.any { betInfoListData ->
                                        betInfoListData.matchOdd.oddsId == odd?.id
                                    }
                                }
                            }
                        }
                    }
                }

                updateAllGameList()
            }
        }

        viewModel.favorMatchList.observe(this.viewLifecycleOwner) { favorMatchList ->

            //若用戶的最愛清單無賽事id則隱藏相關的介面, 若有則等待後續我的賽事資料判斷需不需要顯示
            if (favorMatchList.isNullOrEmpty()) {
                noFavoriteMatchViewState()
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                leagueAdapter.oddsType = oddsType
            }
        }
    }

    /**
     * 若我的賽事無資料時顯示的介面
     */
    private fun noFavoriteMatchViewState() {
        favorite_toolbar.visibility = View.VISIBLE
        fl_no_game.visibility = View.VISIBLE
        appbar_layout.visibility = View.GONE
        favorite_game_list.visibility = View.GONE
    }

    /**
     * 若我的賽事有資料時顯示的介面
     */
    private fun showFavoriteMatchViewState() {
        favorite_toolbar.visibility = View.GONE
        fl_no_game.visibility = View.GONE
        appbar_layout.visibility = View.VISIBLE
        favorite_game_list.visibility = View.VISIBLE
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
            GameType.ES.key -> getString(GameType.ES.string)
            else -> ""
        }
        if (MultiLanguagesApplication.isNightMode) {
            Glide.with(this).load(R.drawable.night_bg_300).into(favorite_bg_layer2)
        } else {
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
                    GameType.ES.key -> R.drawable.esport_100
                    else -> null
                }
            ).into(favorite_bg_layer2)
        }

    }

    private fun updatePlayCategory(plays: List<Play>?) {
        playCategoryAdapter.data = plays ?: listOf()
        if (isReloadPlayCate != false)
            view?.let { notNullView ->
                setupPlayCategory(notNullView)
                isReloadPlayCate = false
            }
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
        (activity as MyFavoriteActivity).showFastBetFragment(fastBetDataBean)
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
        StatisticsDialog.newInstance(matchId, clickListener = StatisticsDialog.StatisticsClickListener {
            when (activity) {
                is MyFavoriteActivity -> {
                    (activity as MyFavoriteActivity).clickMenuEvent()
                }
            }
        }).show(childFragmentManager, StatisticsDialog::class.java.simpleName)
    }

    private fun updateGameList(index: Int, leagueOdd: LeagueOdd) {
        leagueAdapter.data[index] = leagueOdd
        if (favorite_game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !favorite_game_list.isComputingLayout) {
            leagueAdapter.updateLeague(index, leagueOdd)
        }
    }

    private fun updateAllGameList() {
        if (favorite_game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !favorite_game_list.isComputingLayout) {
            leagueAdapter.data.forEachIndexed { index, leagueOdd -> leagueAdapter.updateLeague(index, leagueOdd) }
        }
    }

    private fun clearQuickPlayCateSelected() {
        leagueAdapter.data.forEach { leagueOdd ->
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
        selectedQuickPlayCate: QuickPlayCate
    ) {
        leagueAdapter.data.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                if (selectedMatchOdd.matchInfo?.id == matchOdd.matchInfo?.id) {
                    matchOdd.isExpand = true
                    matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                        if (selectedQuickPlayCate.code == quickPlayCate.code) quickPlayCate.isSelected = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isReloadPlayCate = null
    }
}