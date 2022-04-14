package org.cxct.sportlottery.ui.odds


import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.exoplayer2.util.Util
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_odds_detail_live.*
import kotlinx.android.synthetic.main.view_odds_detail_toolbar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailLiveBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.repository.FLAG_LIVE
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.common.TimerManager
import org.cxct.sportlottery.ui.component.LiveViewToolbar
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.LanguageManager.getSelectLanguage
import org.cxct.sportlottery.util.TimeUtil.DM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import java.util.*


@Suppress("DEPRECATION", "SetTextI18n")
class OddsDetailLiveFragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class), TimerManager, Animation.AnimationListener {

    private val args: OddsDetailLiveFragmentArgs by navArgs()

    private var oddsDetailListAdapter: OddsDetailListAdapter? = null

    private val tabCateAdapter: TabCateAdapter by lazy {
        TabCateAdapter(OnItemSelectedListener {
            tabCateAdapter.selectedPosition = it
            viewModel.oddsDetailResult.value?.peekContent()?.oddsDetailData?.matchOdd?.playCateTypeList?.getOrNull(
                it
            )?.code?.let { code ->
                oddsDetailListAdapter?.notifyDataSetChangedByCode(code)
            }
        })
    }

    private var matchId: String? = null
    private var matchOdd: MatchOdd? = null

    private var curHomeScore: Int? = null
    private var curAwayScore: Int? = null

    private var curStatus: Int? = null

    private var isGamePause = false
    override var startTime: Long = 0
    override var timer: Timer = Timer()
    override var timerHandler: Handler = Handler {
        var timeMillis = startTime * 1000L

        if (!isGamePause) {
            when (args.gameType) {
                GameType.FT -> {
                    timeMillis += 1000
                }
                GameType.BK, GameType.RB, GameType.AFT -> {
                    timeMillis -= 1000
                }
                else -> {
                }
            }

        }
        tv_time_bottom?.apply {
            if (needCountStatus(curStatus)) {
                if (timeMillis >= 1000) {
                    text = TimeUtil.longToMmSs(timeMillis)
                    startTime = timeMillis / 1000L
                } else {
                    text = this.context.getString(R.string.time_null)
                }
            } else {
                text = this.context.getString(R.string.time_null)
            }
        }
        return@Handler false
    }

