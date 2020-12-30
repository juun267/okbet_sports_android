package org.cxct.sportlottery.ui.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.android.synthetic.main.fragment_game.view.*
import kotlinx.android.synthetic.main.game_bar_inplay.*
import kotlinx.android.synthetic.main.game_bar_inplay.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : BaseFragment<MainViewModel>(MainViewModel::class) {
    private val args: GameFragmentArgs by navArgs()
    private val gameTypeAdapter = GameTypeAdapter(GameTypeListener {
        viewModel.getLeagueList(args.matchType, it)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false).apply {

            this.hall_game_type_list.apply {
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

            this.inplay_ou.setOnClickListener {
                viewModel.setPlayType(PlayType.OU)
            }

            this.inplay_1x2.setOnClickListener {
                viewModel.setPlayType(PlayType.X12)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.sportMenuResult.observe(this.viewLifecycleOwner, Observer {

            when (args.matchType) {
                MatchType.IN_PLAY -> {
                    setupInPlayFilter(it.sportMenuData?.inPlay ?: listOf())
                }
                MatchType.TODAY -> {
                    setupTodayFilter(it.sportMenuData?.today ?: listOf())
                }
                MatchType.EARLY -> {
                    setupEarlyFilter(it.sportMenuData?.early ?: listOf())
                }
                MatchType.PARLAY -> {
                    setupParlayFilter(it.sportMenuData?.parlay ?: listOf())
                }
                else -> {
                }
            }
        })

        viewModel.leagueListResult.observe(this.viewLifecycleOwner,
            object : Observer<LeagueListResult> {
                override fun onChanged(t: LeagueListResult?) {
                    Timber.d("${t?.total}")
                }
            })

        viewModel.curPlayType.observe(this.viewLifecycleOwner, Observer {
            inplay_ou.isSelected = (it === PlayType.OU)
            inplay_1x2.isSelected = (it == PlayType.X12)
        })

        viewModel.getLeagueList(args.matchType)
    }

    private fun setupInPlayFilter(sportList: List<Sport>) {
        val selectSportName = sportList.find { sport ->
            sport.isSelected
        }?.name

        gameTypeAdapter.data = sportList

        hall_inplay_row.visibility = View.VISIBLE
        inplay_sport.text = selectSportName
    }

    private fun setupTodayFilter(sportList: List<Sport>) {
        gameTypeAdapter.data = sportList

        hall_inplay_row.visibility = View.GONE
    }

    private fun setupEarlyFilter(sportList: List<Sport>) {
        gameTypeAdapter.data = sportList

        hall_inplay_row.visibility = View.GONE
    }

    private fun setupParlayFilter(sportList: List<Sport>) {
        gameTypeAdapter.data = sportList

        hall_inplay_row.visibility = View.GONE
    }

    companion object {
        @JvmStatic
        fun newInstance() {
        }
    }
}
