package org.cxct.sportlottery.ui.game

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.fragment_game_detail.*
import kotlinx.android.synthetic.main.fragment_game_detail.view.*
import kotlinx.android.synthetic.main.itemview_league_odd.view.league_odd_arrow
import kotlinx.android.synthetic.main.itemview_league_odd.view.league_odd_sub_list
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.odds.MatchOddAdapter
import org.cxct.sportlottery.ui.game.odds.MatchOddListener
import org.cxct.sportlottery.ui.game.outright.OutrightOddAdapter
import org.cxct.sportlottery.ui.home.MainViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [GameDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameDetailFragment : BaseFragment<MainViewModel>(MainViewModel::class) {

    private val playType: PlayType by lazy { PlayType.OU_HDP }

    private val matchOddAdapter by lazy {
        MatchOddAdapter().apply {
            matchOddListener = MatchOddListener({
                viewModel.getOddsDetail(it.matchInfo?.id)
            }, { matchOdd, oddString, odd -> viewModel.updateMatchBetList(matchOdd, oddString, odd) })
        }
    }

    private val outrightOddAdapter by lazy {
        OutrightOddAdapter().apply {
            outrightOddListener = OutrightOddAdapter.OutrightOddListener {
                viewModel.updateOutrightOddsSelectedState(it)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_game_detail, container, false).apply {
            setupEvent(this)
            setupMatchOddList(this)
            setupOutrightOddList(this)
        }
    }

    private fun setupEvent(view: View) {
        view.league_odd_arrow.setOnClickListener {
            backEvent()
        }
    }

    private fun setupMatchOddList(view: View) {
        view.league_odd_sub_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = matchOddAdapter.apply {
                this.playType = this@GameDetailFragment.playType
            }
        }
    }

    private fun setupOutrightOddList(view: View) {
        view.outright_odd_sub_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = outrightOddAdapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
            //TODO : 重構, view不應該直接去拿repository東西。應該要再view model去操作repository.
            outrightOddAdapter.betInfoListData = viewModel.betInfoRepository?.betInfoList?.value
        }
    }

    private fun backEvent() {
        //比照h5特別處理退出動畫
        val animation: Animation =
            AnimationUtils.loadAnimation(requireActivity(), R.anim.exit_to_right)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (league_odd_sub_list.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.oddsListResult.observe(this.viewLifecycleOwner, Observer {
            if (it != null && it.success) {
                setupOddsUpperBar(it)
                setupMatchOddList(it)
            }
        })

        viewModel.betInfoRepository?.betInfoList?.observe(this.viewLifecycleOwner, Observer {
            matchOddAdapter.betInfoListData = it
            outrightOddAdapter.betInfoListData = it
        })

        viewModel.outrightOddsListResult.observe(this.viewLifecycleOwner, Observer {
            if (it != null && it.success) {
                setupLeagueOddsUpperBar()
                setupOutrightOddList(it)
            }
        })

        setSocketObserver()
    }

    private fun setSocketObserver() {
        /*
        viewModel.oddsChange.observe(this.viewLifecycleOwner, Observer { oddsChangeEvent ->
            if (oddsChangeEvent == null) return@Observer

            updateMatchOdd(oddsChangeEvent)

            outrightOddAdapter.updatedWinnerOddsList = oddsChangeEvent.odds[winnerItemKey] ?: listOf()
        })
*/
        viewModel.oddsChange.observe(this.viewLifecycleOwner, {
            it?.let {
                if (it.odds.isNullOrEmpty()) return@observe
                matchOddAdapter.updatedOddsMap = it.odds
                outrightOddAdapter.updatedWinnerOddsList = it.odds[winnerItemKey] ?: listOf()
            }
        })

        viewModel.matchStatusChange.observe(this.viewLifecycleOwner, {
            Log.e(">>>", "hello world")
        })
    }

    private fun updateMatchOdd(oddsChangeEvent: OddsChangeEvent) {
        val matchOdds = matchOddAdapter.data

        matchOdds.forEach { matchOdd ->
            matchOdd.odds.forEach { oldOdds ->
                val newOdds = oddsChangeEvent.odds[oldOdds.key]

                oldOdds.value.forEach { oldOdd ->
                    val updateOdd = newOdds?.find { newOdd ->
                        newOdd.id == oldOdd.id
                    }

                    updateOdd?.odds?.let { nonNullUpdateOdd ->
                        oldOdd.odds?.let { nonNullOldOdd ->
                            when {
                                (nonNullUpdateOdd > nonNullOldOdd) -> {
                                    oldOdd.oddState = OddState.LARGER.state
                                }

                                (nonNullUpdateOdd == nonNullOldOdd) -> {
                                    oldOdd.oddState = OddState.SAME.state
                                }

                                (nonNullUpdateOdd < nonNullOldOdd) -> {
                                    oldOdd.oddState = OddState.SMALLER.state
                                }
                            }

                            oldOdd.odds = updateOdd.odds
                        }
                    }
                }
            }
        }

        matchOddAdapter.data = matchOdds
    }

    private fun setupOddsUpperBar(oddsListResult: OddsListResult) {
        league_odd_count.visibility = View.VISIBLE
        val oddsFirst = oddsListResult.oddsListData?.leagueOdds?.get(0)

        oddsFirst?.let {
            league_odd_name.text = it.league.name
            league_odd_count.text = it.matchOdds.size.toString()
        }
    }

    private fun setupMatchOddList(oddsListResult: OddsListResult) {
        league_odd_sub_list.visibility = View.VISIBLE
        outright_odd_sub_list.visibility = View.GONE

        val oddsFirst = oddsListResult.oddsListData?.leagueOdds?.get(0)

        oddsFirst?.let {
            matchOddAdapter.data = it.matchOdds.apply { this[0].isExpand = true }
            matchOddAdapter.betInfoListData = viewModel.betInfoRepository?.betInfoList?.value
        }
    }

    private fun setupLeagueOddsUpperBar() {
        league_odd_name.text = getString(R.string.detail_outright_upper_bar_title)
        league_odd_count.visibility = View.GONE
    }

    var winnerItemKey: String = ""

    private fun setupOutrightOddList(outrightOddsListResult: OutrightOddsListResult) {
        league_odd_sub_list.visibility = View.GONE
        outright_odd_sub_list.visibility = View.VISIBLE

        winnerItemKey = outrightOddsListResult.outrightOddsListData?.leagueOdds?.get(0)?.matchOdds?.get(0)?.odds?.keys?.firstOrNull() ?: ""

        val emptyList = listOf(Odd(), Odd()) //default放入兩個空值, 以便socket比對並更新內容
        outrightOddAdapter.data =
            outrightOddsListResult.outrightOddsListData?.leagueOdds?.get(0)?.matchOdds?.get(0)?.odds?.values?.first()
                ?: emptyList
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
}
