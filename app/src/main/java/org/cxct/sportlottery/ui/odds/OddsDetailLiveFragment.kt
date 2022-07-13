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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.fragment_odds_detail_live.*
import kotlinx.android.synthetic.main.view_odds_detail_toolbar.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailLiveBinding
import org.cxct.sportlottery.enum.MatchSource
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusCO
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.repository.FLAG_LIVE
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.common.TimerManager
import org.cxct.sportlottery.ui.component.LiveViewToolbar
import org.cxct.sportlottery.ui.favorite.MyFavoriteActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.LanguageManager.getSelectLanguage
import org.cxct.sportlottery.util.TimeUtil.DM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import java.util.*

/**
 * @app_destination 全部玩法(包含直播)
 */
//TODO 即將開賽轉滾球時會短暫顯示完賽的狀態, 已通報後端進行溝通
@Suppress("DEPRECATION", "SetTextI18n")
class OddsDetailLiveFragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class), TimerManager, Animation.AnimationListener {

    private val args: OddsDetailLiveFragmentArgs by navArgs()
    private var matchType: MatchType = MatchType.OTHER

    private var oddsDetailListAdapter: OddsDetailListAdapter? = null
    private var isLogin:Boolean = false
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

    private var curHomeCornerKicks: Int? = null
    private var curAwayCornerKicks: Int? = null

    private var curStatus: Int? = null

