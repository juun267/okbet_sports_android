package org.cxct.sportlottery.ui.sport.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.exoplayer2.util.Util
import com.google.android.material.appbar.AppBarLayout
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_detail_sport.*
import kotlinx.android.synthetic.main.bet_bar_layout.view.*
import kotlinx.android.synthetic.main.content_baseball_status.*
import kotlinx.android.synthetic.main.view_detail_head_toolbar.*
import kotlinx.android.synthetic.main.view_detail_head_toolbar.view.*
import kotlinx.android.synthetic.main.view_status_bar.*
import kotlinx.android.synthetic.main.view_toolbar_detail_collaps.*
import kotlinx.android.synthetic.main.view_toolbar_detail_collaps.view.*
import kotlinx.android.synthetic.main.view_toolbar_detail_live.*
import kotlinx.android.synthetic.main.view_toolbar_detail_live.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.common.TimerManager
import org.cxct.sportlottery.ui.component.DetailLiveViewToolbar
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.maintab.SportViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.odds.*
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.widget.MyWebView
import timber.log.Timber
import java.util.*


@Suppress("DEPRECATION", "SetTextI18n")
class SportDetailActivity : BaseBottomNavActivity<SportViewModel>(SportViewModel::class),
    TimerManager {
    companion object {
        fun startActivity(context: Context, matchInfo: MatchInfo, matchType: MatchType? = null) {
            matchInfo?.let {
                val intent = Intent(context, SportDetailActivity::class.java)
                intent.putExtra("matchInfo", matchInfo)
                intent.putExtra("matchType",
                    matchType
                        ?: if (TimeUtil.isTimeInPlay(it.startTime)) MatchType.IN_PLAY else MatchType.DETAIL)
                context.startActivity(intent)
            }
        }
    }

    private var matchType: MatchType = MatchType.DETAIL
    private var oddsDetailListAdapter: OddsDetailListAdapter? = null
    private var isLogin: Boolean = false
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
    private var betListFragment = BetListFragment()
    private var matchOdd: MatchOdd? = null
    private var matchInfo: MatchInfo? = null
    private var isFlowing = false
    private lateinit var enterAnim: Animation
    private lateinit var exitAnim: Animation
    val handler = Handler()
    private val delayHideRunnable = Runnable { collaps_toolbar.startAnimation(exitAnim) }
    private var isGamePause = false
    override var startTime: Long = 0
    override var timer: Timer = Timer()
    override var timerHandler: Handler = Handler {
        var timeMillis = startTime * 1000L
        if (TimeUtil.isTimeInPlay(matchOdd?.matchInfo?.startTime)) {
            if (!isGamePause) {
                when (matchInfo?.gameType) {
                    GameType.FT.key -> {
                        timeMillis += 1000
                    }
                    GameType.BK.key, GameType.RB.key, GameType.AFT.key -> {
                        timeMillis -= 1000
                    }
                    else -> {
                    }
                }

            }
            //过滤部分球类
            if (when (matchInfo?.gameType) {
                    GameType.BB.key, GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.BM.key ->
                        true
                    else -> {
                        false
                    }
                }
            ) {
                tv_match_time.isVisible = false
                cancelTimer()
                return@Handler false
            }
            tv_match_time?.apply {
                if (needCountStatus(matchOdd?.matchInfo?.socketMatchStatus)) {
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
                    isVisible = true
                }
                collaps_toolbar.tv_toolbar_match_time.text = text
                collaps_toolbar.tv_toolbar_match_time.isVisible = isVisible
            }
        }
        return@Handler false
    }
    private val liveToolBarListener by lazy {
        object : DetailLiveViewToolbar.LiveToolBarListener {
            override fun getLiveInfo(newestUrl: Boolean) {
                matchInfo?.let {
                    viewModel.getLiveInfo(it.id, newestUrl)
                }
            }

            override fun onFullScreen(enable: Boolean) {
                if (enable) {
                    showFullScreen(enable)
                } else {
                    showFullScreen(enable)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_detail_sport)
        initToolBar()
        initData()
        initAllObserve()
        initUI()
        initBottomNavigation()
    }



    override fun initToolBar() {
        iv_back.setOnClickListener {
            onBackPressed()
        }
        iv_toolback.setOnClickListener {
            if (live_view_tool_bar.isFullScreen) {
                live_view_tool_bar.showFullScreen(false)
                showFullScreen(false)
            } else {
                if (toolBar.isVisible) {
                    onBackPressed()
                } else {
                    toolBar.isVisible = true
                    live_view_tool_bar.isVisible = false
                    setScrollEnable(true)
                    collaps_toolbar.isVisible = false
                    collaps_toolbar.iv_toolbar_bg.isVisible = true
                    live_view_tool_bar.release()
                }
            }
        }
        iv_refresh.setOnClickListener {
            iv_refresh.animate().rotation(720f).setDuration(1000).start()
            initAllObserve()
        }
        ImmersionBar.with(this)
            .statusBarDarkFont(false)
            .init()
        ImmersionBar.getStatusBarHeight(this).let {
            v_statusbar.minimumHeight = it
            v_statusbar_1.minimumHeight = it
            toolbar_layout.minimumHeight = it
            iv_toolbar_bg.layoutParams.apply {
                height = it + resources.getDimensionPixelOffset(R.dimen.tool_bar_height)
            }
            collaps_toolbar.layoutParams.height =
                it + resources.getDimensionPixelOffset(R.dimen.tool_bar_height)
        }

        app_bar_layout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
                if (state === State.COLLAPSED) {
                    //折叠状态
                    collaps_toolbar.visibility = View.VISIBLE
                } else {
                    if (live_view_tool_bar.isVisible) {
                        collaps_toolbar.visibility = View.VISIBLE
                    } else {
                        collaps_toolbar.visibility = View.GONE
                    }
                }
            }
        })
        live_view_tool_bar.web_view.setOnTouchScreenListener(object :
            MyWebView.OnTouchScreenListener {
            override fun onTouchScreen() {
                isFlowing = true;
                if (collaps_toolbar.getVisibility() == View.GONE) {
                    collaps_toolbar.startAnimation(enterAnim);
                    collaps_toolbar.setVisibility(View.VISIBLE);
                }
            }

            override fun onReleaseScreen() {
                isFlowing = false;
                startDelayHideTitle()
            }
        })
    }

    private fun initAnim() {
        enterAnim = AnimationUtils.loadAnimation(this, R.anim.pop_top_to_bottom_enter)
        enterAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                collaps_toolbar.isVisible = true
                iv_fullscreen.isVisible = true
            }

            override fun onAnimationEnd(animation: Animation?) {

            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
        })
        enterAnim.duration = 300
        exitAnim = AnimationUtils.loadAnimation(this, R.anim.push_bottom_to_top_exit)
        exitAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                collaps_toolbar.isVisible = false
                iv_fullscreen.isVisible = false
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        exitAnim.duration = 300
    }

    private fun startDelayHideTitle() {
        collaps_toolbar.isVisible = true
        handler.removeCallbacks(delayHideRunnable)
        handler.postDelayed(delayHideRunnable, 2000)
    }

    override fun initMenu() {
    }

    override fun clickMenuEvent() {
    }

    override fun initBottomNavigation() {
        cl_bet_list_bar.tv_balance_currency.text = sConfigData?.systemCurrencySign
        cl_bet_list_bar.tv_balance.text = TextUtil.formatMoney(0.0)
        cl_bet_list_bar.setOnClickListener {
            showBetListPage()
        }
    }

    override fun showBetListPage() {
        betListFragment =
            BetListFragment.newInstance(object : BetListFragment.BetResultListener {
                override fun onBetResult(
                    betResultData: Receipt?,
                    betParlayList: List<ParlayOdd>,
                    isMultiBet: Boolean,
                ) {
                    showBetReceiptDialog(betResultData, betParlayList, isMultiBet, R.id.fl_bet_list)
                }

            })

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit,
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit
            )
            .add(R.id.fl_bet_list, betListFragment)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()
    }

    override fun updateUiWithLogin(isLogin: Boolean) {
    }

    override fun updateOddsType(oddsType: OddsType) {
    }

    override fun updateBetListCount(num: Int) {
        cl_bet_list_bar.isVisible = num > 0
        cl_bet_list_bar.tv_bet_list_count.text = num.toString()
        Timber.e("num: $num")
        if (num > 0) viewModel.getMoney()
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            setAnchorView(R.id.snackbar_holder)
            show()
        }
    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
    }

    override fun navOneSportPage(thirdGameCategory: ThirdGameCategory?) {
        TODO("Not yet implemented")
    }

    private fun initData() {
        clickButton()
        matchInfo = intent.getParcelableExtra<MatchInfo>("matchInfo")
        matchType = intent.getSerializableExtra("matchType") as MatchType
        matchInfo?.let {
            setupMatchInfo(it)
        }
    }

    override fun onResume() {
        super.onResume()
        startTimer()
        isLogin = viewModel.loginRepository.isLogin.value == true
        live_view_tool_bar.initLoginStatus(isLogin)

    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            live_view_tool_bar.stopPlayer()
        }
        cancelTimer()
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            live_view_tool_bar.stopPlayer()
        }
        unSubscribeChannelEventAll()
    }

    override fun onDestroy() {
        viewModel.clearLiveInfo()
        super.onDestroy()
    }

    private fun initUI() {
        oddsDetailListAdapter = OddsDetailListAdapter(
            OnOddClickListener { odd, oddsDetail, scoPlayCateNameForBetInfo ->
                if (mIsEnabled) {
                    avoidFastDoubleClick()
                    matchOdd?.let { matchOdd ->
                        val fastBetDataBean = FastBetDataBean(
                            matchType = matchType,
                            gameType = GameType.getGameType(matchOdd.matchInfo.gameType)!!,
                            playCateCode = oddsDetail?.gameType ?: "",
                            playCateName = oddsDetail?.name ?: "",
                            matchInfo = matchOdd.matchInfo,
                            matchOdd = null,
                            odd = odd,
                            subscribeChannelType = ChannelType.EVENT,
                            betPlayCateNameMap = matchOdd.betPlayCateNameMap,
                            otherPlayCateName = scoPlayCateNameForBetInfo
                        )
                        viewModel.updateMatchBetListData(fastBetDataBean)
                    }
                }
            }
        ).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            oddsDetailListener = OddsDetailListener {
                viewModel.pinFavorite(FavoriteType.PLAY_CATE, it, matchInfo?.gameType)
            }

            sportCode = GameType.getGameType(matchInfo?.gameType)
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
        iv_arrow.apply {
            isSelected = false
            setOnClickListener {
                isSelected = !isSelected
                oddsDetailListAdapter?.apply {
                    oddsDetailDataList.forEach {
                        it.isExpand = !isSelected
                    }
                    notifyDataSetChanged()
                }
            }
        }
        matchInfo?.id?.let {
            setupAnalyze(it)
        }
        isShowOdd(true)
        initAnim()
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun initObserve() {
        viewModel.notifyLogin.observe(this) {
            showLoginNotify()
        }
        viewModel.userInfo.observe(this) { userInfo ->
            oddsDetailListAdapter?.discount = userInfo?.discount ?: 1.0F
        }
        viewModel.userMoney.observe(this) {
            it?.let { money ->
                cl_bet_list_bar.tv_balance.text = TextUtil.formatMoney(money)
            }
        }
        viewModel.showBetInfoSingle.observe(this) {
            it.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }
        viewModel.oddsDetailResult.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.success) {
                    true -> {
                        result.setupPlayCateTab()

                        matchOdd = result.oddsDetailData?.matchOdd

                        result.oddsDetailData?.matchOdd?.matchInfo?.let { matchInfo ->
                            this.matchInfo = matchInfo
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
                        setupLiveView(result.oddsDetailData?.matchOdd?.matchInfo?.liveVideo)
                    }
                    false -> {
                        showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                    }
                }
            }
        }

        viewModel.oddsDetailList.observe(this) {
            it.peekContent()?.let { list ->
                if (list.isNotEmpty()) {
                    oddsDetailListAdapter?.oddsDetailDataList = list
                }
            }
        }

        viewModel.betInfoList.observe(this) {
            it.peekContent().let { list ->
                oddsDetailListAdapter?.betInfoList = list
            }
        }

