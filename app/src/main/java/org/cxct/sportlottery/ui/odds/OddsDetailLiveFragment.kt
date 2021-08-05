package org.cxct.sportlottery.ui.odds


import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_odds_detail.*
import kotlinx.android.synthetic.main.fragment_odds_detail_live.*
import kotlinx.android.synthetic.main.fragment_odds_detail_live.live_view_tool_bar
import kotlinx.android.synthetic.main.fragment_odds_detail_live.rv_detail
import kotlinx.android.synthetic.main.fragment_odds_detail_live.tab_cat
import kotlinx.android.synthetic.main.view_odds_detail_toolbar.*
import kotlinx.android.synthetic.main.view_toolbar_live.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailLiveBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.producer_up.ProducerUpEvent
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import java.util.*


@Suppress("DEPRECATION", "SetTextI18n")
class OddsDetailLiveFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class),
    OnOddClickListener,
    BaseSocketActivity.ReceiverChannelEvent, BaseSocketActivity.ReceiverChannelPublic,
    BaseSocketActivity.ReceiverChannelMatch {

    private val args: OddsDetailLiveFragmentArgs by navArgs()

    private var oddsDetailListAdapter: OddsDetailListAdapter? = null

    private var mSportCode: String? = null
    private var matchId: String? = null
    private var matchOdd: MatchOdd? = null

    private var curHomeScore: Int? = null
    private var curAwayScore: Int? = null

    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSportCode = args.gameType.key
        matchId = args.matchId

        registerChannelEvent(this)
        registerChannelMatch(this)
        registerChannelPublic(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentOddsDetailLiveBinding.inflate(inflater, container, false).apply {
        fragment = this@OddsDetailLiveFragment
        gameViewModel = this@OddsDetailLiveFragment.viewModel
        lifecycleOwner = this@OddsDetailLiveFragment.viewLifecycleOwner
        executePendingBindings()
    }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        observeData()
    }

    override fun onStart() {
        super.onStart()

        getData()

        live_view_tool_bar.setWebViewUrl(matchId)
    }

    override fun onStop() {
        super.onStop()

        unSubscribeChannelEventAll()

        timer?.cancel()
    }

    private fun initUI() {
        oddsDetailListAdapter = OddsDetailListAdapter(this@OddsDetailLiveFragment).apply {
            sportCode = mSportCode
        }

        rv_detail.apply {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            adapter = oddsDetailListAdapter
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        }

        tab_cat.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { t ->
                    viewModel.playCateListResult.value?.peekContent()?.rows?.get(t)?.code?.let {
                        oddsDetailListAdapter?.notifyDataSetChangedByCode(it)
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun observeData() {
        viewModel.playCateListResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                result.success.let { success ->
                    if (success) {
                        tab_cat.removeAllTabs()
                        if (result.rows.isNotEmpty()) {
                            for (row in result.rows) {
                                val customTabView =
                                    layoutInflater.inflate(R.layout.tab_odds_detail, null).apply {
                                        findViewById<TextView>(R.id.tv_tab).text = row.name
                                    }

                                tab_cat.addTab(
                                    tab_cat.newTab().setCustomView(customTabView),
                                    false
                                )
                            }
                            tab_cat.getTabAt(0)?.select()
                        } else {
                            tab_cat.visibility = View.GONE
                        }
                    }
                }
            }
        })

        viewModel.oddsDetailResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                when (result.success) {
                    true -> {
                        matchOdd = result.oddsDetailData?.matchOdd
                        result.oddsDetailData?.matchOdd?.matchInfo?.homeName?.let { home ->
                            result.oddsDetailData.matchOdd.matchInfo.awayName.let { away ->
                                oddsDetailListAdapter?.homeName = home
                                oddsDetailListAdapter?.awayName = away
                            }
                        }
                        setupStartTime(matchOdd?.matchInfo)
                    }
                    false -> {
                        showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                    }
                }
            }
        })

        viewModel.oddsDetailList.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { list ->
                if (list.isNotEmpty()) {
                    oddsDetailListAdapter?.oddsDetailDataList = list
                }
            }
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, {
            it.peekContent().let { list ->
                oddsDetailListAdapter?.betInfoList = list
            }
        })

        viewModel.betInfoResult.observe(this.viewLifecycleOwner, {
            val eventResult = it.getContentIfNotHandled()
            eventResult?.success?.let { success ->
                if (!success && eventResult.code != HttpError.BET_INFO_CLOSE.code) {
                    showErrorPromptDialog(getString(R.string.prompt), eventResult.msg) {}
                }
            }
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            oddsDetailListAdapter?.oddsType = it
        })
    }

    private fun setupStartTime(matchInfo: MatchInfo?) {
        matchInfo?.apply {
            tv_home_name.text = homeName
            tv_away_name.text = awayName
            tv_time_bottom.text = TimeUtil.timeFormat(startTime, HM_FORMAT)
        }
    }

    private fun getData() {
        mSportCode?.let { mSportCode ->
            matchId?.let { matchId ->
                viewModel.getPlayCateListAndOddsDetail(mSportCode, matchId)
                subscribeChannelEvent(matchId)
            }
        }
    }

    override fun getBetInfoList(odd: Odd, oddsDetail: OddsDetailListData) {
        matchOdd?.let { matchOdd ->
            matchOdd.matchInfo.homeScore = curHomeScore
            matchOdd.matchInfo.awayScore = curAwayScore

            viewModel.updateMatchBetList(
                matchType = MatchType.IN_PLAY,
                gameType = args.gameType,
                playCateName = oddsDetail.name,
                playName = odd.name ?: "",
                matchInfo = matchOdd.matchInfo,
                odd = odd
            )
        }
    }

    override fun removeBetInfoItem(odd: Odd) {
        viewModel.removeBetInfoItem(odd.id)
    }

    override fun onMatchStatusChanged(matchStatusChangeEvent: MatchStatusChangeEvent) {
        matchStatusChangeEvent.matchStatusCO?.takeIf { ms -> ms.matchId == this.matchId }?.apply {
            tv_time_top?.let {
                it.text = this.statusName
            }

            curHomeScore = homeScore
            curAwayScore = awayScore
        }
    }

    override fun onMatchClockChanged(matchClockEvent: MatchClockEvent) {
        val updateTime = when (args.gameType) {
            GameType.FT -> {
                matchClockEvent.matchClockCO?.matchTime
            }
            GameType.BK -> {
                matchClockEvent.matchClockCO?.remainingTimeInPeriod
            }
            else -> null
        }

        updateTime?.let {
            startMatchTimer(updateTime)
        }
    }

    override fun onMatchOddsChanged(matchOddsChangeEvent: MatchOddsChangeEvent) {
        viewModel.updateOddForOddsDetail(matchOddsChangeEvent)
    }

    override fun onGlobalStop(globalStopEvent: GlobalStopEvent) {
    }

    override fun onProducerUp(producerUpEvent: ProducerUpEvent) {
        unSubscribeChannelEventAll()
        subscribeChannelEvent(matchId)
    }

    private fun startMatchTimer(startTime: Int) {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                var timeMillis = startTime * 1000L

                when (args.gameType) {
                    GameType.FT -> {
                        timeMillis += 1000
                    }
                    GameType.BK -> {
                        timeMillis -= 1000
                    }
                }

                if (timeMillis > 0) {
                    Handler(Looper.getMainLooper()).post {

                        tv_time_bottom?.apply {
                            text = TimeUtil.timeFormat(timeMillis, "mm:ss")
                        }
                    }
                }
            }
        }, 1000L, 1000L)
    }
}