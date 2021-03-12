package org.cxct.sportlottery.ui.game.outright

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_outright_detail.*
import kotlinx.android.synthetic.main.fragment_outright_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import timber.log.Timber


private const val ARG_EVENT_ID = "eventId"
private const val ARG_GAME_TYPE = "gameType"

class OutrightDetailFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    //just for service subscribe use
    private var eventId: String? = null
    private var gameType: String? = null

    companion object {
        fun newInstance(eventId: String, gameType: String) =
            OutrightDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_EVENT_ID, eventId)
                    putString(ARG_GAME_TYPE, gameType)
                }
            }
    }

    private val outrightOddAdapter by lazy {
        OutrightOddAdapter().apply {
            outrightOddListener = OutrightOddAdapter.OutrightOddListener {
                viewModel.updateOutrightOddsSelectedState(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            eventId = it.getString(ARG_EVENT_ID)
            gameType = it.getString(ARG_GAME_TYPE)
        }

        service.subscribeHallChannel(gameType, CateMenuCode.OUTRIGHT.code, eventId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_outright_detail, container, false).apply {
            setupBack(this)
            setupOutrightOddList(this)
        }
    }

    private fun setupBack(view: View) {
        view.outright_detail_back.setOnClickListener {
            backEvent()
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

    private fun setupOutrightOddList(view: View) {
        view.outright_detail_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = outrightOddAdapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        initSocketObserver()
    }

    private fun initObserver() {
        viewModel.outrightOddsListResult.observe(this.viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { outrightOddsListResult ->
                if (outrightOddsListResult.success) {
                    setupOutrightOddList(outrightOddsListResult)
                }
            }
        })

        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, Observer {
            outrightOddAdapter.betInfoListData = it
        })
    }

    private fun initSocketObserver() {
        updateOddsChange()
        updateSocketGlobalStop()
        updateSocketProducerUp()
    }

    private fun updateOddsChange() {
        receiver.oddsChange.observe(this.viewLifecycleOwner, Observer { oddsChangeEvent ->
            if (oddsChangeEvent == null) return@Observer

            val matchOdd = outrightOddAdapter.matchOdd
            matchOdd?.odds?.forEach { odds ->
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

            outrightOddAdapter.matchOdd = matchOdd
        })
    }

    private fun updateSocketGlobalStop() {
        receiver.globalStop.observe(this.viewLifecycleOwner, Observer { globalStopEvent ->
            if (globalStopEvent == null) return@Observer

            val matchOdd = outrightOddAdapter.matchOdd

            when (globalStopEvent.producerId) {
                null -> {
                    matchOdd?.displayList?.forEach {
                        if (it is Odd) {
                            it.status = BetStatus.LOCKED.code
                        }
                    }
                }
                else -> {
                    val odd = matchOdd?.displayList?.find {
                        it is Odd && it.producerId == globalStopEvent.producerId
                    } as Odd
                    odd.status = BetStatus.LOCKED.code
                }
            }

            outrightOddAdapter.matchOdd = matchOdd
        })
    }

    private fun updateSocketProducerUp() {
        receiver.producerUp.observe(this.viewLifecycleOwner, Observer { producerUpEvent ->
            if (producerUpEvent == null) return@Observer

            val matchOdd = outrightOddAdapter.matchOdd

            when (producerUpEvent.producerId) {
                null -> {
                    matchOdd?.displayList?.forEach {
                        if (it is Odd) {
                            it.status = BetStatus.ACTIVATED.code
                        }
                    }
                }
                else -> {
                    val odd = matchOdd?.displayList?.find {
                        it is Odd && it.producerId == producerUpEvent.producerId
                    } as Odd
                    odd.status = BetStatus.ACTIVATED.code
                }
            }

            outrightOddAdapter.matchOdd = matchOdd
        })
    }

    private fun setupOutrightOddList(outrightOddsListResult: OutrightOddsListResult) {
        val matchOdd =
            outrightOddsListResult.outrightOddsListData?.leagueOdds?.get(0)?.matchOdds?.get(0)

        outright_detail_title.text =
            outrightOddsListResult.outrightOddsListData?.leagueOdds?.get(0)?.league?.name ?: ""

        outright_detail_date.text = matchOdd?.startDate ?: ""
        outright_detail_time.text = matchOdd?.startTime ?: ""

        outrightOddAdapter.matchOdd = matchOdd
    }

    override fun onDestroy() {
        super.onDestroy()

        service.unSubscribeHallChannel(gameType, CateMenuCode.OUTRIGHT.code, eventId)
    }
}