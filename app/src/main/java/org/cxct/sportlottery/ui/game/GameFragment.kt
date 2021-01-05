package org.cxct.sportlottery.ui.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.android.synthetic.main.fragment_game.view.*
import kotlinx.android.synthetic.main.game_bar_date.view.*
import kotlinx.android.synthetic.main.game_bar_inplay.*
import kotlinx.android.synthetic.main.game_bar_inplay.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.league.LeagueAdapter
import org.cxct.sportlottery.ui.game.odds.LeagueOddAdapter
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration


/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : BaseFragment<MainViewModel>(MainViewModel::class) {
    private val args: GameFragmentArgs by navArgs()
    private val gameTypeAdapter by lazy {
        GameTypeAdapter(GameTypeListener {
            viewModel.getLeagueList(args.matchType, it)
        })
    }
    private val gameDateAdapter by lazy {
        GameDateAdapter(GameDateListener {
            viewModel.updateDateSelectedState(it)
        })
    }

    private val leagueOddAdapter by lazy {
        LeagueOddAdapter()
    }

    private val leagueAdapter by lazy {
        LeagueAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false).apply {

            setupSportTypeRow(this)

            setupDateRow(this)

            setupOddsList(this)

            setupLeagueList(this)

            this.inplay_ou.setOnClickListener {
                viewModel.setPlayType(PlayType.OU)
            }

            this.inplay_1x2.setOnClickListener {
                viewModel.setPlayType(PlayType.X12)
            }
        }
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

    private fun setupDateRow(view: View) {
        view.date_row_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sportMenuResult.observe(this.viewLifecycleOwner, Observer {

            when (args.matchType) {
                MatchType.IN_PLAY -> {
                    setupInPlayFilterRow(it.sportMenuData?.inPlay ?: listOf())
                }
                MatchType.TODAY -> {
                    setupTodayFilterRow(it.sportMenuData?.today ?: listOf())
                }
                MatchType.EARLY -> {
                    setupEarlyFilterRow(it.sportMenuData?.early ?: listOf())
                }
                MatchType.PARLAY -> {
                    setupParlayFilterRow(it.sportMenuData?.parlay ?: listOf())
                }
                else -> {
                }
            }
        })

        viewModel.oddsListResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                hall_odds_list.visibility = View.VISIBLE
                hall_league_list.visibility = View.GONE

                leagueOddAdapter.data = it.oddsListData?.leagueOdds ?: listOf()
            }
        })

        viewModel.leagueListResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                hall_odds_list.visibility = View.GONE
                hall_league_list.visibility = View.VISIBLE

                leagueAdapter.data = it.rows ?: listOf()
            }
        })

        viewModel.curPlayType.observe(this.viewLifecycleOwner, Observer {
            inplay_ou.isSelected = (it === PlayType.OU)
            inplay_1x2.isSelected = (it == PlayType.X12)
        })

        viewModel.curDateEarly.observe(this.viewLifecycleOwner, Observer {
            gameDateAdapter.data = it
        })

        viewModel.getLeagueList(args.matchType)
        viewModel.getEarlyDateRow()
    }

    private fun setupInPlayFilterRow(sportList: List<Sport>) {
        val selectSportName = sportList.find { sport ->
            sport.isSelected
        }?.name

        gameTypeAdapter.data = sportList

        hall_inplay_row.visibility = View.VISIBLE
        inplay_sport.text = selectSportName
        hall_date_row.visibility = View.GONE
    }

    private fun setupTodayFilterRow(sportList: List<Sport>) {
        gameTypeAdapter.data = sportList

        hall_inplay_row.visibility = View.GONE
        hall_date_row.visibility = View.GONE
    }

    private fun setupEarlyFilterRow(sportList: List<Sport>) {
        gameTypeAdapter.data = sportList

        hall_inplay_row.visibility = View.GONE
        hall_date_row.visibility = View.VISIBLE
    }

    private fun setupParlayFilterRow(sportList: List<Sport>) {
        gameTypeAdapter.data = sportList

        hall_inplay_row.visibility = View.GONE
        hall_date_row.visibility = View.VISIBLE
    }

    companion object {
        @JvmStatic
        fun newInstance() {
        }
    }
}
