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
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.hall.adapter.CountryAdapter


class LeagueFilterFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: LeagueFilterFragmentArgs by navArgs()

    private val countryAdapter by lazy {
        CountryAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_league_filter, container, false).apply {
            this.league_filter_country_list.apply {
                layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = countryAdapter
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
        loading()
    }

    private fun setupToolbar() {
        game_toolbar_match_type.text = getString(args.matchType.resId)

        game_toolbar_sport_type.text = getString(args.gameType.string)

        game_toolbar_back.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initObserver() {
        viewModel.leagueListResult.observe(this.viewLifecycleOwner, {
            hideLoading()

            it?.getContentIfNotHandled()?.let { leagueListResult ->
                if (leagueListResult.success) {
                    countryAdapter.data = leagueListResult.rows ?: listOf()
                }
            }
        })
    }
}