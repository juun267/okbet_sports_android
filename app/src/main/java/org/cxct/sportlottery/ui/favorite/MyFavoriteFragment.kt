package org.cxct.sportlottery.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.fragment_my_favorite.*
import kotlinx.android.synthetic.main.fragment_my_favorite.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
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
import org.cxct.sportlottery.ui.game.PlayCateUtils
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryListener
import org.cxct.sportlottery.ui.menu.OddsType
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
                viewModel.switchPlay(it)
            }
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter(MatchType.MY_EVENT).apply {
            leagueOddListener = LeagueOddListener(
                { _, _ ->
                    //TODO 目前後端回傳資料無法分辨MatchType類型，等可以分辨時會在區分要連到OddsDetail/OddsDetailLive
                },
                { matchInfo, odd, playCateName, playName ->
                    addOddsDialog(matchInfo, odd, playCateName, playName)
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

            this.adapter = leagueAdapter
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        initSocketObserver()
    }

    private fun initSocketObserver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, {
            it?.let { matchStatusChangeEvent ->
                matchStatusChangeEvent.matchStatusCO?.let { matchStatusCO ->
                    matchStatusCO.matchId?.let { matchId ->

                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEach { leagueOdd ->
                            if (leagueOdd.isExpand) {

                                val updateMatchOdd = leagueOdd.matchOdds.find { matchOdd ->
                                    matchOdd.matchInfo?.id == matchId
                                }
                                //TODO Bill matchStatusCO.status == 100 如果沒有資料的UI顯示 待確認
                                updateMatchOdd?.let {
                                    if (matchStatusCO.status == 100) {
                                        leagueOdd.matchOdds.remove(updateMatchOdd)
                                        leagueAdapter.notifyItemRangeChanged(
                                            leagueOdds.indexOf(leagueOdd),
                                            leagueAdapter.itemCount
                                        )
                                    } else {
                                        updateMatchOdd.matchInfo?.homeScore =
                                            matchStatusCO.homeScore
                                        updateMatchOdd.matchInfo?.awayScore =
                                            matchStatusCO.awayScore
                                        updateMatchOdd.matchInfo?.statusName =
                                            matchStatusCO.statusName

                                        leagueAdapter.notifyItemChanged(leagueOdds.indexOf(leagueOdd))
                                    }
                                }
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

                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEach { leagueOdd ->
                            if (leagueOdd.isExpand) {

                                val updateMatchOdd = leagueOdd.matchOdds.find { matchOdd ->
                                    matchOdd.matchInfo?.id == matchId
                                }

                                updateMatchOdd?.let {
                                    updateMatchOdd.leagueTime = when (matchClockCO.gameType) {
                                        GameType.FT.key -> {
                                            matchClockCO.matchTime
                                        }
                                        GameType.BK.key -> {
                                            matchClockCO.remainingTimeInPeriod
                                        }
                                        else -> null
                                    }

                                    leagueAdapter.notifyItemChanged(leagueOdds.indexOf(leagueOdd))
                                }
                            }
                        }
                    }
                }
            }
        })

        receiver.oddsChange.observe(this.viewLifecycleOwner, {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.updateOddsSelectedState()
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->
                    val leagueOdds = leagueAdapter.data
                    val oddsType = leagueAdapter.oddsType

                    leagueOdds.forEach { leagueOdd ->
                        if (leagueOdd.isExpand) {
                            val updateMatchOdd = leagueOdd.matchOdds.find { matchOdd ->
                                matchOdd.matchInfo?.id == it.eventId
                            }

                            if (updateMatchOdd?.odds.isNullOrEmpty()) {
                                updateMatchOdd?.odds = PlayCateUtils.filterOdds(
                                    oddTypeSocketMap.toMutableMap(),
                                    updateMatchOdd?.matchInfo?.gameType ?: ""
                                )

                            } else {
                                updateMatchOdd?.odds?.forEach { oddTypeMap ->
                                    val oddsSocket = oddTypeSocketMap[oddTypeMap.key]
                                    val odds = oddTypeMap.value

                                    odds.forEach { odd ->
                                        val oddSocket = oddsSocket?.find { oddSocket ->
                                            oddSocket?.id == odd?.id
                                        }

                                        oddSocket?.let {
                                            odd?.updateOddsState(oddSocket, oddsType)
                                        }
                                    }
                                }

                                updateMatchOdd?.quickPlayCateList?.forEach { quickPlayCate ->
                                    quickPlayCate.quickOdds?.forEach { oddTypeMap ->
                                        val oddsSocket = oddTypeSocketMap[oddTypeMap.key]
                                        val odds = oddTypeMap.value

                                        odds.forEach { odd ->
                                            val oddSocket = oddsSocket?.find { oddSocket ->
                                                oddSocket?.id == odd?.id
                                            }

                                            oddSocket?.let {
                                                odd?.updateOddsState(oddSocket, oddsType)
                                            }
                                        }
                                    }
                                }
                            }

                            leagueAdapter.notifyItemChanged(
                                leagueOdds.indexOf(
                                    leagueOdd
                                )
                            )
                        }
                    }
                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.odds.values.forEach { odds ->
                            odds.forEach { odd ->
                                odd?.updateOddStatus(globalStopEvent.producerId)
                            }
                        }

                        matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                            quickPlayCate.quickOdds?.values?.forEach { odds ->
                                odds.forEach { odd ->
                                    odd?.updateOddStatus(globalStopEvent.producerId)
                                }
                            }
                        }
                    }

                    leagueAdapter.notifyItemChanged(leagueOdds.indexOf(leagueOdd))
                }
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            it?.let {
                unSubscribeChannelHallAll()
                subscribeHallChannel()
            }
        })
    }

    //TODO Bill 先放這裡
    private fun Odd.updateOddsState(oddSocket: Odd, oddsType: OddsType): Odd {
        when (oddsType) {
            OddsType.EU -> {
                this.odds?.let { oddValue ->
                    oddSocket.odds?.let { oddSocketValue ->
                        when {
                            oddValue > oddSocketValue -> {
                                this.oddState =
                                    OddState.SMALLER.state
                            }
                            oddValue < oddSocketValue -> {
                                this.oddState =
                                    OddState.LARGER.state
                            }
                            oddValue == oddSocketValue -> {
                                this.oddState =
                                    OddState.SAME.state
                            }
                        }
                    }
                }
            }

            OddsType.HK -> {
                this.hkOdds?.let { oddValue ->
                    oddSocket.hkOdds?.let { oddSocketValue ->
                        when {
                            oddValue > oddSocketValue -> {
                                this.oddState =
                                    OddState.SMALLER.state
                            }
                            oddValue < oddSocketValue -> {
                                this.oddState =
                                    OddState.LARGER.state
                            }
                            oddValue == oddSocketValue -> {
                                this.oddState =
                                    OddState.SAME.state
                            }
                        }
                    }
                }
            }
        }

        this.odds = oddSocket.odds
        this.hkOdds = oddSocket.hkOdds
        this.status = oddSocket.status
        this.extInfo = oddSocket.extInfo

        return this
    }

    private fun Odd.updateOddStatus(producerId: Int?): Odd {
        when (producerId) {
            null -> {
                this.status = BetStatus.DEACTIVATED.code
            }
            else -> {
                if (this.producerId == producerId) {
                    this.status = BetStatus.DEACTIVATED.code
                }
            }
        }
        return this
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

    override fun onStart() {
        super.onStart()

        viewModel.getSportQuery()
        loading()
    }

    private fun initObserver() {
        viewModel.sportQueryData.observe(this.viewLifecycleOwner, {
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
        })

        viewModel.curPlay.observe(this.viewLifecycleOwner, {
            showPlayCateBottomSheet(it)
        })

        viewModel.favorMatchOddList.observe(this.viewLifecycleOwner, {
            hideLoading()
            leagueAdapter.data = it.toMutableList()
            try {
                it.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        subscribeChannelHall(
                            leagueOdd.gameType?.key,
                            MenuCode.MAIN.code,
                            matchOdd.matchInfo?.id
                        )
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, {
            it.peekContent().let {
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.odds.values.forEach { oddList ->
                            oddList.forEach { odd ->
                                odd?.isSelected = it.any { betInfoListData ->
                                    betInfoListData.matchOdd.oddsId == odd?.id
                                }
                            }
                        }
                    }
                }

                leagueAdapter.notifyDataSetChanged()
            }
        })

        viewModel.favorMatchList.observe(this.viewLifecycleOwner, {
            if (it.isNullOrEmpty()) {
                fl_no_game.visibility = View.VISIBLE
            } else {
                fl_no_game.visibility = View.GONE
            }
        })
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
                viewModel.switchPlayCategory(data.code)
                (activity as BaseActivity<*>).bottomSheet.dismiss()
            })
    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateName: String,
        playName: String
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        if (gameType == null || matchInfo == null) {
            return
        }

        viewModel.updateMatchBetList(
            MatchType.MY_EVENT,
            gameType,
            playCateName,
            playName,
            matchInfo,
            odd,
            ChannelType.HALL
        )//TODO 訂閱HALL需傳入CateMenuCode
    }

    private fun subscribeHallChannel() {
        leagueAdapter.data.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                subscribeChannelHall(
                    leagueOdd.gameType?.key,
                    MenuCode.MAIN.code,
                    matchOdd.matchInfo?.id
                )
            }
        }
    }
}