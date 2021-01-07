package org.cxct.sportlottery.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.league.LeagueAdapter
import org.cxct.sportlottery.ui.game.league.LeagueListener
import org.cxct.sportlottery.ui.game.odds.LeagueOddAdapter
import org.cxct.sportlottery.ui.game.odds.MatchOddListener
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration
import timber.log.Timber
import org.cxct.sportlottery.util.TimeUtil


/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : BaseFragment<MainViewModel>(MainViewModel::class) {

    private val navController by lazy {
        findNavController()
    }

    //TODO Dean : 順一下獲取List的邏輯, 目前有重複call api的問題
    private var timeRangeParams: TimeRangeParams = object : TimeRangeParams {
        override val startTime: String?
            get() = null
        override val endTime: String?
            get() = null
    }

    private val args: GameFragmentArgs by navArgs()
    private val gameTypeAdapter by lazy {
        GameTypeAdapter(GameTypeListener {
            viewModel.getLeagueList(args.matchType, it, timeRangeParams)
        })
    }
    private val gameDateAdapter by lazy {
        GameDateAdapter(GameDateListener {
            viewModel.updateDateSelectedState(it)
        })
    }

    private val leagueOddAdapter by lazy {
        LeagueOddAdapter().apply {
            matchOddListener = MatchOddListener {
                Timber.i(it.toString())
            }
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter(LeagueListener {
            val action = GameFragmentDirections.actionGameFragmentToGame2Fragment(it.list.first().id, args.matchType, timeRangeParams.startTime ?: "", timeRangeParams.endTime ?: "")
            navController.navigate(action)
        })
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
                viewModel.setPlayType(PlayType.OU_HDP)
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
        if (args.matchType == MatchType.TODAY) {
            timeRangeParams = TimeUtil.getTodayTimeRangeParams()
        } else {
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
                    setupInPlayFilter(it.sportMenuData?.menu?.inPlay?.items ?: listOf())
                }
                MatchType.TODAY -> {
                    setupTodayFilter(it.sportMenuData?.menu?.today?.items ?: listOf())
                }
                MatchType.EARLY -> {
                    setupEarlyFilter(it.sportMenuData?.menu?.early?.items ?: listOf())
                }
                MatchType.PARLAY -> {
                    setupParlayFilter(it.sportMenuData?.menu?.parlay?.items ?: listOf())
                }
                else -> {
                }
            }
        })

        viewModel.oddsListResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                hall_league_list.visibility = View.GONE
                hall_odds_list.visibility = View.VISIBLE

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
            inplay_ou.isSelected = (it === PlayType.OU_HDP)
            inplay_1x2.isSelected = (it == PlayType.X12)

            leagueOddAdapter.playType = it
        })

        viewModel.curDateEarly.observe(this.viewLifecycleOwner, Observer { pair ->
            gameDateAdapter.data = pair
            pair.find { it.second }?.first?.let {
                timeRangeParams = TimeUtil.getDayDateTimeRangeParams(it)
                viewModel.getLeagueList(args.matchType, timeRangeParams)
            }
        })

        viewModel.getEarlyDateRow()
    }

    private fun setupInPlayFilter(itemList: List<Item>) {
        val selectSportName = itemList.find { sport ->
            sport.isSelected
        }?.name

        gameTypeAdapter.data = itemList

        hall_inplay_row.visibility = View.VISIBLE
        inplay_sport.text = selectSportName
        hall_date_row.visibility = View.GONE
    }

    private fun setupTodayFilter(itemList: List<Item>) {
        gameTypeAdapter.data = itemList

        hall_inplay_row.visibility = View.GONE
        hall_date_row.visibility = View.GONE
    }

    private fun setupEarlyFilter(itemList: List<Item>) {
        gameTypeAdapter.data = itemList

        hall_inplay_row.visibility = View.GONE
        hall_date_row.visibility = View.VISIBLE
    }

    private fun setupParlayFilter(itemList: List<Item>) {
        gameTypeAdapter.data = itemList

        hall_inplay_row.visibility = View.GONE
        hall_date_row.visibility = View.VISIBLE
    }

    companion object {
        @JvmStatic
        fun newInstance() {
        }
    }
}
