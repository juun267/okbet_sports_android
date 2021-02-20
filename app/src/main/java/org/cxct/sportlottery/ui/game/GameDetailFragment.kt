package org.cxct.sportlottery.ui.game

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.fragment_game_detail.*
import kotlinx.android.synthetic.main.itemview_league_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.odds.MatchOddAdapter
import org.cxct.sportlottery.ui.game.odds.MatchOddListener
import org.cxct.sportlottery.ui.home.MainViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [GameDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val ARG_EVENT_ID = "eventId"
private const val ARG_GAME_TYPE = "gameType"

class GameDetailFragment : BaseSocketFragment<MainViewModel>(MainViewModel::class) {
    //just for service subscribe use
    private var eventId: String? = null
    private var gameType: String? = null

    companion object {
        fun newInstance(eventId: String, gameType: String) =
            GameDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_EVENT_ID, eventId)
                    putString(ARG_GAME_TYPE, gameType)
                }
            }
    }

    private val playType: PlayType by lazy { PlayType.OU_HDP }

    private val matchOddAdapter by lazy {
        MatchOddAdapter().apply {
            matchOddListener = MatchOddListener(
                { matchOdd, matchOddList ->
                    viewModel.getOddsDetail(matchOdd.matchInfo?.id)
                    viewModel.setOddsDetailMoreList(matchOddList)
                }, {
                    viewModel.updateMatchOddExpandDetail(it)
                },
                { matchOdd, oddString, odd ->
                    viewModel.updateMatchBetList(
                        matchOdd,
                        oddString,
                        odd
                    )
                })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            eventId = it.getString(ARG_EVENT_ID)
            gameType = it.getString(ARG_GAME_TYPE)
        }

        service.subscribeHallChannel(gameType, CateMenuCode.HDP_AND_OU.code, eventId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_detail, container, false).apply {
            setupEvent(this)
            setupMatchOddList(this)
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
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = matchOddAdapter.apply {
                this.playType = this@GameDetailFragment.playType
            }
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

        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, Observer {
            matchOddAdapter.betInfoListData = it
        })

        setSocketObserver()
    }

    private fun setSocketObserver() {
        receiver.oddsChange.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            updateMatchOdd(it)
        })

        updateSocketGlobalStop()
        updateSocketProducerUp()
    }

    private fun updateMatchOdd(oddsChangeEvent: OddsChangeEvent) {
        val matchOdds = matchOddAdapter.data
        matchOdds.forEach { matchOdd ->
            matchOdd.odds.forEach { odds ->
                val socketOdds = oddsChangeEvent.odds[odds.key]
                val originOdds = odds.value

                originOdds.forEach { originOdd ->
                    val updateOdd = socketOdds?.find { socketOdd ->
                        originOdd.id == socketOdd.id
                    }

                    val originOddValue = originOdd.odds
                    val updateOddValue = updateOdd?.odds

                    originOddValue?.let {
                        updateOddValue?.let {
                            Timber.i("$originOddValue -> $updateOddValue")

                            //update Odd state
                            when {
                                originOddValue > updateOddValue -> {
                                    originOdd.oddState = OddState.SMALLER.state
                                }
                                originOddValue < updateOddValue -> {
                                    originOdd.oddState = OddState.LARGER.state
                                }
                                originOddValue == updateOddValue -> {
                                    originOdd.oddState = OddState.SAME.state
                                }
                            }

                            //update Odd value
                            originOdd.odds = updateOdd.odds

                            //update Odd status
                            originOdd.status = updateOdd.status
                        }
                    }
                }
            }
        }

        matchOddAdapter.data = matchOdds
    }

    private fun updateSocketGlobalStop() {
        receiver.globalStop.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            when (val stopProducerId = it.producerId) {
                null -> {
                    updateAllOddStatus(BetStatus.LOCKED)
                }
                else -> {
                    updateOddStatus(stopProducerId, BetStatus.LOCKED)
                }
            }
        })
    }

    private fun updateSocketProducerUp() {
        receiver.producerUp.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            when (val upProducerId = it.producerId) {
                null -> {
                    updateAllOddStatus(BetStatus.ACTIVATED)
                }
                else -> {
                    updateOddStatus(upProducerId, BetStatus.ACTIVATED)
                }
            }
        })
    }

    private fun updateAllOddStatus(betStatus: BetStatus) {
        val matchOdds = matchOddAdapter.data

        matchOdds.forEach { matchOdd ->
            matchOdd.odds.values.forEach { odds ->
                odds.forEach {

                    it.status = betStatus.code
                }
            }
        }

        matchOddAdapter.data = matchOdds
    }

    private fun updateOddStatus(stopProducerId: Int, betStatus: BetStatus) {
        val matchOdds = matchOddAdapter.data

        matchOdds.forEach { matchOdd ->
            matchOdd.odds.values.forEach { odds ->
                val updateOdd = odds.find { odd ->
                    odd.producerId == stopProducerId
                }

                updateOdd?.status = betStatus.code
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
            matchOddAdapter.data = it.matchOdds
            matchOddAdapter.betInfoListData = viewModel.betInfoRepository?.betInfoList?.value
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

    override fun onDestroy() {
        super.onDestroy()

        service.unSubscribeHallChannel(gameType, CateMenuCode.HDP_AND_OU.code, eventId)
    }
}