    private var isGamePause = false
    override var startTime: Long = 0
    override var timer: Timer = Timer()
    override var timerHandler: Handler = Handler {
        var timeMillis = startTime * 1000L

        when (matchType) {
            //原滾球時間顯示邏輯
            MatchType.IN_PLAY -> {
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
                            isVisible = true
                        } else {
                            text = this.context.getString(R.string.time_null)
                            isVisible = false
                        }
                    } else {
                        text = this.context.getString(R.string.time_null)
                        isVisible = false
                    }
                }
            }
            else -> {
                setupNotInPlayTime()

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
                StatisticsDialog.newInstance(matchId, StatisticsDialog.StatisticsClickListener { clickMenu() })
                    .show(childFragmentManager, StatisticsDialog::class.java.simpleName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        matchId = args.matchId
        matchType = args.matchType
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
        vToolbar.ivTitleBar.setImageResource(
            GameConfigManager.getTitleBarBackground(args.gameType.key, MultiLanguagesApplication.isNightMode) ?: R.drawable.img_home_title_soccer_background
        )
    }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    override fun onStart() {
        super.onStart()

        if (Util.SDK_INT >= 24) {
            live_view_tool_bar.startPlayer(matchId, matchOdd?.matchInfo?.trackerId, null,isLogin)
        }
    }

    override fun onResume() {
        super.onResume()
        startTimer()
        isLogin = viewModel.loginRepository.isLogin.value == true
        live_view_tool_bar.initLoginStatus(isLogin)

        if ((Util.SDK_INT < 24) || live_view_tool_bar.getExoPlayer() == null) {
            live_view_tool_bar.startPlayer(matchId, matchOdd?.matchInfo?.trackerId, null,isLogin)
        }

//        if(args.liveVideo == 0) live_view_tool_bar.setUnLiveState()

        matchOdd?.matchInfo?.let { matchInfo ->
            live_view_tool_bar.setStatisticsState(matchInfo.source == MatchSource.SHOW_STATISTICS.code)
        }

        if (MultiLanguagesApplication.colorModeChanging) {
            initObserve()
            initSocketObserver()
            MultiLanguagesApplication.colorModeChanging = false
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
        live_view_tool_bar.gameType = args.gameType //賽事動畫icon用，之後用不到可刪
        oddsDetailListAdapter = OddsDetailListAdapter(
            OnOddClickListener { odd, oddsDetail, scoPlayCateNameForBetInfo ->
                if(mIsEnabled) {
                    avoidFastDoubleClick()
                    matchOdd?.let { matchOdd ->
                        matchOdd.matchInfo.homeScore = "$curHomeScore"
                        matchOdd.matchInfo.awayScore = "$curAwayScore"

                        matchOdd.matchInfo.homeCornerKicks = curHomeCornerKicks
                        matchOdd.matchInfo.awayCornerKicks = curAwayCornerKicks

                        val fastBetDataBean = FastBetDataBean(
                            matchType = matchType,
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
                        when (activity) {
                            is GameActivity -> (activity as GameActivity).showFastBetFragment(
                                fastBetDataBean
                            )
                            is GamePublicityActivity -> (activity as GamePublicityActivity).showFastBetFragment(
                                fastBetDataBean
                            )
                            is MyFavoriteActivity -> (activity as MyFavoriteActivity).showFastBetFragment(
                                fastBetDataBean
                            )
                        }
                    }
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
            addScrollListenerForBottomNavBar(
                onScrollDown = {
                    MultiLanguagesApplication.mInstance.setIsScrollDown(it)
                }
            )
        }

        rv_cat.apply {
            adapter = tabCateAdapter
            itemAnimator?.changeDuration = 0
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
        }

        app_bar_layout.addOffsetListenerForBottomNavBar {
            MultiLanguagesApplication.mInstance.setIsScrollDown(it)
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun initObserve() {
        viewModel.userInfo.observe(this.viewLifecycleOwner) { userInfo ->
            oddsDetailListAdapter?.discount = userInfo?.discount ?: 1.0F
        }

        viewModel.oddsDetailResult.observe(this.viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.success) {
                    true -> {
                        result.setupPlayCateTab()

                        matchOdd = result.oddsDetailData?.matchOdd

                        result.oddsDetailData?.matchOdd?.matchInfo?.let { matchInfo ->
                            //region 配置主客隊名稱給內部Item使用
                            matchInfo.homeName?.let { home ->
                                oddsDetailListAdapter?.homeName = home
                            }
                            matchInfo.awayName.let { away ->
                                oddsDetailListAdapter?.awayName = away
                            }
                            //endregion

                            setupMatchInfo(matchInfo)
                        }

                        setupStartTime()
                        setupInitShowView(result.oddsDetailData?.matchOdd?.matchInfo)
                        setupLiveView(result.oddsDetailData?.matchOdd?.matchInfo?.liveVideo)
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
                    v_loading.visibility = View.GONE
                    cl_content.visibility = View.VISIBLE
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
                    liveStreamInfo.streamUrl,isLogin
                )
            }
        }

        viewModel.matchTrackerUrl.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { matchTrackerUrl ->
//                live_view_tool_bar.setupTrackerUrl(matchTrackerUrl.h5Url)
                live_view_tool_bar.setupTrackerUrl(matchTrackerUrl)
            }
        }
    }

    /**
     * 配置賽事資訊(隊伍名稱、是否延期、賽制)
     */
    private fun setupMatchInfo(matchInfo: MatchInfo) {
        //region 隊伍名稱
        tv_home_name.text = matchInfo.homeName ?: ""
        tv_away_name.text = matchInfo.awayName ?: ""
        //endregion

        //region 比賽延期判斷
        if (matchInfo.status == GameStatus.POSTPONED.code
            && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)
        ) {
            tv_status_left.text = context?.getString(R.string.game_postponed)
        }
        //endregion

        //region 比賽賽制
        if (matchType == MatchType.IN_PLAY &&
            (args.gameType == GameType.TN || args.gameType == GameType.VB || args.gameType == GameType.TT || args.gameType == GameType.BM)
            && (matchInfo.spt != null)
        ) {
            tv_spt.visibility = View.VISIBLE
            tv_spt.text = " / ${(matchInfo.spt)}"
        } else {
            tv_spt.visibility = View.GONE
        }
        //endregion
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
                        //從滾球以外的狀態轉變為滾球時, 重新獲取一次賽事資料, 看是否有新的直播或動畫url
                        if (matchType != MatchType.IN_PLAY) {
                            matchType = MatchType.IN_PLAY
                            unsubscribeHallChannel(matchId)
                            getData()
                        }

                        tv_time_top?.let { tv ->
                            val statusValue =
                                statusNameI18n?.get(getSelectLanguage(context).key) ?: statusName
                            tv.text = statusValue
                        }

                        curHomeScore = homeScore
                        curAwayScore = awayScore
                        curStatus = status

                        updateCornerKicks()

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
                    GameType.BK, GameType.RB, GameType.AFT -> {
                        matchClockEvent.matchClockCO?.remainingTimeInPeriod
                    }
                    else -> null
                }

                isGamePause = (matchClockEvent.matchClockCO?.stopped == 1)

                updateTime?.let { time ->
                    startTime = time
                }
            }
        }

        receiver.matchOddsChange.observe(this.viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { matchOddsChangeEvent ->
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
                                updateBetInfo(oddsDetailListData, matchOddsChangeEvent)
                                oddsDetailListAdapter?.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        }

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                //比對收到 matchOddsLock event 的 matchId
                if (matchId == matchOddsLockEvent.matchId) {
                    oddsDetailListAdapter?.oddsDetailDataList?.let { oddsDetailListDataList ->
                        oddsDetailListDataList.forEachIndexed { index, oddsDetailListData ->
                            if (SocketUpdateUtil.updateOddStatus(oddsDetailListData)) {
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

        receiver.closePlayCate.observe(this.viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                if (args.gameType.key != it.gameType) return@observe
                oddsDetailListAdapter?.oddsDetailDataList?.apply {
                    indexOf(
                        find { date ->
                            date.gameType == it.playCateCode //命名待優化 此處gameType並非球種 而為玩法code
                        }
                    ).let { index ->
                        if (index < 0) return@observe
                        removeAt(index)
                        oddsDetailListAdapter?.notifyItemRemoved(index)
                    }
                }
            }
        }
    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(oddsDetailListData: OddsDetailListData, matchOddsChangeEvent: MatchOddsChangeEvent) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (oddsDetailListData.oddArrayList.any { odd ->
                    odd?.isSelected == true
                }) {
                viewModel.updateMatchOdd(matchOddsChangeEvent)
            }
        }
    }

    /**
     * 更新當前頁面的角球數
     */
    private fun MatchStatusCO.updateCornerKicks() {
        //region 判斷需不需要更新資料 角球數都沒變的話就不更新
        var cornerKicksChanged = false
        if (homeCornerKicks != null && curHomeCornerKicks != homeCornerKicks) {
            curHomeCornerKicks = homeCornerKicks
            oddsDetailListAdapter?.homeCornerKicks = homeCornerKicks
            cornerKicksChanged = true
        }

        if (awayCornerKicks != null && curAwayCornerKicks != awayCornerKicks) {
            curAwayCornerKicks = awayCornerKicks
            oddsDetailListAdapter?.awayCornerKicks = awayCornerKicks
            cornerKicksChanged = true
        }
        //endregion

        if (cornerKicksChanged) {
            oddsDetailListAdapter?.oddsDetailDataList?.let { oddsDetailListDataList ->
                oddsDetailListDataList.forEachIndexed { index, oddsDetailListData ->
                    //需要顯示當前滾球的玩法標題需更新
                    if (PlayCate.needShowCurrentCorner(oddsDetailListData.gameType)) {
                        oddsDetailListAdapter?.notifyItemChanged(index)
                    }
                }
            }
        }
    }

    private fun setupStartTime() {
        matchOdd?.matchInfo?.apply {
            if (matchType != MatchType.IN_PLAY) {
                this@OddsDetailLiveFragment.startTime = startTime ?: 0
                setupNotInPlayTime()
            } else {
                //滾球狀態透過socket事件MATCH_CLOCK更新
            }
        }
    }

    private fun setupInitShowView(matchInfo: MatchInfo?) {
        live_view_tool_bar.initLiveType(
            matchInfo?.liveVideo.toString() == FLAG_LIVE,
            !matchOdd?.matchInfo?.trackerId.isNullOrEmpty()
        )
    }

    private fun setupLiveView(liveVideo: Int?) {
        with (live_view_tool_bar) {
            when (matchType) {
                MatchType.IN_PLAY -> {
                    setupToolBarListener(liveToolBarListener)
                    setStatisticsState(matchOdd?.matchInfo?.source == MatchSource.SHOW_STATISTICS.code)
                    setupPlayerControl(liveVideo.toString() == FLAG_LIVE)
                    startPlayer(matchId, matchOdd?.matchInfo?.trackerId, null, isLogin)
                }
                else -> {
                    setupToolBarListener(liveToolBarListener)
                    setStatisticsState(matchOdd?.matchInfo?.source == MatchSource.SHOW_STATISTICS.code)
                    setupPlayerControl(false)
                }
            }
        }
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
    }

    private fun showBackTimeBlock(show: Boolean) {
        ll_time.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupStatusList(event: MatchStatusChangeEvent) {
        if (matchType != MatchType.IN_PLAY) return

        //region setup game score
        when (event.matchStatusCO?.status) {
            GameMatchStatus.FINISH.value -> {
                //donothing
            }
            //20220507 status:999 邏輯變更 隱藏分數 -> 賽事狀態變為滾球
            /*GameMatchStatus.HIDE_SCORE.value -> {
                tv_home_score.visibility = View.GONE
                tv_away_score.visibility = View.GONE
                tv_home_score_total.visibility = View.GONE
                tv_away_score_total.visibility = View.GONE
                tv_home_score_live.visibility = View.GONE
                tv_away_score_live.visibility = View.GONE
            }*/
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
                    }
                    GameType.VB,
                    GameType.TT -> {
                        setupBackScore(event)
                    }
                    GameType.RB, GameType.AFT -> {
                        setupFrontScore(event)
                    }
                    GameType.BM -> {
                        setupBackScore(event)
                    }
                    GameType.CK -> {
                        setupFrontScore(event)
                    }
                    GameType.BB -> {
                        setupFrontScore(event)
                        setupStatusBB(event)
                    }
                    // Todo: 仍有其他球種待處理
                    // 20220412 根據h5顯示的版面進行同步, MR, GF, FB, OTHER 無法模擬野佔無賽事可參考
                    // MR, GF, FB, OTHER
                    else -> {
                    }
                }
            }
        }
        //endregion

        //region setup game status
        //20220507 status:999 邏輯變更 隱藏分數 -> 賽事狀態變為滾球
//        val showScore = event.matchStatusCO?.status != GameMatchStatus.HIDE_SCORE.value
        when (args.gameType) {
            GameType.BK -> {
                setupStatusBk(event)
            }
            GameType.TN, GameType.VB, GameType.TT, GameType.BM -> {
                showBackTimeBlock(false)
                setupStatusTnVb(event, true)
            }
            GameType.BB -> {
                setupFrontScore(event)
                setupStatusBB(event)
            }
            // Todo: 仍有其他球種待處理
            // 20220412 根據h5顯示的版面進行同步, MR, GF, FB, OTHER 無法模擬野佔無賽事可參考
            // MR, GF, FB, OTHER
            else -> {
            }
        }
        //endregion
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
        tv_status_right.visibility = View.GONE
        tv_status_left.setTextColor(
            ContextCompat.getColor(
                tv_status_left.context,
                R.color.color_FFFFFF
            )
        )

        event.matchStatusList?.forEachIndexed { index, it ->
            val spanStatusName =
                SpannableString(it.statusNameI18n?.get(getSelectLanguage(context).key))
            val spanScore = SpannableString(" ${it.homeScore ?: 0}-${it.awayScore ?: 0}  ")

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

    private fun setupStatusBB(event: MatchStatusChangeEvent) {
        tv_status_left.visibility = View.INVISIBLE
        tv_spt.visibility = View.GONE
        tv_status_right.visibility = View.GONE
        ll_time.visibility = View.GONE

        ll_status_bb.visibility = View.VISIBLE
        txvOut.visibility = View.VISIBLE
        league_odd_match_basebag.visibility = View.VISIBLE

        if (event.matchStatusCO?.attack.equals("H")) {
            ic_attack_h.visibility = View.VISIBLE
            ic_attack_c.visibility = View.INVISIBLE
        } else {
            ic_attack_h.visibility = View.INVISIBLE
            ic_attack_c.visibility = View.VISIBLE
        }

    league_odd_match_bb_status.apply {
        text = event.matchStatusCO?.statusNameI18n?.get(getSelectLanguage(context).key) ?: ""
        isVisible = true
    }
    league_odd_match_halfStatus.apply {
        setImageResource(if(event.matchStatusCO?.halfStatus == 0) R.drawable.ic_bb_first_half else R.drawable.ic_bb_second_half)
        isVisible = true
    }
    league_odd_match_basebag.apply {
        setImageResource(
            when {
                event.matchStatusCO?.firstBaseBag == 0 && event.matchStatusCO.secBaseBag == 0 && event.matchStatusCO.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_0_0
                event.matchStatusCO?.firstBaseBag == 1 && event.matchStatusCO.secBaseBag == 0 && event.matchStatusCO.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_0_0
                event.matchStatusCO?.firstBaseBag == 0 && event.matchStatusCO.secBaseBag == 1 && event.matchStatusCO.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_1_0
                event.matchStatusCO?.firstBaseBag == 0 && event.matchStatusCO.secBaseBag == 0 && event.matchStatusCO.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_0_1
                event.matchStatusCO?.firstBaseBag == 1 && event.matchStatusCO.secBaseBag == 1 && event.matchStatusCO.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_1_0
                event.matchStatusCO?.firstBaseBag == 1 && event.matchStatusCO.secBaseBag == 0 && event.matchStatusCO.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_0_1
                event.matchStatusCO?.firstBaseBag == 0 && event.matchStatusCO.secBaseBag == 1 && event.matchStatusCO.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_1_1
                event.matchStatusCO?.firstBaseBag == 1 && event.matchStatusCO.secBaseBag == 1 && event.matchStatusCO.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_1_1
                else -> R.drawable.ic_bb_base_bag_0_0_0
            }
        )
        isVisible = true
    }

        txvOut.apply {
            text = this.context.getString(R.string.game_out, event.matchStatusCO?.outNumber ?: "")
            isVisible = true
        }

    }

    private fun setupStatusTnVb(event: MatchStatusChangeEvent, showScore: Boolean = true) {

        val statusBuilder = SpannableStringBuilder()

        tv_status_left.visibility = View.VISIBLE
        tv_status_left.text = event.matchStatusCO?.statusNameI18n?.get(getSelectLanguage(context).key) ?: ""

        tv_status_right.visibility = if (showScore) View.VISIBLE else View.GONE

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
    }


    /**
     * 配置滾球以外的時間格式 (即將開賽、今日、早盤 ...)
     */
    private fun setupNotInPlayTime() {
        //賽事開始時間若為今天則日期部分顯示今日
        tv_time_top.text =
            if (TimeUtil.isTimeToday(startTime)) getString(R.string.home_tab_today) else TimeUtil.timeFormat(
                startTime,
                DM_FORMAT
            )

        if (TimeUtil.isTimeAtStart(startTime)) {
            //即將開賽時間格式
            tv_time_bottom.text =
                String.format(getString(R.string.at_start_remain_minute), TimeUtil.getRemainMinute(startTime))
            tv_time_top.visibility = View.GONE
        } else {
            //今日、早盤
            val timeStr = TimeUtil.timeFormat(startTime, HM_FORMAT)
            if (timeStr.isNotEmpty()) {
                tv_time_bottom.text = timeStr
            } else {
                tv_time_bottom.text = getString(R.string.time_null)
            }
            //记分牌无法显示时间時，隱藏-:-
            tv_time_bottom.isVisible = timeStr.isNotEmpty()

            tv_time_top.visibility = View.VISIBLE
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
        initObserve()
        initSocketObserver()
    }

    override fun onAnimationRepeat(animation: Animation?) {}
}