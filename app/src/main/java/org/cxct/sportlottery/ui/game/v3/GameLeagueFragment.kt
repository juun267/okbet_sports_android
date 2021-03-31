package org.cxct.sportlottery.ui.game.v3

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_league.*
import kotlinx.android.synthetic.main.fragment_game_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel


private const val ARG_MATCH_TYPE = "matchType"
private const val ARG_SPORT_TYPE = "sportType"
private const val ARG_EVENT_ID = "eventId"

class GameLeagueFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    companion object {
        fun newInstance(matchType: String, sportType: String, eventId: String) =
            GameLeagueFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MATCH_TYPE, matchType)
                    putString(ARG_SPORT_TYPE, sportType)
                    putString(ARG_EVENT_ID, eventId)
                }
            }
    }

    private lateinit var matchType: MatchType
    private var sportType: String? = null
    private var eventId: String? = null

    private val leagueAdapter by lazy {
        LeagueAdapter(matchType).apply {
            leagueOddListener = LeagueOddListener(
                { matchOdd ,_ ->
                    //TODO open live and play type page
                    viewModel.getOddsDetail(matchOdd.matchInfo?.id)
                },
                { matchOdd, oddString, odd ->
                    viewModel.updateMatchBetList(matchOdd, oddString, odd)
                }
            )

            itemExpandListener = ItemExpandListener {
                when (it.isExpand) {
                    true -> {
                        it.matchOdds.forEach { matchOdd ->
                            service.subscribeHallChannel(
                                sportType,
                                CateMenuCode.HDP_AND_OU.code,
                                matchOdd.matchInfo?.id
                            )
                        }
                    }

                    false -> {
                        it.matchOdds.forEach { matchOdd ->
                            service.unsubscribeHallChannel(
                                sportType,
                                CateMenuCode.HDP_AND_OU.code,
                                matchOdd.matchInfo?.id
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            sportType = it.getString(ARG_SPORT_TYPE)
            matchType = when (it.getString(ARG_MATCH_TYPE)) {
                MatchType.IN_PLAY.postValue -> MatchType.IN_PLAY
                MatchType.TODAY.postValue -> MatchType.TODAY
                MatchType.EARLY.postValue -> MatchType.EARLY
                MatchType.PARLAY.postValue -> MatchType.PARLAY
                MatchType.OUTRIGHT.postValue -> MatchType.OUTRIGHT
                else -> MatchType.AT_START
            }
            eventId = it.getString(ARG_EVENT_ID)
        }

        service.subscribeHallChannel(sportType, CateMenuCode.HDP_AND_OU.code, eventId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_league, container, false).apply {
            setupGameFilterRow(this)
            setupLeagueOddList(this)
        }
    }

    private fun setupGameFilterRow(view: View) {
        view.game_league_filter_row.apply {

            searchHint = getString(R.string.game_filter_row_search_hint_league)

            backClickListener = View.OnClickListener {
                backEvent()
            }

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        viewModel.searchMatch(it)
                    }
                    return true
                }
            }
        }
    }

    private fun setupLeagueOddList(view: View) {
        view.game_league_odd_list.apply {

            this.layoutManager =
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

            this.adapter = leagueAdapter

            this.addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initObserve()
            initSocketReceiver()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initObserve() {
        viewModel.oddsListResult.observe(this.viewLifecycleOwner, Observer {

            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success) {
                    game_league_filter_row.sportName = oddsListResult.oddsListData?.sport?.name
                    leagueAdapter.data = oddsListResult.oddsListData?.leagueOdds ?: listOf()
                }
            }
        })

        viewModel.leagueListSearchResult.observe(this.viewLifecycleOwner, Observer {
            leagueAdapter.data = it
        })
    }

    private fun initSocketReceiver() {
        receiver.oddsChange.observe(this.viewLifecycleOwner, Observer {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->
                    val leagueOdds = leagueAdapter.data

                    leagueOdds.forEach { leagueOdd ->
                        if (leagueOdd.isExpand) {

                            leagueOdd.matchOdds.forEach { matchOdd ->
                                matchOdd.odds.forEach { oddTypeMap ->

                                    val oddsSocket = oddTypeSocketMap[oddTypeMap.key]
                                    val odds = oddTypeMap.value

                                    odds.forEach { odd ->
                                        odd?.let { oddNonNull ->
                                            val oddSocket = oddsSocket?.find { oddSocket ->
                                                oddSocket.id == odd.id
                                            }

                                            oddSocket?.let { oddSocketNonNull ->

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

                                                oddNonNull.odds = oddSocketNonNull.odds

                                                oddNonNull.status = oddSocketNonNull.status

                                                leagueAdapter.notifyItemChanged(
                                                    leagueOdds.indexOf(
                                                        leagueOdd
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, Observer {
            it?.let { globalStopEvent ->

                val leagueOdds = leagueAdapter.data

                leagueOdds.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
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

                                leagueAdapter.notifyItemChanged(leagueOdds.indexOf(leagueOdd))
                            }
                        }

                    }
                }
            }
        })
    }

    private fun backEvent() {
        val animation: Animation =
            AnimationUtils.loadAnimation(requireActivity(), R.anim.exit_to_right)
        animation.duration = resources.getInteger(R.integer.config_navAnimTime).toLong()
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                parentFragmentManager.popBackStack()
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        this.view?.startAnimation(animation)
    }

    override fun onResume() {
        super.onResume()

        requireView().setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                backEvent()
                return@OnKeyListener true
            }
            false
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        leagueAdapter.data.forEach {
            if (it.isExpand) {
                it.matchOdds.forEach { matchOdd ->
                    service.unsubscribeHallChannel(
                        sportType,
                        CateMenuCode.HDP_AND_OU.code,
                        matchOdd.matchInfo?.id
                    )
                }
            }
        }
    }
}