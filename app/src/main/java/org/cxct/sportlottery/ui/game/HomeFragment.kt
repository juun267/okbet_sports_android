package org.cxct.sportlottery.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.home_game_table.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.ui.base.BaseFragment


class HomeFragment : BaseFragment<GameViewModel>(GameViewModel::class) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false).apply {
            setupGameTypeCard(this)
        }
    }

    private fun setupGameTypeCard(view: View) {
        view.card_game_soon.setOnClickListener {
            viewModel.selectHomeCard(MatchType.AT_START, null)
        }

        view.card_football.setOnClickListener {
            viewModel.selectHomeCard(MatchType.PARLAY, SportType.FOOTBALL)
        }

        view.card_basketball.setOnClickListener {
            viewModel.selectHomeCard(MatchType.PARLAY, SportType.BASKETBALL)
        }

        view.card_tennis.setOnClickListener {
            viewModel.selectHomeCard(MatchType.PARLAY, SportType.TENNIS)
        }

        view.card_badminton.setOnClickListener {
            viewModel.selectHomeCard(MatchType.PARLAY, SportType.BADMINTON)
        }

        view.card_volleyball.setOnClickListener {
            viewModel.selectHomeCard(MatchType.PARLAY, SportType.VOLLEYBALL)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
    }

    private fun initObserver() {
        viewModel.countAtStart.observe(viewLifecycleOwner, Observer {
            hideLoading()
            card_game_soon.tv_count.text = it.toString()
        })
        viewModel.countParlayFootball.observe(viewLifecycleOwner, Observer {
            hideLoading()
            tv_football_count.text = it.toString()
        })
        viewModel.countParlayBasketball.observe(viewLifecycleOwner, Observer {
            hideLoading()
            tv_basketball_count.text = it.toString()
        })
        viewModel.countParlayTennis.observe(viewLifecycleOwner, Observer {
            hideLoading()
            card_tennis.tv_count.text = it.toString()
        })
        viewModel.countParlayBadminton.observe(viewLifecycleOwner, Observer {
            hideLoading()
            card_badminton.tv_count.text = it.toString()
        })
        viewModel.countParlayVolleyball.observe(viewLifecycleOwner, Observer {
            hideLoading()
            card_volleyball.tv_count.text = it.toString()
        })
    }
}