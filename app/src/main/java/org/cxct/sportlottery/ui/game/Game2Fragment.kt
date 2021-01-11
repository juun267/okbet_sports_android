package org.cxct.sportlottery.ui.game

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.fragment_game2.*
import kotlinx.android.synthetic.main.itemview_league_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.odds.MatchOddAdapter
import org.cxct.sportlottery.ui.game.odds.MatchOddListener
import org.cxct.sportlottery.ui.home.MainViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [Game2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Game2Fragment : BaseFragment<MainViewModel>(MainViewModel::class) {

    private val playType: PlayType by lazy { PlayType.OU_HDP }

    private val matchOddAdapter by lazy {
        MatchOddAdapter().apply {
            matchOddListener = MatchOddListener {
                viewModel.getOddsDetail(it.matchInfo.id)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game2, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (league_odd_sub_list.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = viewModel.oddsListResult.value
        data?.oddsListData?.leagueOdds?.getOrNull(0)?.let { leagueOdd ->
            this.view?.let { view ->
                setupMatchOddList(view, leagueOdd)
                setupLeagueLayout(view, data.oddsListData.leagueOdds[0])
                setupEvent(view)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                backEvent()
                return@OnKeyListener true
            }
            false
        })
    }

    private fun setupMatchOddList(view: View, item: LeagueOdd) {
        view.league_odd_sub_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = matchOddAdapter
        }
        matchOddAdapter.data = item.matchOdds.apply { this[0].isExpand = true }
        matchOddAdapter.playType = playType
    }

    private fun setupLeagueLayout(view: View, item: LeagueOdd) {
        view.apply {
            league_odd_name.text = item.league.name
            league_odd_count.text = item.matchOdds.size.toString()
        }
    }

    private fun setupEvent(view: View) {
        view.league_odd_arrow.setOnClickListener {
            backEvent()
        }
    }

    private fun backEvent() {
        //比照h5特別處理退出動畫
        val animation: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.exit_to_right)
        animation.duration = resources.getInteger(R.integer.config_navAnimTime).toLong()
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                parentFragmentManager.popBackStack()
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        this.view?.startAnimation(animation)
    }
}
