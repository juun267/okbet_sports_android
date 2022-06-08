package org.cxct.sportlottery.ui.game.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_league_filter.view.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.hall.adapter.CountryAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.CountryLeagueListener
import java.util.*


class LeagueFilterFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: LeagueFilterFragmentArgs by navArgs()

    private val countryAdapter by lazy {
        CountryAdapter().apply {
            countryLeagueListener = CountryLeagueListener(
                {
                    viewModel.filterLeague(listOf(it))
                    findNavController().navigateUp()
                }, { league ->
                    viewModel.pinFavorite(FavoriteType.LEAGUE, league.id)
                }, { league ->
                    viewModel.selectLeague(league)
                })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_league_filter, container, false).apply {
            this.league_filter_country_list.apply {
                layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = countryAdapter
                countryAdapter.setPreloadItem()
            }
            this.league_filter_all_view.setOnClickListener {
                viewModel.filterLeague(listOf())
                findNavController().navigateUp()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        initObserver()
    }

    override fun onStart() {
        super.onStart()

        viewModel.getLeagueList(args.gameType.key, args.matchType.postValue, null)
//        loading()
    }

    private fun setupToolbar() {

//        根據2022/1/25需求先拔除，確定不要可刪
//        game_toolbar_match_type.text = if (args.matchType == MatchType.AT_START) getString(R.string.home_tab_at_start_2) else getString(args.matchType.resId)

        game_toolbar_sport_type.text = getString(args.gameType.string)

        game_toolbar_back.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initObserver() {
        viewModel.leagueListResult.observe(this.viewLifecycleOwner) {
            hideLoading()

            it?.getContentIfNotHandled()?.let { leagueListResult ->
                if (leagueListResult.success) {
                    countryAdapter.data = leagueListResult.rows ?: listOf()
                }
            }
        }

        viewModel.favorLeagueList.observe(this.viewLifecycleOwner) {
            val leaguePinList = mutableListOf<League>()

            countryAdapter.data.forEach { row ->
                val pinLeague = row.list.filter { league ->
                    it.contains(league.id)
                }

                row.list.forEach { league ->
                    league.isPin = it.contains(league.id)
                }

                leaguePinList.addAll(pinLeague)
            }

            countryAdapter.datePin = leaguePinList.sortedBy { league ->
                it.indexOf(league.id)
            }
        }

        viewModel.leagueSelectedList.observe(this.viewLifecycleOwner) {
            countryAdapter.apply {
                data.forEach { row ->
                    row.list.forEach { league ->
                        league.isSelected = it.any { it.id == league.id }
                    }
                }

                notifyDataSetChanged()
            }
        }

        viewModel.leagueSubmitList.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { leagueList ->
                viewModel.filterLeague(leagueList)
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearSelectedLeague()
    }
}