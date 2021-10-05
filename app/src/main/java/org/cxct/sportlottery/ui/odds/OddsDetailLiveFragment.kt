package org.cxct.sportlottery.ui.odds


import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_odds_detail_live.*
import kotlinx.android.synthetic.main.view_odds_detail_toolbar.*
import kotlinx.android.synthetic.main.view_toolbar_live.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailLiveBinding
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.repository.FLAG_LIVE
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.common.TimerManager
import org.cxct.sportlottery.ui.component.LiveViewToolbar
import org.cxct.sportlottery.ui.component.NodeMediaManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.LanguageManager.getSelectLanguage
import org.cxct.sportlottery.util.TimeUtil.DM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import java.util.*


@Suppress("DEPRECATION", "SetTextI18n")
class OddsDetailLiveFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class),
    OnOddClickListener, TimerManager {

    private val args: OddsDetailLiveFragmentArgs by navArgs()

    private var oddsDetailListAdapter: OddsDetailListAdapter? = null

    private var matchId: String? = null
    private var matchOdd: MatchOdd? = null

    private var curHomeScore: Int? = null
    private var curAwayScore: Int? = null
    
    private var curStatus: Int? = null

    override var startTime: Long = 0
    override var timer: Timer = Timer()
    override var timerHandler: Handler = Handler {
        var timeMillis = startTime * 1000L

        when (args.gameType) {
            GameType.FT -> {
                timeMillis += 1000
            }
            GameType.BK -> {
                timeMillis -= 1000
            }
            else -> {
            }
        }

        if (needCountStatus(curStatus)) {
            if (timeMillis >= 0) {
                tv_time_bottom?.apply {
                    text = TimeUtil.timeFormat(timeMillis, "mm:ss")
                }
                startTime = timeMillis / 1000L
            }
        } else {
            tv_time_bottom?.apply {
                text = this.context.getString(R.string.time_null)
            }
        }

        return@Handler false
    }

    private val liveToolBarListener by lazy {
        object : LiveViewToolbar.LiveToolBarListener {
            override fun onExpand() {
                matchId?.let {
                    viewModel.getLiveInfo(it)
                }
            }
        }
    }

    private val eventListener by lazy {
        object : NodeMediaManager.LiveEventListener {
            override fun reRequestStreamUrl() {
                matchId?.let { viewModel.getLiveInfo(it, true) }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        matchId = args.matchId
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
        initSocketObserver()
    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    override fun onResume() {
        super.onResume()
        live_view_tool_bar.startNodeMediaPlayer()
        startTimer()
    }

    override fun onPause() {
        super.onPause()

        cancelTimer()
    }

    override fun onStop() {
        live_view_tool_bar.stopNodeMediaPlayer()
        super.onStop()

        unSubscribeChannelEventAll()
    }

    override fun onDestroyView() {
        live_view_tool_bar.releaseNodeMediaPlayer()
        super.onDestroyView()
    }

    private fun initUI() {
        oddsDetailListAdapter = OddsDetailListAdapter(this@OddsDetailLiveFragment).apply {
            oddsDetailListener = OddsDetailListener {
                viewModel.pinFavorite(FavoriteType.PLAY_CATE, it, args.gameType.key)
            }

            sportCode = args.gameType
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
                    viewModel.oddsDetailResult.value?.peekContent()?.oddsDetailData?.matchOdd?.playCateTypeList?.getOrNull(t)?.code?.let {
                        oddsDetailListAdapter?.notifyDataSetChangedByCode(it)
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun observeData() {
        viewModel.oddsDetailResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                when (result.success) {
                    true -> {
                        result.setupPlayCateTab()

                        matchOdd = result.oddsDetailData?.matchOdd
                        result.oddsDetailData?.matchOdd?.matchInfo?.homeName?.let { home ->
                            result.oddsDetailData.matchOdd.matchInfo.awayName.let { away ->
                                oddsDetailListAdapter?.homeName = home
                                oddsDetailListAdapter?.awayName = away
                            }
                        }
                        setupStartTime()
                        setupLiveView(result.oddsDetailData?.matchOdd?.matchInfo?.liveVideo)


                        if (args.matchType == MatchType.IN_PLAY) {
                            tv_time_bottom?.setTextColor(ContextCompat.getColor(tv_time_bottom.context, R.color.colorOrangeLight))
                            tv_time_top?.setTextColor(ContextCompat.getColor(tv_time_bottom.context, R.color.colorOrangeLight))
                        } else {
                            tv_time_bottom?.setTextColor(ContextCompat.getColor(tv_time_bottom.context, R.color.colorSilver))
                            tv_time_top?.setTextColor(ContextCompat.getColor(tv_time_bottom.context, R.color.colorSilver))
                        }

                        if (args.matchType == MatchType.IN_PLAY &&
                            (args.gameType == GameType.BK || args.gameType == GameType.TN || args.gameType == GameType.VB)
                            && tv_status_left.isVisible) {
                            tv_spt.visibility = View.VISIBLE
                            tv_spt.text = " / ${(it.peekContent()?.oddsDetailData?.matchOdd?.matchInfo?.spt)?:0}"
                        }

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

        viewModel.favorPlayCateList.observe(this.viewLifecycleOwner, {
            oddsDetailListAdapter?.let { oddsDetailListAdapter ->
                val playCate = it.find { playCate ->
                    playCate.gameType == args.gameType.key
                }

                val playCateCodeList = playCate?.code?.let { it1 ->
                    if (it1.isNotEmpty()) {
                        TextUtil.split(it1)
                    } else {
                        mutableListOf()
                    }
                }

                val pinList = oddsDetailListAdapter.oddsDetailDataList.filter {
                    playCateCodeList?.contains(it.gameType) ?: false
                }.sortedByDescending { oddsDetailListData ->
                    playCateCodeList?.indexOf(oddsDetailListData.gameType)
                }

                val epsSize = oddsDetailListAdapter.oddsDetailDataList.groupBy {
                    it.gameType == PlayCate.EPS.value
                }[true]?.size ?: 0

                oddsDetailListAdapter.oddsDetailDataList.sortBy { it.originPosition }
                oddsDetailListAdapter.oddsDetailDataList.forEach {
                    it.isPin = false
                }

                pinList.forEach {
                    it.isPin = true

                    oddsDetailListAdapter.oddsDetailDataList.add(
                        epsSize,
                        oddsDetailListAdapter.oddsDetailDataList.removeAt(
                            oddsDetailListAdapter.oddsDetailDataList.indexOf(
                                it
                            )
                        )
                    )
                }

                oddsDetailListAdapter.notifyDataSetChanged()
            }
        })

        viewModel.matchLiveInfo.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { liveStreamInfo ->
                live_view_tool_bar.setupLiveUrl(liveStreamInfo.streamUrl)
            }
        })
    }

    private fun initSocketObserver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, {
            it?.let { matchStatusChangeEvent ->
                matchStatusChangeEvent.matchStatusCO?.takeIf { ms -> ms.matchId == this.matchId }
                    ?.apply {
                        tv_time_top?.let { tv ->
                            tv.text = when (getSelectLanguage(context)) {
                                LanguageManager.Language.ZH -> statusNameI18n?.zh
                                LanguageManager.Language.EN -> statusNameI18n?.en
                                else -> statusName
                            }
                        }

                        curHomeScore = homeScore
                        curAwayScore = awayScore
                        curStatus = status

                        setupStatusList(matchStatusChangeEvent)
                    }
            }
        })

        receiver.matchClock.observe(this.viewLifecycleOwner, {
            it?.let { matchClockEvent ->
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
                    startTime = updateTime.toLong()
                }
            }
        })

        receiver.matchOddsChange.observe(this.viewLifecycleOwner, {
            it?.let { matchOddsChangeEvent ->
                matchOddsChangeEvent.updateOddsSelectedState()

                oddsDetailListAdapter?.oddsDetailDataList?.forEachIndexed { index, oddsDetailListData ->
                    if (SocketUpdateUtil.updateMatchOdds(
                            oddsDetailListData,
                            matchOddsChangeEvent
                        )
                        && oddsDetailListData.isExpand
                    ) {
                        oddsDetailListAdapter?.notifyItemChanged(index)
                    }
                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->
                oddsDetailListAdapter?.oddsDetailDataList?.forEachIndexed { index, oddsDetailListData ->
                    if (SocketUpdateUtil.updateOddStatus(
                            oddsDetailListData,
                            globalStopEvent
                        ) && oddsDetailListData.isExpand
                    ) {
                        oddsDetailListAdapter?.notifyItemChanged(index)
                    }
                }
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            it?.let {
                unSubscribeChannelEventAll()
                subscribeChannelEvent(matchId)
            }
        })
    }

    private fun MatchOddsChangeEvent.updateOddsSelectedState(): MatchOddsChangeEvent {
        this.odds?.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value.odds?.onEach { odd ->
                    odd?.isSelected =
                        viewModel.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                }
            }
        }

        return this
    }

    private fun setupStartTime() {

        matchOdd?.matchInfo?.apply {

            tv_home_name.text = this.homeName ?: ""

            tv_away_name.text = this.awayName ?: ""

            tv_time_bottom.text = TimeUtil.timeFormat(startTime, HM_FORMAT)

            if (args.matchType != MatchType.IN_PLAY) {
                tv_time_top.text = TimeUtil.timeFormat(startTime, DM_FORMAT)
            }
        }

    }

    private fun setupLiveView(liveVideo: Int?) {
        live_view_tool_bar.setupToolBarListener(liveToolBarListener)
        live_view_tool_bar.setupNodeMediaPlayer(eventListener)
        live_view_tool_bar.setupPlayerControl(liveVideo.toString() == FLAG_LIVE)

        matchOdd?.let {
            live_view_tool_bar.matchOdd = it
        }
    }

    private fun getData() {
        matchId?.let { matchId ->
            viewModel.getOddsDetail(matchId)
            subscribeChannelEvent(matchId)
        }
    }

    private fun OddsDetailResult.setupPlayCateTab(){
            tab_cat.removeAllTabs()
        val playCateTypeList = this.oddsDetailData?.matchOdd?.playCateTypeList
            if (playCateTypeList?.isNotEmpty() == true) {
                for (row in playCateTypeList) {
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

    override fun getBetInfoList(odd: Odd, oddsDetail: OddsDetailListData) {
        matchOdd?.let { matchOdd ->
            matchOdd.matchInfo.homeScore = curHomeScore
            matchOdd.matchInfo.awayScore = curAwayScore

            viewModel.updateMatchBetList(
                matchType = MatchType.IN_PLAY,
                gameType = args.gameType,
                playCateName = oddsDetail.name,
                matchInfo = matchOdd.matchInfo,
                odd = odd,
                subscribeChannelType = ChannelType.EVENT
            )
        }
    }

    override fun removeBetInfoItem(odd: Odd) {
        viewModel.removeBetInfoItem(odd.id)
    }

    private fun setupFrontScore(event: MatchStatusChangeEvent) {
        tv_home_score.visibility = View.VISIBLE
        tv_home_score.text = (event.matchStatusCO?.homeTotalScore ?: 0).toString()

        tv_away_score.visibility = View.VISIBLE
        tv_away_score.text = (event.matchStatusCO?.awayTotalScore ?: 0).toString()
    }

    private fun setupBackScore(event: MatchStatusChangeEvent) {
        tv_home_score_total.visibility = View.VISIBLE
        tv_home_score_total.text = (event.matchStatusCO?.homeTotalScore ?: 0).toString()

        tv_away_score_total.visibility = View.VISIBLE
        tv_away_score_total.text = (event.matchStatusCO?.awayTotalScore ?: 0).toString()

        tv_home_score_live.visibility = View.VISIBLE
        tv_home_score_live.text = (event.matchStatusCO?.homeScore ?: 0).toString()

        tv_away_score_live.visibility = View.VISIBLE
        tv_away_score_live.text = (event.matchStatusCO?.awayScore ?: 0).toString()

        ll_time.visibility = View.GONE
    }

    private fun setupStatusList(event: MatchStatusChangeEvent) {
        if (args.matchType != MatchType.IN_PLAY) return

        when (args.gameType) {
            GameType.FT -> {
                setCardText(event)
                setupFrontScore(event)
            }
            GameType.BK -> {
                setupFrontScore(event)
                setupStatusBk(event)
            }
            GameType.TN -> {
                setupPoint(event)
                setupBackScore(event)
                setupStatusTnVB(event)
            }
            GameType.VB -> {
                setupBackScore(event)
                setupStatusTnVB(event)
            }
        }
    }

    private fun setCardText(event: MatchStatusChangeEvent) {
        tv_home_card.isVisible = (event.matchStatusCO?.homeCards ?:0 > 0)
        tv_home_card.text = (event.matchStatusCO?.homeCards ?: 0).toString()

        tv_away_card.isVisible = (event.matchStatusCO?.awayCards ?: 0 > 0)
        tv_away_card.text = (event.matchStatusCO?.awayCards ?: 0).toString()
    }

    private fun setupPoint(event: MatchStatusChangeEvent) {
        tv_home_point_live.visibility = View.VISIBLE
        tv_home_point_live.text = (event.matchStatusCO?.homePoints ?: 0).toString()

        tv_away_point_live.visibility = View.VISIBLE
        tv_away_point_live.text = (event.matchStatusCO?.awayPoints ?: 0).toString()
    }

    private fun setupStatusBk(event: MatchStatusChangeEvent) {
        if (event.matchStatusList?.isEmpty() == true) return

        val statusBuilder = SpannableStringBuilder()

        tv_status_left.visibility = View.VISIBLE

        event.matchStatusList?.forEachIndexed { index, it ->
            val spanStatusName = SpannableString(it.statusNameI18n?.get(getSelectLanguage(context).key))
            val spanScore = SpannableString("${it.homeScore ?: 0}-${it.awayScore ?: 0}  ")

            if (index == event.matchStatusList.lastIndex) {
                spanStatusName.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    spanStatusName.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spanScore.setSpan(StyleSpan(Typeface.BOLD), 0, spanScore.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            statusBuilder.append(spanStatusName).append(spanScore)
        }

        tv_status_left.text = statusBuilder
    }

    private fun setupStatusTnVB(event: MatchStatusChangeEvent) {
        if (event.matchStatusList?.isEmpty() == true) return

        val statusBuilder = SpannableStringBuilder()

        tv_status_left.visibility = View.VISIBLE
        tv_status_right.visibility = View.VISIBLE

        event.matchStatusList?.forEachIndexed { index, it ->
            if (index != event.matchStatusList.lastIndex) {
                val spanScore = SpannableString("${it.homeScore ?: 0}-${it.awayScore ?: 0}")
                statusBuilder.append(spanScore)
            }

            if (index < event.matchStatusList.lastIndex - 1) {
                statusBuilder.append("  ")
            }
        }

        tv_status_right.text = statusBuilder

        tv_status_left.text = when (getSelectLanguage(context)) {
            LanguageManager.Language.ZH -> "${event.matchStatusCO?.statusNameI18n?.zh}"
            LanguageManager.Language.EN -> event.matchStatusCO?.statusNameI18n?.en
            else -> event.matchStatusCO?.statusName
        }
    }
}