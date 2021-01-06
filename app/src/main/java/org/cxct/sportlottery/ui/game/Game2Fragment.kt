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
import kotlinx.android.synthetic.main.fragment_game.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.odds.LeagueOddAdapter
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.TimeUtil

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

    private val leagueOddAdapter by lazy {
        LeagueOddAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game2, container, false).apply {
            setupOddsList(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getLeagueOddsList()

        viewModel.oddsListResult.observe(this.viewLifecycleOwner, Observer {
            if (it.success) {
                leagueOddAdapter.data = it.oddsListData?.leagueOdds ?: listOf()
            }
        })
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

    private fun getLeagueOddsList() {
        viewModel.getLeagueOddsList(args.matchType, args.oddsListId, TimeUtil.getTodayTimeRangeParams())
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
