package org.cxct.sportlottery.ui.game.v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_game_v3.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel


class GameV3Fragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameV3FragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_game_v3, container, false).apply {
            setupGameFilterRow(this)
            setupGameListView(this)
        }
    }

    private fun setupGameFilterRow(view: View) {
        view.game_filter_row.apply {
            matchType = when (args.matchType) {
                MatchType.IN_PLAY -> GameFilterRow.IN_PLAY
                MatchType.TODAY -> GameFilterRow.TODAY
                MatchType.EARLY -> GameFilterRow.EARLY
                MatchType.PARLAY -> GameFilterRow.PARLAY
                MatchType.OUTRIGHT -> GameFilterRow.OUTRIGHT
                MatchType.AT_START -> GameFilterRow.AT_START
            }

            backClickListener = View.OnClickListener {
                //TODO add back logic to view model

                if (view.game_filter_row.league != null) {
                    view.game_filter_row.league = null

                    view.game_filter_row.matchType = when (args.matchType) {
                        MatchType.IN_PLAY -> GameFilterRow.IN_PLAY
                        MatchType.TODAY -> GameFilterRow.TODAY
                        MatchType.EARLY -> GameFilterRow.EARLY
                        MatchType.PARLAY -> GameFilterRow.PARLAY
                        MatchType.OUTRIGHT -> GameFilterRow.OUTRIGHT
                        MatchType.AT_START -> GameFilterRow.AT_START
                    }

                    viewModel.getGameHallList(args.matchType, true)
                    loading()
                }
            }

            ouHDPClickListener = View.OnClickListener {
                viewModel.setPlayType(PlayType.OU_HDP)
            }

            x12ClickListener = View.OnClickListener {
                viewModel.setPlayType(PlayType.X12)
            }

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    //TODO add query logic to view model
                    return true
                }
            }

            sportTypeListener = SportTypeListener {
                viewModel.getGameHallList(args.matchType, it)
            }

            gameTypeListener = GameTypeListener {
                //TODO add 滾球賽事
                viewModel.getGameHallList(args.matchType, it)
            }
        }
    }

    private fun setupGameListView(view: View) {
        view.game_list_view.apply {
            matchType = when (args.matchType) {
                MatchType.IN_PLAY -> GameFilterRow.IN_PLAY
                MatchType.TODAY -> GameFilterRow.TODAY
                MatchType.EARLY -> GameFilterRow.EARLY
                MatchType.PARLAY -> GameFilterRow.PARLAY
                MatchType.OUTRIGHT -> GameFilterRow.OUTRIGHT
                MatchType.AT_START -> GameFilterRow.AT_START
            }

            countryLeagueListener = CountryLeagueListener {
                view.game_filter_row.league = it

                viewModel.getLeagueOddsList(args.matchType, it.id)
                loading()
            }

            leagueOddListener = LeagueOddListener {
                //TODO open live and play type page
                it.matchInfo?.id
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sportMenuResult.observe(this.viewLifecycleOwner, Observer {
            when (args.matchType) {
                MatchType.IN_PLAY -> {
                    game_filter_row.sportList = it?.sportMenuData?.menu?.inPlay?.items ?: listOf()
                }

                MatchType.TODAY -> {
                    game_filter_row.sportList = it?.sportMenuData?.menu?.today?.items ?: listOf()
                }

                MatchType.EARLY -> {
                    game_filter_row.sportList = it?.sportMenuData?.menu?.early?.items ?: listOf()
                }

                MatchType.PARLAY -> {
                    game_filter_row.sportList = it?.sportMenuData?.menu?.parlay?.items ?: listOf()
                }

                MatchType.OUTRIGHT -> {
                    game_filter_row.sportList = it?.sportMenuData?.menu?.outright?.items ?: listOf()
                }

                MatchType.AT_START -> {
                    game_filter_row.sportList = it?.sportMenuData?.atStart?.items ?: listOf()
                }
            }
        })

        viewModel.curPlayType.observe(viewLifecycleOwner, Observer {
            game_filter_row.playType = it
        })

        viewModel.curDate.observe(this.viewLifecycleOwner, Observer {
            game_filter_row.dateList = it
        })

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner, Observer {
            hideLoading()
            if (it != null && it.success) {
                game_list_view.leagueOddList = it.oddsListData?.leagueOdds ?: listOf()
            }
        })

        viewModel.oddsListResult.observe(this.viewLifecycleOwner, Observer {
            hideLoading()
            if (it != null && it.success) {
                game_list_view.leagueOddList = it.oddsListData?.leagueOdds ?: listOf()
            }
        })

        viewModel.leagueListResult.observe(this.viewLifecycleOwner, Observer {
            hideLoading()
            if (it != null && it.success) {
                game_list_view.countryList = it.rows ?: listOf()
            }
        })

        viewModel.getGameHallList(args.matchType, true)
        loading()
    }
}