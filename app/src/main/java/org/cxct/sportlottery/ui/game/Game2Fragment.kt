package org.cxct.sportlottery.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.itemview_league_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.odds.LeagueOddAdapter
import org.cxct.sportlottery.ui.game.odds.MatchOddAdapter
import org.cxct.sportlottery.ui.home.MainViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Game2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Game2Fragment : BaseFragment<MainViewModel>(MainViewModel::class) {

    private val args: Game2FragmentArgs by navArgs()
    private val navController by lazy {
        findNavController()
    }
    private val timeRangeParams = object : TimeRangeParams {
        override val startTime: String?
            get() = args.startTime
        override val endTime: String?
            get() = args.endTime

    }

    private val playType: PlayType by lazy { PlayType.OU_HDP }

    private val leagueOddAdapter by lazy {
        LeagueOddAdapter()
    }

    private val matchOddAdapter by lazy {
        MatchOddAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getLeagueOddsList()

        viewModel.oddsListResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                leagueOddAdapter.data = it.oddsListData?.leagueOdds ?: listOf()
                it.oddsListData?.leagueOdds?.getOrNull(0)?.let { leagueOdd ->
                    this.view?.let { view ->
                        setupMatchOddList(view, leagueOdd)
                        setupLeagueLayout(view, it.oddsListData.leagueOdds[0])
                        setupBackEvent(view)
                    }
                }
            }
        })
    }

    private fun getLeagueOddsList() {
        viewModel.getLeagueOddsList(args.matchType, args.oddsListId, timeRangeParams)
    }

    private fun setupMatchOddList(view: View, item: LeagueOdd) {
        view.league_odd_sub_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = matchOddAdapter
        }

        matchOddAdapter.data = item.matchOdds
        matchOddAdapter.playType = playType
    }

    private fun setupLeagueLayout(view: View, item: LeagueOdd) {
        view.apply {
            league_odd_name.text = item.league.name
            league_odd_count.text = item.matchOdds.size.toString()
        }
    }

    private fun setupBackEvent(view: View) {
        view.apply {
            league_odd_arrow.setOnClickListener {
                val action = Game2FragmentDirections.actionGame2FragmentToGameFragment(args.matchType)
                navController.navigate(action)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @return A new instance of fragment Game2Fragment.
         */
        @JvmStatic
        fun newInstance() {
        }
    }
}
