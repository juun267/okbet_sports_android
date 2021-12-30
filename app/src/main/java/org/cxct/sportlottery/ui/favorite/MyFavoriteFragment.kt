package org.cxct.sportlottery.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_my_favorite.*
import kotlinx.android.synthetic.main.fragment_my_favorite.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueListener
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryListener
import org.cxct.sportlottery.ui.statistics.KEY_MATCH_ID
import org.cxct.sportlottery.ui.statistics.StatisticsActivity
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
                            unSubscribePlayCateChannel()
                            viewModel.switchPlay(it)
                            loading()
                        }
                    }
                } else {
                    unSubscribePlayCateChannel()
                    viewModel.switchPlay(it)
                    upDateSelectPlay(it)
                    loading()
                }

            }
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
        LeagueAdapter(MatchType.MY_EVENT).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            leagueListener = LeagueListener ({
                subscribeChannelHall(it)
            }, {})

            leagueOddListener = LeagueOddListener(
                { matchId, matchInfoList ->
                    if (matchInfoList.firstOrNull()?.isInPlay == true) {
                        matchId?.let {
                            navOddsDetailLive(matchId)
                        }
                    } else {
                        matchId?.let {
                            navOddsDetail(matchId, matchInfoList)
                        }
                    }
                },
                { matchInfo, odd, playCateCode, playCateName ->
                    addOddsDialog(matchInfo, odd, playCateCode, playCateName)
                },
                { matchId ->
                    viewModel.getQuickList(matchId)
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
                }
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

                val playSelected = playCategoryAdapter.data.find { play -> play.isSelected }
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateMatchOdds(
                                context,
                                matchOdd.apply {
                                    this.oddsMap.filter { odds -> playSelected?.code == MenuCode.MAIN.code || odds.key == playSelected?.playCateList?.firstOrNull()?.code }
                                },
                                oddsChangeEvent
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
            it?.let {
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
        subscribeSportChannelHall() //有呼叫/api/front/sport/query的页面就要订阅
        loading()
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

            favorite_game_list.adapter = leagueAdapter
            leagueAdapter.data = leagueOddList.toMutableList()
            try {
                leagueAdapter.data.forEach { leagueOdd ->
                    subscribeChannelHall(leagueOdd)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let {
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.oddsMap.values.forEach { oddList ->
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
                GameType.MR.key -> R.drawable.rancing_100
                GameType.GF.key -> R.drawable.golf_108
                else -> null
            }
        ).into(favorite_bg_layer2)
    }

    private fun updatePlayCategory(plays: List<Play>?) {
        playCategoryAdapter.data = plays ?: listOf()
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
            StatusSheetAdapter.ItemCheckedListener { _, data ->
                unSubscribePlayCateChannel()
                viewModel.switchPlayCategory(play,data.code)
                loading()
                upDateSelectPlay(play)
                (activity as BaseActivity<*>).bottomSheet.dismiss()
            })
    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        if (gameType == null || matchInfo == null) {
            return
        }

        viewModel.updateMatchBetList(
            MatchType.MY_EVENT,
            gameType,
            playCateCode,
            playCateName,
            matchInfo,
            odd,
            ChannelType.HALL
        )//TODO 訂閱HALL需傳入CateMenuCode
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
                }
                false -> {
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
    }

    /**
     * 解除訂閱當前所選擇的PlayCate(MAIN, MATCH, 1ST ...)
     */
    private fun unSubscribePlayCateChannel() {
        leagueAdapter.data.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                unSubscribeChannelHall(
                    leagueOdd.gameType?.key,
                    getPlaySelectedCode(),
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
     * 可以下拉的PlayCate, 進行oddsMap的篩選, 只留下選擇的玩法
     */
    private fun List<LeagueOdd>.filterMenuPlayCate() {
        val playSelected = playCategoryAdapter.data.find { it.isSelected }
        when (playSelected?.selectionType) {
            SelectionType.SELECTABLE.code -> {
                this.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.oddsMap.entries.retainAll { oddMap -> oddMap.key == getPlayCateMenuCode() }
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

    private fun navOddsDetailLive(matchId: String) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            val action =
                MyFavoriteFragmentDirections.actionMyFavoriteFragmentToOddsDetailLiveFragment(
                    MatchType.MY_EVENT,
                    gameType,
                    matchId
                )

            findNavController().navigate(action)
        }
    }

    private fun navStatistics(matchId: String?) {
        matchId?.let {
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
    }
}