    private val liveToolBarListener by lazy {
        object : LiveViewToolbar.LiveToolBarListener {
            override fun getLiveInfo(newestUrl: Boolean) {
                matchId?.let {
                    viewModel.getLiveInfo(it, newestUrl)
                }
            }

            override fun showStatistics() {
                StatisticsDialog.newInstance(matchId).show(childFragmentManager, StatisticsDialog::class.java.simpleName)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBottomNavigation()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    override fun onStart() {
        super.onStart()

        if (Util.SDK_INT >= 24) {
            live_view_tool_bar.startPlayer(matchId, matchOdd?.matchInfo?.trackerId, null)
        }
    }

    override fun onResume() {
        super.onResume()
        startTimer()

        if ((Util.SDK_INT < 24) || live_view_tool_bar.getExoPlayer() == null) {
            live_view_tool_bar.startPlayer(matchId, matchOdd?.matchInfo?.trackerId, null)
        }
    }

    override fun onPause() {
        if (Util.SDK_INT < 24) {
            live_view_tool_bar.stopPlayer()
        }
        super.onPause()

        cancelTimer()
    }

    override fun onStop() {
        if (Util.SDK_INT >= 24) {
            live_view_tool_bar.stopPlayer()
        }
        super.onStop()

        unSubscribeChannelEventAll()
    }

    override fun onDestroy() {
        viewModel.clearLiveInfo()
        super.onDestroy()
    }

    private fun initUI() {
//        live_view_tool_bar.gameType = args.gameType //賽事動畫icon用，之後用不到可刪
        oddsDetailListAdapter = OddsDetailListAdapter(
            OnOddClickListener { odd, oddsDetail, scoPlayCateNameForBetInfo ->
                matchOdd?.let { matchOdd ->
                    matchOdd.matchInfo.homeScore = "$curHomeScore"
                    matchOdd.matchInfo.awayScore = "$curAwayScore"

                    val fastBetDataBean = FastBetDataBean(
                        matchType = MatchType.IN_PLAY,
                        gameType = args.gameType,
                        playCateCode = oddsDetail?.gameType ?: "",
                        playCateName = oddsDetail?.name ?: "",
                        matchInfo = matchOdd.matchInfo,
                        matchOdd = null,
                        odd = odd,
                        subscribeChannelType = ChannelType.EVENT,
                        betPlayCateNameMap = matchOdd.betPlayCateNameMap,
                        otherPlayCateName = scoPlayCateNameForBetInfo
                    )
                    (activity as GameActivity).showFastBetFragment(fastBetDataBean)

//                    viewModel.updateMatchBetList(
//                        matchType = MatchType.IN_PLAY,
//                        gameType = args.gameType,
//                        playCateCode = oddsDetail?.gameType ?: "",
//                        playCateName = oddsDetail?.name ?: "",
//                        matchInfo = matchOdd.matchInfo,
//                        odd = odd,
//                        subscribeChannelType = ChannelType.EVENT,
//                        betPlayCateNameMap = matchOdd.betPlayCateNameMap,
//                        otherPlayCateName = scoPlayCateNameForBetInfo
//                    )
                }
            }
        ).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

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

        rv_cat.apply {
            adapter = tabCateAdapter
            itemAnimator?.changeDuration = 0
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun observeData() {
        viewModel.oddsDetailResult.observe(this.viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.success) {
                    true -> {
                        result.setupPlayCateTab()

                        matchOdd = result.oddsDetailData?.matchOdd
                        matchOdd?.matchInfo?.leagueName = result.oddsDetailData?.league?.name
                        result.oddsDetailData?.matchOdd?.matchInfo?.homeName?.let { home ->
                            result.oddsDetailData.matchOdd.matchInfo.awayName.let { away ->
                                oddsDetailListAdapter?.homeName = home
                                oddsDetailListAdapter?.awayName = away
                            }
                        }

                        result.oddsDetailData?.matchOdd?.matchInfo?.let { matchInfo ->
                            if (matchInfo.status == GameStatus.POSTPONED.code
                                && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)
                            ) {
                                tv_status_left.text = context?.getString(R.string.game_postponed)
                                tv_status_left.setTextColor(
                                    ContextCompat.getColor(
                                        tv_status_left.context,
                                        R.color.colorRedDark
                                    )
                                )
                                tv_status_left.tag = GameStatus.POSTPONED.code
                            } else {
                                tv_status_left.tag = null
                            }
                        }

                        setupStartTime()
                        setupLiveView(result.oddsDetailData?.matchOdd?.matchInfo?.liveVideo)


                        if (args.matchType == MatchType.IN_PLAY) {
                            tv_time_bottom?.setTextColor(
                                ContextCompat.getColor(
                                    tv_time_bottom.context,
                                    R.color.colorWhite
                                )
                            )
                            tv_time_top?.setTextColor(
                                ContextCompat.getColor(
                                    tv_time_bottom.context,
                                    R.color.colorWhite
                                )
                            )
                        } else {
                            tv_time_bottom?.setTextColor(
                                ContextCompat.getColor(
                                    tv_time_bottom.context,
                                    R.color.colorSilver
                                )
                            )
                            tv_time_top?.setTextColor(
                                ContextCompat.getColor(
                                    tv_time_bottom.context,
                                    R.color.colorSilver
                                )
                            )
                        }

                        if (args.matchType == MatchType.IN_PLAY &&
                            (args.gameType == GameType.BK || args.gameType == GameType.TN || args.gameType == GameType.VB || args.gameType == GameType.TT)
//                            && tv_status_left.isVisible
                            && (it.peekContent()?.oddsDetailData?.matchOdd?.matchInfo?.spt != null)
                        ) {
                            tv_spt.visibility = View.VISIBLE
                            tv_spt.text =
                                " / ${(it.peekContent()?.oddsDetailData?.matchOdd?.matchInfo?.spt) ?: 0}"
                        } else {
                            tv_spt.visibility = View.GONE
                        }

                    }
                    false -> {
                        showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                    }
                }
            }
        }

        viewModel.oddsDetailList.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { list ->
                if (list.isNotEmpty()) {
                    oddsDetailListAdapter?.oddsDetailDataList = list
                }
            }
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let { list ->
                oddsDetailListAdapter?.betInfoList = list
            }
        }

        viewModel.betInfoResult.observe(this.viewLifecycleOwner) {
            val eventResult = it.getContentIfNotHandled()
            eventResult?.success?.let { success ->
                if (!success && eventResult.code != HttpError.BET_INFO_CLOSE.code) {
                    showErrorPromptDialog(getString(R.string.prompt), eventResult.msg) {}
                }
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            oddsDetailListAdapter?.oddsType = it
        }

        viewModel.favorPlayCateList.observe(this.viewLifecycleOwner) {
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
        }

        viewModel.matchLiveInfo.observe(this.viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { liveStreamInfo ->
                live_view_tool_bar.startPlayer(
                    matchId,
                    matchOdd?.matchInfo?.trackerId,
                    liveStreamInfo.streamUrl
                )
            }
        }

        viewModel.matchTrackerUrl.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { matchTrackerUrl ->
                live_view_tool_bar.setupTrackerUrl(matchTrackerUrl.h5Url)
            }
        }
    }

    private fun initSocketObserver() {
        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    getData()
                }
            }
        }

        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            it?.let { matchStatusChangeEvent ->
                matchStatusChangeEvent.matchStatusCO?.takeIf { ms -> ms.matchId == this.matchId }
                    ?.apply {
                        tv_time_top?.let { tv ->
                            val statusValue =
                                statusNameI18n?.get(getSelectLanguage(context).key) ?: statusName
                            tv.text = statusValue
                        }

                        curHomeScore = homeScore
                        curAwayScore = awayScore
                        curStatus = status

                        setupStatusList(matchStatusChangeEvent)
                    }
            }
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val updateTime = when (args.gameType) {
                    GameType.FT -> {
                        matchClockEvent.matchClockCO?.matchTime
                    }
                    GameType.BK ,GameType.RB ,GameType.AFT-> {
                        matchClockEvent.matchClockCO?.remainingTimeInPeriod
                    }
                    else -> null
                }

                isGamePause = (matchClockEvent.matchClockCO?.stopped == 1)

                updateTime?.let {
                    startTime = updateTime
                    setupStartTime()
                }
            }
        }

        receiver.matchOddsChange.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsChangeEvent ->
                matchOddsChangeEvent.updateOddsSelectedState()

                oddsDetailListAdapter?.oddsDetailDataList?.let { oddsDetailListDataList ->
                    SocketUpdateUtil.updateMatchOddsMap(
                        oddsDetailListDataList,
                        matchOddsChangeEvent,
                        viewModel.favorPlayCateList.value?.find { playCate ->
                            playCate.gameType == args.gameType.key
                        }
                    )
                        ?.let { updatedDataList ->
                            oddsDetailListAdapter?.oddsDetailDataList = updatedDataList
                        } ?: run {
                        oddsDetailListDataList.forEachIndexed { index, oddsDetailListData ->
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
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
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
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeChannelEventAll()
                subscribeChannelEvent(matchId)
            }
        }
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

            if (args.matchType != MatchType.IN_PLAY) {
                val timeStr = TimeUtil.timeFormat(startTime, HM_FORMAT)
                if (timeStr.isNotEmpty()) {
                    tv_time_bottom.text = timeStr
                }
                else {
                    tv_time_bottom.text = getString(R.string.time_null)
                }
                tv_time_top.text = TimeUtil.timeFormat(startTime, DM_FORMAT)
            }
            else {
                // 不需要一直重置
//                tv_time_bottom.text = getString(R.string.time_null)
            }
        }
    }

    private fun setupLiveView(liveVideo: Int?) {
        live_view_tool_bar.setupToolBarListener(liveToolBarListener)
        live_view_tool_bar.setupPlayerControl(liveVideo.toString() == FLAG_LIVE)
        live_view_tool_bar.startPlayer(matchId, matchOdd?.matchInfo?.trackerId, null)
    }

    private fun getData() {
        matchId?.let { matchId ->
            viewModel.getOddsDetail(matchId).run {
                subscribeChannelEvent(matchId)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun OddsDetailResult.setupPlayCateTab() {
        val playCateTypeList = this.oddsDetailData?.matchOdd?.playCateTypeList
        if (playCateTypeList?.isNotEmpty() == true) {
            tabCateAdapter.dataList = playCateTypeList
            tabCateAdapter.selectedPosition = 0
        } else {
            rv_cat.visibility = View.GONE
        }
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

        when (event.matchStatusCO?.status) {
            GameMatchStatus.HIDE_SCORE.value -> {
                tv_home_score.visibility = View.GONE
                tv_away_score.visibility = View.GONE
                tv_home_score_total.visibility = View.GONE
                tv_away_score_total.visibility = View.GONE
                tv_home_score_live.visibility = View.GONE
                tv_away_score_live.visibility = View.GONE
            }
            else -> {
                when (args.gameType) {
                    GameType.FT,
                    GameType.IH -> {
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
                        setupStatusTnVb(event)
                    }
                    GameType.VB,
                    GameType.TT -> {
                        setupBackScore(event)
                        setupStatusTnVb(event)
                    }
                    GameType.RB, GameType.AFT -> {
                        setupFrontScore(event)
                    }
                    GameType.BM -> {
                        setupBackScore(event)
                        setupStatusTnVb(event)
                    }
                    GameType.CK -> {
                        setupFrontScore(event)
                    }
                    // Todo: 仍有其他球種待處理
                    // 20220412 根據h5顯示的版面進行同步, MR, GF, FB, OTHER 無法模擬野佔無賽事可參考
                    // MR, GF, FB, OTHER
                    else -> {
                    }
                }
            }
        }
    }

    private fun setCardText(event: MatchStatusChangeEvent) {
        tv_home_card.isVisible = (event.matchStatusCO?.homeCards ?: 0 > 0)
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
        tv_spt.visibility = View.GONE
        tv_status_left.setTextColor(
            ContextCompat.getColor(
                tv_status_left.context,
                R.color.colorSilver
            )
        )

        event.matchStatusList?.forEachIndexed { index, it ->
            val spanStatusName =
                SpannableString(it.statusNameI18n?.get(getSelectLanguage(context).key))
            val spanScore = SpannableString("${it.homeScore ?: 0}-${it.awayScore ?: 0}  ")

            if (index == event.matchStatusList.lastIndex) {
                spanStatusName.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    spanStatusName.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spanScore.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    spanScore.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            statusBuilder.append(spanStatusName).append(spanScore)
        }

        tv_status_left.text = statusBuilder
    }

    private fun setupStatusTnVb(event: MatchStatusChangeEvent) {

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

        if (tv_status_left.tag != GameStatus.POSTPONED.code) {
            tv_status_left.setTextColor(
                ContextCompat.getColor(
                    tv_status_left.context,
                    R.color.colorWhite8
                )
            )
            tv_spt.setTextColor(
                ContextCompat.getColor(
                    tv_status_left.context,
                    R.color.colorWhite8
                )
            )
            val statusValue =
                event.matchStatusCO?.statusNameI18n?.get(getSelectLanguage(context).key)
                    ?: event.matchStatusCO?.statusName
            tv_status_left.text = statusValue
        } else {
            tv_status_left.setTextColor(
                ContextCompat.getColor(
                    tv_status_left.context,
                    R.color.colorSilver
                )
            )
            tv_spt.setTextColor(ContextCompat.getColor(tv_status_left.context, R.color.colorSilver))
        }
    }

    //作用於頁面轉場流暢性
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            AnimationUtils.loadAnimation(context, nextAnim).apply {
                setAnimationListener(this@OddsDetailLiveFragment)
            }
        } else {
            super.onCreateAnimation(transit, enter, nextAnim)
        }
    }

    override fun onAnimationStart(animation: Animation?) {}

    override fun onAnimationEnd(animation: Animation?) {
        observeData()
        initSocketObserver()
    }

    override fun onAnimationRepeat(animation: Animation?) {}
}