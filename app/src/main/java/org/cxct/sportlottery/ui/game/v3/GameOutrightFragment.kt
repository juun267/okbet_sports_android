package org.cxct.sportlottery.ui.game.v3

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_league.view.*
import kotlinx.android.synthetic.main.fragment_game_outright.*
import kotlinx.android.synthetic.main.fragment_game_outright.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel


private const val ARG_SPORT_TYPE = "sportType"
private const val ARG_EVENT_ID = "eventId"

class GameOutrightFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    companion object {
        fun newInstance(sportType: String, eventId: String) =
            GameOutrightFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SPORT_TYPE, sportType)
                    putString(ARG_EVENT_ID, eventId)
                }
            }
    }

    private var sportType: String? = null

    private var eventId: String? = null

    private val outrightOddAdapter by lazy {
        OutrightOddAdapter().apply {
            outrightOddListener = OutrightOddListener {
                viewModel.updateOutrightOddsSelectedState(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            sportType = it.getString(ARG_SPORT_TYPE)
            eventId = it.getString(ARG_EVENT_ID)
        }

        service.subscribeHallChannel(sportType, CateMenuCode.OUTRIGHT.code, eventId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_outright, container, false).apply {
            setupGameFilterRow(this)
            setupOutrightOddList(this)
        }
    }

    private fun setupGameFilterRow(view: View) {
        view.outright_filter_row.apply {

            isSearchViewVisible = false

            backClickListener = View.OnClickListener {
                backEvent()
            }
        }
    }

    private fun setupOutrightOddList(view: View) {
        view.outright_league_odd_list.apply {

            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            adapter = outrightOddAdapter

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
        try {

            viewModel.outrightOddsListResult.observe(this.viewLifecycleOwner, Observer {

                it.getContentIfNotHandled()?.let { outrightOddsListResult ->
                    if (outrightOddsListResult.success) {
                        val matchOdd =
                            outrightOddsListResult.outrightOddsListData?.leagueOdds?.get(0)?.matchOdds?.get(
                                0
                            )

                        outright_filter_row.sportName =
                            outrightOddsListResult.outrightOddsListData?.sport?.name ?: ""

                        outright_league_name.text =
                            outrightOddsListResult.outrightOddsListData?.leagueOdds?.get(0)?.league?.name
                                ?: ""

                        outright_league_date.text = matchOdd?.startDate ?: ""

                        outright_league_time.text = matchOdd?.startTime ?: ""

                        outrightOddAdapter.data = matchOdd?.displayList ?: listOf()
                    }
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun backEvent() {
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

    override fun onResume() {
        super.onResume()

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

        service.unSubscribeHallChannel(sportType, CateMenuCode.OUTRIGHT.code, eventId)
    }
}