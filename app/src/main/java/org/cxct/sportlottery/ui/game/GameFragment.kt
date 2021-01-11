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
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.common.*
import org.cxct.sportlottery.ui.game.league.LeagueAdapter
import org.cxct.sportlottery.ui.game.league.LeagueListener
import org.cxct.sportlottery.ui.game.odds.LeagueOddAdapter
import org.cxct.sportlottery.ui.game.odds.MatchOddListener
import org.cxct.sportlottery.ui.game.outright.OutrightOddAdapter
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.TimeUtil
import timber.log.Timber


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
            viewModel.getGameHallList(args.matchType, it, timeRangeParams)
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
                viewModel.getOddsDetail(it.matchInfo.id)
            }
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter(LeagueListener {
            val action = GameFragmentDirections.actionGameFragmentToGame2Fragment(
                it.list.first().id,
                args.matchType,
                timeRangeParams.startTime ?: "",
                timeRangeParams.endTime ?: ""
            )
            navController.navigate(action)
        })
    }

    private val outrightOddAdapter by lazy {
        OutrightOddAdapter()
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

            setupMatchTypeRow(this)

            setupDateRow(this)

            setupOddsList(this)

            setupLeagueList(this)

            setupOutrightOddsList(this)
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

    private fun setupMatchTypeRow(view: View) {
        view.hall_match_type_row.ouHDPClickListener = View.OnClickListener {
            viewModel.setPlayType(PlayType.OU_HDP)
        }
        view.hall_match_type_row.x12ClickListener = View.OnClickListener {
            viewModel.setPlayType(PlayType.X12)
        }
        view.hall_match_type_row.matchClickListener = View.OnClickListener {
            viewModel.getGameHallList(args.matchType, timeRangeParams)
        }
        view.hall_match_type_row.outrightClickListener = View.OnClickListener {
            //TODO replace timber to get outright list
            Timber.i("click outright entrance")
        }
    }

    private fun setupDateRow(view: View) {
        if (args.matchType == MatchType.TODAY) {
            timeRangeParams = TimeUtil.getTodayTimeRangeParams()
        } else {
            view.hall_date_list.apply {
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

    private fun setupOutrightOddsList(view: View) {
        view.hall_outright_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = outrightOddAdapter
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
                MatchType.OUTRIGHT -> {
                    setupOutrightFilter(it.sportMenuData?.menu?.outright?.items ?: listOf())
                }
                else -> {
                }
            }
        })

        viewModel.oddsListResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                hall_league_list.visibility = View.GONE
                hall_outright_list.visibility = View.GONE
                hall_odds_list.visibility = View.VISIBLE

                leagueOddAdapter.data = it.oddsListData?.leagueOdds ?: listOf()
            }
        })

        viewModel.leagueListResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                hall_odds_list.visibility = View.GONE
                hall_outright_list.visibility = View.GONE
                hall_league_list.visibility = View.VISIBLE

                leagueAdapter.data = it.rows ?: listOf()
            }
        })

        viewModel.outrightOddsListResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                hall_odds_list.visibility = View.GONE
                hall_league_list.visibility = View.GONE
                hall_outright_list.visibility = View.VISIBLE

                outrightOddAdapter.data = it.outrightOddsListData?.leagueOdds ?: listOf()
            }
        })

        viewModel.curPlayType.observe(this.viewLifecycleOwner, Observer {
            hall_match_type_row.curPlayType = it

            leagueOddAdapter.playType = it
        })

        viewModel.curDateEarly.observe(this.viewLifecycleOwner, Observer { pair ->
            gameDateAdapter.data = pair
            pair.find { it.second }?.first?.let {
                timeRangeParams = if (it == getString(R.string.date_row_other)) {
                    TimeUtil.getOtherEarlyDateTimeRangeParams()
                } else {
                    TimeUtil.getDayDateTimeRangeParams(it)
                }
                viewModel.getGameHallList(args.matchType, timeRangeParams)
            }
        })

        viewModel.getEarlyDateRow()
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

    companion object {
        @JvmStatic
        fun newInstance() {
        }
    }
}