//        TODO 被删了
//        viewModel.betInfoResult.observe(this) {
//            val eventResult = it.getContentIfNotHandled()
//            eventResult?.success?.let { success ->
//                if (!success && eventResult.code != HttpError.BET_INFO_CLOSE.code) {
//                    showErrorPromptDialog(getString(R.string.prompt), eventResult.msg) {}
//                }
//            }
//        }

        viewModel.oddsType.observe(this) {
            oddsDetailListAdapter?.oddsType = it
        }

        viewModel.favorPlayCateList.observe(this) {
            oddsDetailListAdapter?.let { oddsDetailListAdapter ->
                val playCate = it.find { playCate ->
                    playCate.gameType == matchInfo?.gameType
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

        viewModel.matchLiveInfo.observe(this) {
            it?.getContentIfNotHandled()?.let { liveStreamInfo ->
//                live_view_tool_bar.videoUrl = liveStreamInfo.streamUrl
            }
        }
        viewModel.videoUrl.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { url ->
                if (lin_video.isVisible)
                    live_view_tool_bar.videoUrl = url
            }
        }
        viewModel.animeUrl.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { url ->
                if (lin_anime.isVisible)
                    live_view_tool_bar.animeUrl = url
            }
        }

        viewModel.showBetUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true)
                snackBarBetUpperLimitNotify.apply {
                    setAnchorView(R.id.cl_bet_list_bar)
                    show()
                }
        }
    }

    private fun setupLiveView(liveVideo: Int?) {
        with(live_view_tool_bar) {
            setupToolBarListener(liveToolBarListener)
        }
    }

    /**
     * 点击事件
     */
    fun clickButton() {
        btn_odd.setOnClickListener { isShowOdd(true) }
        btn_analyze.setOnClickListener { isShowOdd(false) }
    }

    private fun isShowOdd(isShowOdd: Boolean) {
        val selectColor = ContextCompat.getColor(this, R.color.color_025BE8)
        val nomalColor = ContextCompat.getColor(this, R.color.color_6C7BA8)
        btn_odd.setTextColor(if (isShowOdd) selectColor else nomalColor)
        viewBtOdd.isVisible = isShowOdd
        rv_detail.isVisible = isShowOdd
        lin_categroy.isVisible = isShowOdd

        btn_analyze.setTextColor(if (!isShowOdd) selectColor else nomalColor)
        viewBtnAnalyze.isVisible = !isShowOdd
        wv_analyze.isVisible = !isShowOdd

    }

    /**
     * 配置賽事資訊(隊伍名稱、是否延期、賽制)
     */
    private fun setupMatchInfo(matchInfo: MatchInfo) {
        //region 隊伍名稱
        tv_game_title.text = matchInfo.leagueName
        tv_home_name.text = matchInfo.homeName ?: ""
        tv_away_name.text = matchInfo.awayName ?: ""
        tv_toolbar_home_name.text = matchInfo.homeName ?: ""
        tv_toolbar_away_name.text = matchInfo.awayName ?: ""
        img_home_logo.setTeamLogo(matchInfo.homeIcon)
        img_away_logo.setTeamLogo(matchInfo.awayIcon)
        //endregion
        //region 比賽延期判斷
        if (matchInfo.status == GameStatus.POSTPONED.code
            && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)
        ) {
            toolBar.tv_score.text = getString(R.string.game_postponed)
            tv_toolbar_home_score.text = "-"
            tv_toolbar_away_score.text = "-"
            lin_bottom.isVisible = false
            return
        }
        //endregion

        //赛事进行中，就显示比分状态，否则就不显示左下角，并且显示开赛时间
        var isInPlay = TimeUtil.isTimeInPlay(matchInfo.startTime)
        if (isInPlay) {
            lin_bottom.isVisible = true
            setStatusText(matchInfo)
            updateMenu(matchInfo)
            setupMatchScore(matchInfo)
        } else {
            var startDate = TimeUtil.timeFormat(matchInfo.startTime, TimeUtil.DM_HM_FORMAT)
            startDate.split(" ").let {
                if (it.size == 2) {
                    tv_match_time.text = it[0]
                    tv_score.text = it[1]
                    tv_match_status.isVisible = false
                    tv_score.isVisible = true
                    tv_match_time.isVisible = true
                } else {
                    tv_match_status.isVisible = false
                    tv_score.isVisible = false
                    tv_match_time.isVisible = false
                }
            }
            lin_bottom.isVisible = false
            collaps_toolbar.tv_toolbar_match_status.text = tv_match_status.text
            collaps_toolbar.tv_toolbar_match_status.isVisible = tv_match_status.isVisible
            collaps_toolbar.tv_toolbar_match_time.text = tv_match_time.text
            collaps_toolbar.tv_toolbar_match_time.isVisible = tv_match_time.isVisible
            collaps_toolbar.tv_toolbar_home_score.isVisible = tv_score.isVisible
            collaps_toolbar.tv_toolbar_away_score.isVisible = tv_score.isVisible
        }
    }

    private fun initSocketObserver() {
        receiver.serviceConnectStatus.observe(this) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    getData()
                }
            }
        }

        receiver.matchStatusChange.observe(this) {
            it?.let { matchStatusChangeEvent ->
                matchStatusChangeEvent.matchStatusCO?.takeIf { ms -> ms.matchId == matchInfo?.id }
                    ?.apply {
                        //從滾球以外的狀態轉變為滾球時, 重新獲取一次賽事資料, 看是否有新的直播或動畫url
                        if (matchType != MatchType.IN_PLAY) {
                            matchType = MatchType.IN_PLAY
                            unsubscribeHallChannel(matchId)
                            getData()
                        }
                        matchOdd?.let { matchOdd ->
                            var isNeedUpdate = SocketUpdateUtil.updateMatchStatus(
                                gameType = gameType,
                                matchOdd = matchOdd,
                                matchStatusChangeEvent,
                                context = this@SportDetailActivity
                            )
                            if (isNeedUpdate) {
                                setupMatchInfo(matchOdd.matchInfo)
                            }
                        }
                    }
            }
        }

        receiver.matchClock.observe(this) {
            it?.let { matchClockEvent ->
                if (matchClockEvent.matchClockCO?.matchId != matchInfo?.id) {
                    return@let
                }
                val updateTime = when (matchInfo?.gameType) {
                    GameType.FT.key -> {
                        matchClockEvent.matchClockCO?.matchTime
                    }
                    GameType.BK.key, GameType.RB.key, GameType.AFT.key -> {
                        matchClockEvent.matchClockCO?.remainingTimeInPeriod
                    }
                    else -> null
                }

                isGamePause = (matchClockEvent.matchClockCO?.stopped == 1)
                updateTime?.let { time ->
                    startTime = time
                    matchType =
                        if (TimeUtil.isTimeInPlay(startTime)) MatchType.IN_PLAY else MatchType.DETAIL
                }
            }
        }

        receiver.matchOddsChange.observe(this) {
            it?.getContentIfNotHandled()?.let { matchOddsChangeEvent ->
                oddsDetailListAdapter?.oddsDetailDataList?.let { oddsDetailListDataList ->
                    SocketUpdateUtil.updateMatchOddsMap(
                        oddsDetailListDataList,
                        matchOddsChangeEvent,
                        viewModel.favorPlayCateList.value?.find { playCate ->
                            playCate.gameType == matchInfo?.gameType
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

        receiver.matchOddsLock.observe(this) {
            it?.let { matchOddsLockEvent ->
                //比對收到 matchOddsLock event 的 matchId
                if (matchInfo?.id == matchOddsLockEvent.matchId) {
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

        receiver.globalStop.observe(this) {
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

        receiver.producerUp.observe(this) {
            it?.let {
                unSubscribeChannelEventAll()
                subscribeChannelEvent(matchInfo?.id)
            }
        }

        receiver.closePlayCate.observe(this) { event ->
            event?.getContentIfNotHandled()?.let {
                if (matchInfo?.gameType != it.gameType) return@observe
                oddsDetailListAdapter?.oddsDetailDataList?.apply {
                    indexOf(
                        find { date ->
                            date.gameType == it.playCateCode //命名待優化 此處gameType並非球種 而為玩法code
                        }?.apply {
                            this.oddArrayList.forEach { odd ->
                                odd?.status = BetStatus.DEACTIVATED.code
                            }
                        }
                    ).let { index ->
                        if (index < 0) return@observe
                        oddsDetailListAdapter?.notifyItemChanged(index)
                    }
                }
            }
        }
    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(
        oddsDetailListData: OddsDetailListData,
        matchOddsChangeEvent: MatchOddsChangeEvent,
    ) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (oddsDetailListData.oddArrayList.any { odd ->
                    odd?.isSelected == true
                }) {
                viewModel.updateMatchOdd(matchOddsChangeEvent)
            }
        }
    }

    //TODO 底部注单是否显示
    override fun getBetListPageVisible(): Boolean {
        return betListFragment.isVisible
    }


    private fun getData() {
        matchInfo?.let {
            viewModel.getOddsDetail(it.id).run {
                subscribeChannelEvent(it.id)
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


    /** 初始化监听 **/
    private fun initAllObserve() {
        initObserve()
        initSocketObserver()
    }

    fun updateMenu(matchInfo: MatchInfo) {
        toolBar.apply {
            lin_video.isVisible =
                matchInfo?.liveVideo == 1 && (TimeUtil.isTimeInPlay(matchInfo.startTime))
            lin_anime.isVisible =
                TimeUtil.isTimeInPlay(matchInfo?.startTime) && !(matchInfo?.trackerId.isNullOrEmpty()) && MultiLanguagesApplication.getInstance()
                    ?.getGameDetailAnimationNeedShow() == true
            lin_video.setOnClickListener {
                live_view_tool_bar.videoUrl?.let {
                    toolBar.isVisible = false
                    live_view_tool_bar.isVisible = true
                    collaps_toolbar.isVisible = true
                    collaps_toolbar.iv_toolbar_bg.isVisible = false
                    live_view_tool_bar.showVideo()
                    setScrollEnable(false)
                    startDelayHideTitle()
                }
            }
            lin_anime.setOnClickListener {
                live_view_tool_bar.animeUrl?.let {
                    toolBar.isVisible = false
                    live_view_tool_bar.isVisible = true
                    collaps_toolbar.isVisible = true
                    collaps_toolbar.iv_toolbar_bg.isVisible = false
                    live_view_tool_bar.showAnime()
                    setScrollEnable(false)
                    startDelayHideTitle()
                }
            }
            if (lin_live.isVisible || lin_video.isVisible || lin_anime.isVisible) {
                lin_menu.isVisible = true
                v_menu_1.isVisible = lin_live.isVisible
                v_menu_2.isVisible = lin_video.isVisible && lin_anime.isVisible
            } else {
                lin_menu.isVisible = false
            }
        }
    }

    fun showFullScreen(enable: Boolean) {
        if (enable) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            sv_content.isVisible = false
            lin_center.isVisible = false
            toolBar.isVisible = false
            live_view_tool_bar.isVisible = true
            collaps_toolbar.isVisible = true
            app_bar_layout.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            setScrollEnable(false)
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            sv_content.isVisible = true
            lin_center.isVisible = true
            toolBar.isVisible = false
            live_view_tool_bar.isVisible = true
            collaps_toolbar.isVisible = true
            app_bar_layout.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            setScrollEnable(false)
        }
        live_view_tool_bar.layoutParams.apply {
            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = if (enable) ViewGroup.LayoutParams.MATCH_PARENT else 250.dp
            } else {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
        live_view_tool_bar.invalidate()
    }

    /**
     * 设置是否可以滑动折叠
     */
    private fun setScrollEnable(enable: Boolean) {
        (app_bar_layout.getChildAt(0).layoutParams as AppBarLayout.LayoutParams).apply {
            scrollFlags = if (enable)
                (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                        or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                        or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP)
            else
                AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
        }
    }

    /**
     * 棒球同时处理
     */
    private fun setupMatchScore(matchInfo: MatchInfo) {
        when (matchInfo?.gameType) {
            GameType.BB.key -> {
                lin_tips.isVisible = false
                content_baseball_status.isVisible = true
            }
            else -> {
                lin_tips.isVisible = true
                content_baseball_status.isVisible = false
            }
        }
        when (matchInfo.gameType) {
            GameType.VB.key -> setVbScoreText(matchInfo)
            GameType.TN.key -> setTnScoreText(matchInfo)
            GameType.FT.key -> setFtScoreText(matchInfo)
            GameType.BK.key -> setBkScoreText(matchInfo)
            GameType.TT.key -> setVbScoreText(matchInfo)
            GameType.BM.key -> setBmScoreText(matchInfo)
            GameType.BB.key -> setBbScoreText(matchInfo)
            else -> setBkScoreText(matchInfo)
        }
    }

    private fun setFtScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setCardText(matchInfo)
        setFbKicks(matchInfo)
        setFtHalfScore(matchInfo)
    }

    private fun setBkScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo)
    }

    private fun setVbScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo)
        setSptText(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setTnScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo)
        setSptText(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setBmScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo)
        setSptText(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setBbScoreText(matchInfo: MatchInfo) {
        if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
            setScoreTextAtFront(matchInfo)
            setAttack(matchInfo)
            setBBStatus(matchInfo)
            setCurrentPeroid(matchInfo)
        } else
            setBkScoreText(matchInfo)
    }

    /**
     * 賽制(5盤3勝)
     * 只有网球，排球，乒乓球，羽毛球
     */
    @SuppressLint("SetTextI18n")
    private fun setSptText(matchInfo: MatchInfo) {
//        matchInfo.spt?.let {
//            if (it == 3 || it == 5|| it == 7) {
//                league_spt.visibility = View.VISIBLE
//                league_spt.text = when (it) {
//                    3 -> {
//                        when(matchInfo.gameType){
//                            ////排球，乒乓球显示3局2胜
//                            GameType.BM.key-> getString(R.string.spt_number_3_2_bm)
//                            else-> getString(R.string.spt_number_3_2)
//                        }
//                    }
//                    5 -> {
//                        when(matchInfo.gameType){
//                            //排球，乒乓球显示5局3胜
//                            GameType.VB.key,GameType.TT.key-> getString(R.string.spt_number_5_3_vb)
//                            else-> getString(R.string.spt_number_5_3)
//                        }
//                    }
//                    7 -> {
//                        //部分乒乓球会采用七局四胜制
//                        when(matchInfo.gameType){
//                            GameType.TT.key-> getString(R.string.spt_number_7_4_tt)
//                            else-> ""
//                        }
//                    }
//                    else -> ""
//                }
//            } else {
////                league_spt.visibility = View.GONE
//            }
//        }
    }

    /**
     * 设置当前盘数/局数/回合
     * 网球显示 第x盘
     * 其他球类显示 第x局
     */
    @SuppressLint("SetTextI18n")
    private fun setCurrentPeroid(matchInfo: MatchInfo) {
        matchInfo.matchStatusList?.let {
            if (it.isEmpty()) return
            tv_match_status.visibility = View.VISIBLE
            matchInfo.matchStatusList?.let { it ->
                tv_match_status.visibility = View.VISIBLE
                it.last()?.let {
                    tv_match_status.text = it.statusNameI18n?.get(
                        LanguageManager.getSelectLanguage(context = this).key
                    ) ?: it.statusName
                }
            }
            tv_toolbar_match_status.isVisible = tv_match_status.isVisible
            tv_toolbar_match_status.text = tv_match_status.text
        }
    }
    /**
     * 设置足球半场比分
     */
    private fun setFtHalfScore(matchInfo: MatchInfo) {
        tv_ft_half.apply {
            visibility = when {
                (!matchInfo.homeHalfScore.isNullOrEmpty()) || (!matchInfo.awayHalfScore.isNullOrEmpty()) -> View.VISIBLE
                else -> View.GONE
            }
            text =
                getString(R.string.half) + ": " + matchInfo.homeHalfScore + "-" + matchInfo.awayHalfScore
        }

    }

    /**
     * 设置足球黄牌，红牌数量
     */
    private fun setCardText(matchInfo: MatchInfo) {
        tv_red_card.apply {
            visibility = when {
                (matchInfo.homeCards ?: 0 > 0) || (matchInfo.awayCards ?: 0 > 0) -> View.VISIBLE
                else -> View.GONE
            }
            text =
                (matchInfo.homeCards ?: 0).toString() + "-" + (matchInfo.awayCards ?: 0).toString()
        }

        tv_yellow_card.apply {
            visibility = when {
                (matchInfo.homeYellowCards ?: 0 > 0) || (matchInfo.awayYellowCards ?: 0 > 0) -> View.VISIBLE
                else -> View.GONE
            }
            text = (matchInfo.homeYellowCards ?: 0).toString() + "-" + (matchInfo.awayYellowCards
                ?: 0).toString()
        }

    }

    /**
     * 设置球权标识，
     *  目前支持 棒球，网球，排球，乒乓球，羽毛球
     *  其中网球标识是另外一个位置
     */
    private fun setAttack(matchInfo: MatchInfo) {
        if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
            when (matchInfo.gameType) {
                GameType.BB.key,
                GameType.VB.key,
                GameType.TT.key,
                GameType.BM.key,
                GameType.TN.key,
                -> {
                    if (matchInfo.attack.equals("H")) {
                        ic_attack_h.visibility = View.VISIBLE
                        ic_attack_c.visibility = View.INVISIBLE
                    } else {
                        ic_attack_h.visibility = View.INVISIBLE
                        ic_attack_c.visibility = View.VISIBLE
                    }
                }
                else -> {
                    ic_attack_h.visibility = View.GONE
                    ic_attack_c.visibility = View.GONE
                }
            }
        } else {
            ic_attack_h.visibility = View.GONE
            ic_attack_c.visibility = View.GONE
        }
    }

    private fun setFbKicks(matchInfo: MatchInfo) {
        league_corner_kicks.apply {
            visibility = when {
                TimeUtil.isTimeInPlay(matchInfo.startTime)
                        && (matchInfo.homeCornerKicks ?: 0 > 0 || matchInfo.awayCornerKicks ?: 0 > 0) -> View.VISIBLE
                else -> View.GONE
            }
            text = (matchInfo.homeCornerKicks
                ?: 0).toString() + "-" + (matchInfo.awayCornerKicks ?: 0)
        }
    }

    private fun setScoreTextAtFront(matchInfo: MatchInfo) {
        tv_score.apply {
            visibility = when (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
                true -> View.VISIBLE
                else -> View.GONE
            }
            text = when (matchInfo.gameType) {
                GameType.VB.key, GameType.TT.key, GameType.BM.key -> (matchInfo.homeTotalScore
                    ?: 0).toString() + " - " + (matchInfo.awayTotalScore ?: 0).toString()
                else -> (matchInfo.homeScore ?: 0).toString() + " - " + (matchInfo.awayScore
                    ?: 0).toString()
            }
        }
        tv_toolbar_home_score.apply {
            visibility = when (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
                true -> View.VISIBLE
                else -> View.GONE
            }
            text = when (matchInfo.gameType) {
                GameType.VB.key, GameType.TT.key, GameType.BM.key -> (matchInfo.homeTotalScore
                    ?: 0).toString()
                else -> (matchInfo.homeScore ?: 0).toString()
            }
        }
        tv_toolbar_away_score.apply {
            visibility = when (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
                true -> View.VISIBLE
                else -> View.GONE
            }
            text = when (matchInfo.gameType) {
                GameType.VB.key, GameType.TT.key, GameType.BM.key -> (matchInfo.awayTotalScore
                    ?: 0).toString()
                else -> (matchInfo.awayScore ?: 0).toString()
            }
        }
    }


    /**
     * 网球和羽毛球  排球，乒乓球 显示局比分
     */
    private fun setAllScoreTextAtBottom(matchInfo: MatchInfo) {
        matchInfo.matchStatusList?.let { matchStatusList ->
            var spanny = Spanny()
            if (matchInfo.gameType == GameType.BK.key) {
                //篮球类 1:第一节 2:第二节 6:上半场 7:下半场 13:第一节 14:第二节 15:第三节 16:第四节 31:半场 32:等待加时赛 40:加时 80:中断 90:弃赛 100:完场 110:加时赛后 301:第1次休息 302:第2次休息 303:第3次休息 999:滚球
                var peroid = when (matchInfo.socketMatchStatus) {
                    1, 13 -> 0
                    2, 14 -> 1
                    15 -> 2
                    16 -> 3
                    else -> 4//其他情况全部显示
                }
                matchStatusList.forEachIndexed { index, it ->
                    val spanScore = "${it.homeScore ?: 0}-${it.awayScore ?: 0}"
                    //9表示已结束，其他代表进行中的
                    if (index < peroid) {
                        spanny.append(spanScore)
                        spanny.append("  ")
                    } else {
                        spanny.append(spanScore,
                            ForegroundColorSpan(getColor(R.color.color_F0A536)))
                    }
                }
            } else {
                matchStatusList.forEachIndexed { index, it ->
                    val spanScore = "${it.homeScore ?: 0}-${it.awayScore ?: 0}"
                    //9表示已结束，其他代表进行中的
                    if (index < matchStatusList.lastIndex) {
                        spanny.append(spanScore)
                        spanny.append("  ")
                    } else {
                        spanny.append(spanScore,
                            ForegroundColorSpan(getColor(R.color.color_F0A536)))
                    }
                }
                tv_peroids_score.isVisible = true
                tv_peroids_score.text = spanny
            }

        }
    }


    private fun setStatusText(matchInfo: MatchInfo) {
        tv_match_status.text = when {
            (TimeUtil.isTimeInPlay(matchInfo.startTime)
                    && matchInfo.status == GameStatus.POSTPONED.code
                    && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)) -> {
                getString(R.string.game_postponed)
            }
            TimeUtil.isTimeInPlay(matchInfo.startTime) -> {
                if (matchInfo.statusName18n != null) {
                    //网球，排球，乒乓，羽毛球，就不显示
                    if (matchInfo.gameType == GameType.TN.name
                        || matchInfo.gameType == GameType.VB.name
                        || matchInfo.gameType == GameType.TT.name
                        || matchInfo.gameType == GameType.BM.name
                    ) {
                        ""
                    } else {
                        matchInfo.statusName18n
                    }

                } else {
                    ""
                }
            }
            else -> {
                if (TimeUtil.isTimeToday(matchInfo.startTime))
                    getString((R.string.home_tab_today))
                else
                    matchInfo.startDateDisplay
            }
        }
        tv_toolbar_match_status.text = tv_match_status.text
    }

    /**
     * 棒球的特殊布局处理
     */
    private fun setBBStatus(matchInfo: MatchInfo) {
        lin_tips.isVisible = false
        content_baseball_status.isVisible = true
        league_odd_match_bb_status.isVisible = false
        league_odd_match_halfStatus.isVisible = false

        txvOut.apply {
            text = getString(R.string.game_out,
                matchInfo.outNumber ?: "")
            isVisible = true
        }
        tv_match_time.apply {
            text =
                if (matchInfo.halfStatus == 0) getString(R.string.half_first_short) else getString(R.string.half_second_short)
            isVisible = true
        }

        league_odd_match_basebag.apply {
            setImageResource(
                when {
                    matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_0_0
                    matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_0_0
                    matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_1_0
                    matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_0_1
                    matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_1_0
                    matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_0_1
                    matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_1_1
                    matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_1_1
                    else -> R.drawable.ic_bb_base_bag_0_0_0
                }
            )
            isVisible = true
        }
    }

    fun setupAnalyze(matchId: String) {
        wv_analyze.apply {
            settings.javaScriptEnabled = true

            webViewClient = WebViewClient()

            sConfigData?.analysisUrl?.replace("{lang}",
                LanguageManager.getSelectLanguage(this@SportDetailActivity).key)
                ?.replace("{eventId}", matchId)?.let {
                loadUrl(it)
            }
            setWebViewCommonBackgroundColor()
        }
    }

}
