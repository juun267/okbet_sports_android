package org.cxct.sportlottery.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.android.synthetic.main.fragment_game.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListResult
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.common.MatchTypeRow
import org.cxct.sportlottery.ui.game.league.LeagueAdapter
import org.cxct.sportlottery.ui.game.league.LeagueListener
import org.cxct.sportlottery.ui.game.odds.ItemExpandListener
import org.cxct.sportlottery.ui.game.odds.LeagueOddAdapter
import org.cxct.sportlottery.ui.game.odds.MatchOddListener
import org.cxct.sportlottery.ui.game.outright.season.SeasonAdapter
import org.cxct.sportlottery.ui.game.outright.season.SeasonSubAdapter
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : BaseSocketFragment<MainViewModel>(MainViewModel::class) {
    private val args: GameFragmentArgs by navArgs()

    private val gameTypeAdapter by lazy {
        GameTypeAdapter(GameTypeListener {
            viewModel.getGameHallList(args.matchType, it)
        })
    }

    private val gameDateAdapter by lazy {
        GameDateAdapter(GameDateListener {
            viewModel.getGameHallList(args.matchType, it)
        })
    }

    private val leagueOddAdapter by lazy {
        LeagueOddAdapter().apply {
            matchOddListener = MatchOddListener(
                {
                    viewModel.getOddsDetail(it.matchInfo?.id)
                },
                { matchOdd, oddString, odd ->
                    viewModel.updateMatchBetList(
                        matchOdd,
                        oddString,
                        odd
                    )
                })

            itemExpandListener = ItemExpandListener { isExpand, leagueOdd, position ->
                val code = gameTypeAdapter.data.find {
                    it.isSelected
                }?.code
                val eventId = leagueOdd.matchOdds[0].matchInfo?.id

                if (isExpand) {
                    service.subscribeHallChannel(code, eventId)
                } else {
                    service.unSubscribeHallChannel(code, eventId)
                }
            }

            betInfoListData = viewModel.betInfoRepository.betInfoList.value
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter(LeagueListener {
            viewModel.getLeagueOddsList(args.matchType, it)
        })
    }

    private val outrightSeasonAdapter by lazy {
        SeasonAdapter().apply {
            seasonSubListener = SeasonSubAdapter.SeasonSubListener {
                viewModel.getOutrightOddsList(it.id)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_game, container, false).apply {

            setupSportTypeRow(this)

            setupMatchTypeRow(this)

            setupDateRow(this)

            setupOddsList(this)

            setupLeagueList(this)

            setupOutrightSeasonList(this)

            initObserve()

            initSocketObserver()
        }
    }

    private fun initObserve() {
        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, Observer {
            leagueOddAdapter.betInfoListData = it
        })
    }

    private fun initSocketObserver() {
        receiver.matchClock.observe(this.viewLifecycleOwner, Observer { matchClockEvent ->
            if (matchClockEvent == null) return@Observer

            val leagueOdds = leagueOddAdapter.data

            leagueOdds.forEach {
                val updateMatchOdd = it.matchOdds.find { matchOdd ->
                    matchOdd.matchInfo?.id == matchClockEvent.matchClockCO?.matchId
                }
                updateMatchOdd?.leagueTime = matchClockEvent.matchClockCO?.matchTime
            }

            leagueOddAdapter.data = leagueOdds
        })

        receiver.oddsChange.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            val leagueOdds = leagueOddAdapter.data

            leagueOdds.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.odds.forEach { odds ->
                        val socketOdds = it.odds[odds.key]
                        val originOdds = odds.value

                        originOdds.forEach { originOdd ->
                            val updateOdd = socketOdds?.find { socketOdd ->
                                originOdd.id == socketOdd.id
                            }

                            val originOddValue = originOdd.odds
                            val updateOddValue = updateOdd?.odds

                            originOddValue?.let {
                                updateOddValue?.let {
                                    Timber.i("$originOddValue -> $updateOddValue")

                                    //update Odd state
                                    when {
                                        originOddValue > updateOddValue -> {
                                            originOdd.oddState = OddState.SMALLER.state
                                        }
                                        originOddValue < updateOddValue -> {
                                            originOdd.oddState = OddState.LARGER.state
                                        }
                                        originOddValue == updateOddValue -> {
                                            originOdd.oddState = OddState.SAME.state
                                        }
                                    }

                                    //update Odd value
                                    originOdd.odds = updateOdd.odds

                                    //update Odd status
                                    originOdd.status = updateOdd.status
                                }
                            }
                        }
                    }
                }
            }

            leagueOddAdapter.data = leagueOdds
        })

        receiver.matchStatusChange.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            val leagueOdds = leagueOddAdapter.data

            leagueOdds.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    if (matchOdd.matchInfo?.id == it.matchStatusCO?.matchId) {
                        matchOdd.matchInfo?.homeScore = it.matchStatusCO?.homeScore
                        matchOdd.matchInfo?.awayScore = it.matchStatusCO?.awayScore
                        matchOdd.matchInfo?.statusName = it.matchStatusCO?.statusName
                    }
                }
            }

            leagueOddAdapter.data = leagueOdds
        })

        updateSocketGlobalStop()

        updateSocketProducerUp()
    }

    private fun updateSocketGlobalStop() {
        receiver.globalStop.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            when (val stopProducerId = it.producerId) {
                null -> {
                    updateAllOddStatus(BetStatus.LOCKED)
                }
                else -> {
                    updateOddStatus(stopProducerId, BetStatus.LOCKED)
                }
            }
        })
    }

    private fun updateSocketProducerUp() {
        receiver.producerUp.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            when (val upProducerId = it.producerId) {
                null -> {
                    updateAllOddStatus(BetStatus.ACTIVATED)
                }
                else -> {
                    updateOddStatus(upProducerId, BetStatus.ACTIVATED)
                }
            }
        })
    }

    private fun updateAllOddStatus(betStatus: BetStatus) {
        val leagueOdds = leagueOddAdapter.data
        leagueOdds.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.odds.values.forEach { odds ->
                    odds.forEach { odd ->

                        odd.status = betStatus.code
                    }
                }
            }
        }

        leagueOddAdapter.data = leagueOdds
    }

    private fun updateOddStatus(stopProducerId: Int, betStatus: BetStatus) {
        val leagueOdds = leagueOddAdapter.data
        leagueOdds.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.odds.values.forEach { odds ->
                    val updateOdd = odds.find { odd ->
                        odd.producerId == stopProducerId
                    }

                    updateOdd?.status = betStatus.code
                }
            }
        }

        leagueOddAdapter.data = leagueOdds
    }

    private fun setupSportTypeRow(view: View) {
        view.hall_game_type_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = gameTypeAdapter
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec
                )
            )
        }
    }

    private fun setupMatchTypeRow(view: View) {
        view.hall_match_type_row.ouHDPClickListener = View.OnClickListener {
            viewModel.setPlayType(PlayType.OU_HDP)
        }
        view.hall_match_type_row.x12ClickListener = View.OnClickListener {
            viewModel.setPlayType(PlayType.X12)
        }
    }

    private fun setupDateRow(view: View) {
        view.hall_date_list.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = gameDateAdapter
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec
                )
            )
        }
    }

    private fun setupOddsList(view: View) {
        view.hall_odds_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = leagueOddAdapter
            this.addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun setupLeagueList(view: View) {
        view.hall_league_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = leagueAdapter
            this.addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun setupOutrightSeasonList(view: View) {
        view.hall_outright_season_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = outrightSeasonAdapter
            this.addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sportMenuResult.observe(this.viewLifecycleOwner, Observer {
            when (args.matchType) {
                MatchType.IN_PLAY -> {
                    setupInPlayFilter(it?.sportMenuData?.menu?.inPlay?.items ?: listOf())
                }
                MatchType.TODAY -> {
                    setupTodayFilter(it?.sportMenuData?.menu?.today?.items ?: listOf())
                }
                MatchType.EARLY -> {
                    setupEarlyFilter(it?.sportMenuData?.menu?.early?.items ?: listOf())
                }
                MatchType.PARLAY -> {
                    setupParlayFilter(it?.sportMenuData?.menu?.parlay?.items ?: listOf())
                }
                MatchType.OUTRIGHT -> {
                    setupOutrightFilter(it?.sportMenuData?.menu?.outright?.items ?: listOf())
                }
                MatchType.AT_START -> {
                    setupAtStartFilter(it?.sportMenuData?.atStart?.items ?: listOf())
                }
            }
        })

        viewModel.curPlayType.observe(this.viewLifecycleOwner, Observer {
            hall_match_type_row.curPlayType = it
            leagueOddAdapter.playType = it
        })

        viewModel.curDate.observe(this.viewLifecycleOwner, Observer {
            gameDateAdapter.data = it
        })

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner, Observer {
            if (it != null && it.success) {
                setupGameHallList(it)
            }
        })

        viewModel.leagueListResult.observe(this.viewLifecycleOwner, Observer {
            if (it != null && it.success) {
                setupGameHallList(it)
            }
        })

        viewModel.outrightSeasonListResult.observe(this.viewLifecycleOwner, Observer {
            if (it != null && it.success) {
                setupGameHallList(it)
            }
        })

        viewModel.isNoHistory.observe(this.viewLifecycleOwner, Observer {
            setupNoHistory(it)
        })

        viewModel.getGameHallList(args.matchType, true)
    }

    private fun setupInPlayFilter(itemList: List<Item>) {
        val selectSportName = itemList.find { sport ->
            sport.isSelected
        }?.name

        gameTypeAdapter.data = itemList

        hall_match_type_row.apply {
            type = MatchTypeRow.IN_PLAY
            sport = selectSportName
        }

        hall_date_list.visibility = View.GONE
    }

    private fun setupTodayFilter(itemList: List<Item>) {
        gameTypeAdapter.data = itemList

        hall_match_type_row.type = MatchTypeRow.TODAY
        hall_date_list.visibility = View.GONE
    }

    private fun setupEarlyFilter(itemList: List<Item>) {
        gameTypeAdapter.data = itemList

        hall_match_type_row.type = MatchTypeRow.EARLY
        hall_date_list.visibility = View.VISIBLE
    }

    private fun setupParlayFilter(itemList: List<Item>) {
        gameTypeAdapter.data = itemList

        hall_match_type_row.type = MatchTypeRow.PARLAY
        hall_date_list.visibility = View.VISIBLE
    }

    private fun setupOutrightFilter(itemList: List<Item>) {
        gameTypeAdapter.data = itemList

        hall_match_type_row.type = MatchTypeRow.OUTRIGHT
        hall_date_list.visibility = View.GONE
    }

    private fun setupAtStartFilter(itemList: List<Item>) {
        val selectSportName = itemList.find { sport ->
            sport.isSelected
        }?.name

        gameTypeAdapter.data = itemList

        hall_match_type_row.apply {
            type = MatchTypeRow.AT_START
            sport = selectSportName
        }

        hall_date_list.visibility = View.GONE
    }

    private fun setupGameHallList(baseResult: BaseResult) {
        when (baseResult) {
            is OddsListResult -> setupOddList(baseResult)
            is LeagueListResult -> setupLeagueList(baseResult)
            is OutrightSeasonListResult -> setupOutrightSeasonList(baseResult)
        }
    }

    private fun setupOddList(oddsListResult: OddsListResult) {
        hall_league_list.visibility = View.GONE
        hall_outright_season_list.visibility = View.GONE
        hall_odds_list.visibility = View.VISIBLE

        leagueOddAdapter.data = oddsListResult.oddsListData?.leagueOdds ?: listOf()
    }

    private fun setupLeagueList(leagueListResult: LeagueListResult) {
        hall_odds_list.visibility = View.GONE
        hall_outright_season_list.visibility = View.GONE
        hall_league_list.visibility = View.VISIBLE

        leagueAdapter.data = leagueListResult.rows ?: listOf()
    }

    private fun setupOutrightSeasonList(outrightSeasonListResult: OutrightSeasonListResult) {
        hall_odds_list.visibility = View.GONE
        hall_league_list.visibility = View.GONE
        hall_outright_season_list.visibility = View.VISIBLE

        outrightSeasonAdapter.data = outrightSeasonListResult.rows ?: listOf()
    }

    private fun setupNoHistory(isShow: Boolean) {
        if (isShow) {
            hall_odds_list.visibility = View.GONE
            hall_outright_season_list.visibility = View.GONE
            hall_league_list.visibility = View.GONE

            hall_no_history_img.visibility = View.VISIBLE
            hall_no_history_title.visibility = View.VISIBLE
            hall_no_history_content.visibility = View.VISIBLE
        } else {
            hall_no_history_img.visibility = View.GONE
            hall_no_history_title.visibility = View.GONE
            hall_no_history_content.visibility = View.GONE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() {
        }
    }
}
