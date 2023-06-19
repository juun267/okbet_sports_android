package org.cxct.sportlottery.ui.sport.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.appbar.AppBarLayout
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_detail_sport.*
import kotlinx.android.synthetic.main.bet_bar_layout.view.*
import kotlinx.android.synthetic.main.content_baseball_status.*
import kotlinx.android.synthetic.main.item_sport_odd.view.*
import kotlinx.android.synthetic.main.view_detail_head_toolbar.lin_anime
import kotlinx.android.synthetic.main.view_detail_head_toolbar1.img_away_logo
import kotlinx.android.synthetic.main.view_detail_head_toolbar1.img_home_logo
import kotlinx.android.synthetic.main.view_detail_head_toolbar1.lin_live
import kotlinx.android.synthetic.main.view_detail_head_toolbar1.lin_video
import kotlinx.android.synthetic.main.view_detail_head_toolbar1.tv_match_time
import kotlinx.android.synthetic.main.view_detail_head_toolbar1.tv_score
import kotlinx.android.synthetic.main.view_toolbar_detail_collaps.view.*
import kotlinx.android.synthetic.main.view_toolbar_detail_collaps1.*
import kotlinx.android.synthetic.main.view_toolbar_detail_collaps1.view.ivToolbarAwayLogo
import kotlinx.android.synthetic.main.view_toolbar_detail_collaps1.view.ivToolbarHomeLogo
import kotlinx.android.synthetic.main.view_toolbar_detail_live.*
import kotlinx.android.synthetic.main.view_toolbar_detail_live.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityDetailSportBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.matchLiveInfo.ChatLiveLoginData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.ui.maintab.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.ui.sport.detail.adapter.*
import org.cxct.sportlottery.ui.sport.detail.fragment.SportChartFragment
import org.cxct.sportlottery.ui.sport.detail.fragment.SportToolBarTopFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager
import splitties.bundle.put
import timber.log.Timber
import java.net.URLEncoder
import java.util.*


@Suppress("DEPRECATION", "SetTextI18n")
class SportDetailActivity : BaseBottomNavActivity<SportViewModel>(SportViewModel::class),
    TimerManager {
    //布局
    private val binding by lazy {
        ActivityDetailSportBinding.inflate(layoutInflater)
    }

    companion object {
        fun startActivity(
            context: Context,
            matchInfo: MatchInfo,
            matchType: MatchType? = null,
            intoLive: Boolean = false,
            tabCode: String? = null,
        ) {
            matchInfo.let {
                val intent = Intent(context, SportDetailActivity::class.java)
                intent.putExtra("matchInfo", matchInfo.toJson())
                intent.putExtra(
                    "matchType",
                    matchType
                        ?: if (TimeUtil.isTimeInPlay(it.startTime)) MatchType.IN_PLAY else MatchType.DETAIL
                )
                intent.putExtra("intoLive", intoLive)
                intent.putExtra("tabCode", tabCode)
                context.startActivity(intent)
            }
        }
    }


    private var matchType: MatchType = MatchType.DETAIL
    private var intoLive = false
    private var oddsDetailListAdapter: OddsDetailListAdapter? = null
    private var isLogin: Boolean = false
    private val tabCateAdapter: TabCateAdapter by lazy {
        TabCateAdapter(OnItemSelectedListener {
            tabCateAdapter.selectedPosition = it
            (rv_cat.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
                rv_cat, RecyclerView.State(), tabCateAdapter.selectedPosition
            )
            viewModel.oddsDetailResult.value?.peekContent()?.oddsDetailData?.matchOdd?.playCateTypeList?.getOrNull(
                it
            )?.code?.let { code ->
                checkSportGuideState(code)
                oddsDetailListAdapter?.notifyDataSetChangedByCode(code)
            }
        })
    }

    val tvToolBarHomeName by lazy { binding.collapsToolbar.tv_toolbar_home_name }
    val tvToolBarAwayName by lazy { binding.collapsToolbar.tv_toolbar_away_name }
    val ivToolbarHomeLogo by lazy { binding.collapsToolbar.ivToolbarHomeLogo }
    val ivToolbarAwayLogo by lazy { binding.collapsToolbar.ivToolbarAwayLogo }
    val tvToolbarHomeScore by lazy { binding.collapsToolbar.tv_toolbar_home_score }
    val tvToolbarAwayScore by lazy { binding.collapsToolbar.tv_toolbar_away_score }

    private val liveToolBarListener by lazy {
        object : DetailLiveViewToolbar.LiveToolBarListener {
            override fun onFullScreen(enable: Boolean) {
                if (enable) {
                    showFullScreen(enable)
                } else {
                    showFullScreen(enable)
                }
            }

            override fun onTabClick(position: Int) {
                showChatWebView(position == 0)
            }
        }
    }

    private var betListFragment: BetListFragment? = null
    private var matchOdd: MatchOdd? = null
    private var matchInfo: MatchInfo? = null

    //进来后默认切到指定tab
    private val tabCode by lazy { intent.getStringExtra("tabCode") }
    private var isFlowing = false
    private lateinit var topBarFragmentList: List<Fragment>
    private lateinit var sportToolBarTopFragment: SportToolBarTopFragment
    private lateinit var sportChartFragment: SportChartFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(binding.root)
        AndroidBug5497Workaround.assistActivity(this)
        initData()
        initToolBar()
        initAllObserve()
        initUI()
        initBottomNavigation()
    }


    override fun initToolBar() {
        iv_back.setOnClickListener {
            if (live_view_tool_bar.isFullScreen) {
                live_view_tool_bar.showFullScreen(false)
                showFullScreen(false)
            } else {
                if (vpContainer.isVisible) {
                    onBackPressed()
                } else {
                    if (intoLive) {
                        finish()
                    } else {
                        vpContainer.visible()
                        live_view_tool_bar.gone()
                        live_view_tool_bar.release()
                        collaps_toolbar.gone()
                        showChatWebView(false)
                        setScrollEnable(true)
                    }
                }
            }
        }

        if (matchInfo?.gameType == GameType.BB.name || matchInfo?.gameType == GameType.ES.name) {
            binding.detailToolBarViewPager.isUserInputEnabled = false
            binding.flRdContainer.gone()
        }

        topBarFragmentList = listOf<Fragment>(SportToolBarTopFragment().apply {
            arguments = Bundle().also {
                it.put("matchInfo", this@SportDetailActivity.matchInfo?.toJson())
            }
        }, SportChartFragment().apply {
            arguments = Bundle().also {
                it.put("matchInfo", this@SportDetailActivity.matchInfo?.toJson())
            }
        })
        sportToolBarTopFragment = topBarFragmentList[0] as SportToolBarTopFragment
        sportChartFragment = topBarFragmentList[1] as SportChartFragment

        binding.detailToolBarViewPager.adapter =
            DetailTopFragmentStateAdapter(this, topBarFragmentList.toMutableList())
        hIndicator.run {
            setIndicatorColor(
                context.getColor(R.color.color_FFFFFF), context.getColor(R.color.color_025BE8)
            )
            val height = 4.dp
            itemWidth = 10.dp
            itemHeight = height
            mRadius = itemWidth.toFloat()
            setSpacing(height)
            resetItemCount(2)
        }
        binding.detailToolBarViewPager.registerOnPageChangeCallback(object :
            OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.d("onPageSelectedListener:position:${position}")
                if (position == 1) {
                    sportChartFragment.notifyRcv()
                }
            }
        })
        flRdContainer.background = DrawableCreatorUtils.getCommonBackgroundStyle(
            14, solidColor = R.color.transparent_black_20
        )
        detailToolBarViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                hIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels)
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        })

        val isMatchFav = matchInfo?.isFavorite ?: false


        ivFavorite.setOnClickListener {
            viewModel.pinFavorite(
                FavoriteType.MATCH, matchInfo?.id
            )
        }

        viewModel.detailNotifyMyFavorite.observe(this) {
            if (it.first == (matchInfo?.id ?: "")) {
                ivFavorite.isSelected = it.second
            }
        }

        ivFavorite.isSelected = isMatchFav


        iv_refresh.setOnClickListener {
            initAllObserve()
        }

        ImmersionBar.with(this).fitsSystemWindows(false).statusBarDarkFont(true)
            .transparentStatusBar().hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR).init()

//        app_bar_layout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
//            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
//                if (state === State.COLLAPSED) {
//                    //折叠状态
//                    collaps_toolbar.visibility = View.VISIBLE
//                } else {
//                    if (live_view_tool_bar.isVisible) {
//                        collaps_toolbar.visibility = View.VISIBLE
//                    } else {
//                        collaps_toolbar.visibility = View.GONE
//                    }
//                }
//            }
//        })

    }

    fun showFullScreen(enable: Boolean) {
        if (enable) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            lin_center.isVisible = false
            llToolBar.gone()
            vpContainer.isVisible = false
            live_view_tool_bar.isVisible = true
            collaps_toolbar.isVisible = false
            clToolContent.gone()
            app_bar_layout.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            setScrollEnable(false)
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            lin_center.isVisible = true
            llToolBar.visible()
            vpContainer.isVisible = false
            live_view_tool_bar.isVisible = true
            clToolContent.visible()
            collaps_toolbar.isVisible = true
            app_bar_layout.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            setScrollEnable(true)
        }

        live_view_tool_bar.layoutParams.apply {
            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height =
                    if (enable) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
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
            scrollFlags = if (enable) (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL)
            else AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
        }
    }

    private fun setResetScrollEnable(enable: Boolean) {
        (app_bar_layout.getChildAt(0).layoutParams as AppBarLayout.LayoutParams).apply {
            scrollFlags =
                if (enable) (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP)
                else AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
        }
    }

    fun setupAnalyze(matchId: String) {
        initAnalyzeWV()
        wv_analyze.apply {
            settings.javaScriptEnabled = true

            webViewClient = WebViewClient()

            sConfigData?.analysisUrl?.replace(
                "{lang}",
                if (LanguageManager.getSelectLanguage(this@SportDetailActivity).key == LanguageManager.Language.PHI.key) {
                    LanguageManager.Language.EN.key
                } else {
                    LanguageManager.getSelectLanguage(this@SportDetailActivity).key
                }
            )

                ?.replace("{eventId}", matchId)?.let {
                    loadUrl(it)
                }
            setWebViewCommonBackgroundColor()
        }
    }


    private lateinit var wv_chat: WebView
    private lateinit var wv_analyze: WebView

    private fun initChatWV() {
        if (!::wv_chat.isInitialized) {
            wv_chat = WebView(this)
            val lp = FrameLayout.LayoutParams(-1, 60.dp)
            lp.gravity = Gravity.BOTTOM
            detailLayout.addView(wv_chat, lp)
        }
    }

    fun setupInput() {
        if (matchInfo?.roundNo.isNullOrEmpty()) {
            showChatWebView(false)
            return
        }
        if (sConfigData?.liveChatOpen == 0) {
            showChatWebView(false)
            return
        }

        initChatWV()
        wv_chat.apply {
            settings.apply {
                javaScriptEnabled = true
                useWideViewPort = true
                displayZoomControls = false
                textZoom = 100
                loadWithOverviewMode = true
            }
            setWebViewCommonBackgroundColor()
            webViewClient = WebViewClient()
            addJavascriptInterface(
                JavaScriptObject(this@SportDetailActivity), "__oi"
            )
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?,
                ): Boolean {
                    return true
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
        }
        if (viewModel.isLogin.value == true) {
            viewModel.loginLive()
        } else {
            sConfigData?.liveChatHost?.let { host ->
                loginChat(host, null)
            }
        }
    }

    private fun setupLiveView(liveVideo: Int?) {
        with(live_view_tool_bar) {
            setupToolBarListener(liveToolBarListener)
        }
    }

    fun loginChat(host: String, chatLiveLoginData: ChatLiveLoginData?) {
        if (chatLiveLoginData == null) {
            var builder = StringBuilder("$host?")
            builder.append("device=android")
            builder.append("&lang=" + LanguageManager.getSelectLanguage(this).key)
            LogUtil.d("builder=$builder")
            wv_chat.loadUrl(builder.toString())
        } else {
            var builder = StringBuilder("$host?")
            builder.append("room=" + matchInfo?.roundNo)
            builder.append("&uid=" + chatLiveLoginData.userData?.userId)
            builder.append("&token=" + URLEncoder.encode(chatLiveLoginData.liveToken))
            builder.append("&role=" + 1)
            builder.append("&device=android")
            builder.append("&lang=" + LanguageManager.getSelectLanguage(this).key)
            LogUtil.d("builder=$builder")
            wv_chat.loadUrl(builder.toString())
        }
        Log.d("hjq", "loginChat=" + host)
    }

    fun showChatWebView(visible: Boolean) {
        if (visible) {
            initChatWV()
            wv_chat.isVisible = true
        } else {
            if (::wv_chat.isInitialized) {
                wv_chat.isVisible = false
            }
        }
    }

    fun updateWebHeight(onMini: Boolean) {
        wv_chat.post {
            var lp = wv_chat.layoutParams
            lp.height = if (onMini) 60.dp else ConstraintLayout.LayoutParams.MATCH_PARENT
            wv_chat.layoutParams = lp
        }

    }

    private fun initAnalyzeWV() {
        if (!::wv_analyze.isInitialized) {
            wv_analyze = WebView(this)
            wv_analyze.isNestedScrollingEnabled = false
            ns_analyze.addView(wv_analyze, FrameLayout.LayoutParams(-1, -1))
        }
    }

    private fun releaseWebView() {
        if (::wv_analyze.isInitialized) {
            wv_analyze.destroy()
        }
        if (::wv_chat.isInitialized) {
            wv_chat.destroy()
        }
    }


    //注入JavaScript的Java类
    inner class JavaScriptObject(val activity: SportDetailActivity) {
        @JavascriptInterface
        fun notify2arg(action: String, data: Boolean) {
            LogUtil.d("notify2arg=" + action + "," + data)
            when (action) {
                "onMini" -> {
                    activity.runOnUiThread {
                        activity.onMini = data
                        updateWebHeight(data)
                        activity.setUpBetBarVisible()
                    }
                }

                "onEmoji" -> {
                    activity.runOnUiThread {
                        activity.showEmoji = data
                        activity.setUpBetBarVisible()
                    }
                }

                "requireLogin" -> {
                    activity.runOnUiThread {
                        activity.startLogin()
                    }
                }
            }
        }
    }

    override fun initMenu() {
    }

    override fun clickMenuEvent() {
    }

    override fun initBottomNavigation() {
//
        binding.parlayFloatWindow.setBetText(getString(R.string.bet_slip))
        binding.parlayFloatWindow.onViewClick = {
            showBetListPage()
        }
    }

    private fun updateMenu(matchInfo: MatchInfo) {
        val setBg: (TextView, ImageView, Int, Boolean) -> Unit = { tv, iv, drawable, isHave ->
            if (isHave) {
                tv.setTextColor(getColor(R.color.color_000000))
                AppCompatResources.getDrawable(this, drawable)?.let {
                    DrawableCompat.setTint(it, getColor(R.color.color_025BE8))
                    iv.setImageDrawable(it)
                }
            } else {
                tv.setTextColor(getColor(R.color.color_BEC7DC))
                AppCompatResources.getDrawable(this, drawable)?.let {
                    DrawableCompat.setTint(it, getColor(R.color.color_BEC7DC))
                    iv.setImageDrawable(it)
                }
            }
        }

        val showLive = matchInfo.isLive == 1
        setBg(
            binding.tvLiveStream, binding.ivLiveStream, R.drawable.icon_live_stream, showLive
        )

        val showVideo = matchInfo.liveVideo == 1
        setBg(
            binding.tvVideo, binding.ivVideo, R.drawable.icon_video, matchInfo.liveVideo == 1
        )

        val showAnim =
            !(matchInfo.trackerId.isNullOrEmpty()) && MultiLanguagesApplication.getInstance()
                ?.getGameDetailAnimationNeedShow() == true
        setBg(
            binding.tvAnim, binding.ivAnim, R.drawable.icon_animation, showAnim
        )



        if (showLive) {
            setOnClickListeners(binding.ivLiveStream, binding.tvLiveStream) {
                if (!viewModel.getLoginBoolean() && sConfigData?.noLoginWitchVideoOrAnimation == 1) {
                    AppManager.currentActivity().startLogin()
                    return@setOnClickListeners
                }
                setResetScrollEnable(true)
                showLive()
            }

            if (matchInfo.pullRtmpUrl.isNullOrEmpty()) {
                matchInfo.roundNo?.let {
                    viewModel.getLiveInfo(it)
                }
            } else {
                live_view_tool_bar.liveUrl = matchInfo.pullRtmpUrl
                if (intoLive) {
                    live_view_tool_bar.showLive()
                }
            }
        }

        if (showVideo) {
            setOnClickListeners(binding.ivVideo, binding.tvVideo) {
                if (!viewModel.getLoginBoolean() && sConfigData?.noLoginWitchVideoOrAnimation == 1) {
                    AppManager.currentActivity().startLogin()
                    return@setOnClickListeners
                }
                setResetScrollEnable(true)
                live_view_tool_bar.videoUrl?.let {
                    binding.vpContainer.gone()
                    live_view_tool_bar.visible()
                    collaps_toolbar.visible()
                    live_view_tool_bar.showVideo()
//                setScrollEnable(false)
                }
            }
        }


        if (showAnim) {
            setOnClickListeners(binding.ivAnim, binding.tvAnim) {
                if (!viewModel.getLoginBoolean() && sConfigData?.noLoginWitchVideoOrAnimation == 1) {
                    AppManager.currentActivity().startLogin()
                    return@setOnClickListeners
                }
                setResetScrollEnable(true)
                live_view_tool_bar.animeUrl?.let {
                    binding.vpContainer.gone()
                    live_view_tool_bar.visible()
                    collaps_toolbar.visible()
//                    collaps_toolbar.iv_toolbar_bg.isVisible = false
                    live_view_tool_bar.showAnime()
//                startDelayHideTitle()
//                setScrollEnable(false)
                }
            }
        }

    }

    override fun showBetListPage() {
        betListFragment = BetListFragment.newInstance(object : BetListFragment.BetResultListener {
            override fun onBetResult(
                betResultData: Receipt?,
                betParlayList: List<ParlayOdd>,
                isMultiBet: Boolean,
            ) {
                showBetReceiptDialog(betResultData, betParlayList, isMultiBet, R.id.fl_bet_list)
            }

        })
        supportFragmentManager.beginTransaction().add(R.id.fl_bet_list, betListFragment!!)
            .addToBackStack(BetListFragment::class.java.simpleName).commit()
    }

    override fun updateUiWithLogin(isLogin: Boolean) {
    }

    override fun updateOddsType(oddsType: OddsType) {
    }

    override fun updateBetListCount(num: Int) {
        setUpBetBarVisible()
        binding.parlayFloatWindow.tv_bet_list_count.text = num.toString()
        Timber.e("num: $num")
        if (num > 0) viewModel.getMoneyAndTransferOut()
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            setAnchorView(R.id.snackbar_holder)
            show()
        }
    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
        snackBarMyFavoriteNotify?.apply {
            setAnchorView(R.id.snackbar_holder)
            show()
        }

    }

    override fun navOneSportPage(thirdGameCategory: ThirdGameCategory?) {

    }

    private fun initData() {
        clickButton()
        matchInfo = intent.getStringExtra("matchInfo")?.fromJson<MatchInfo>()
        matchType = intent.getSerializableExtra("matchType") as MatchType
        intoLive = intent.getBooleanExtra("intoLive", false)
        matchInfo?.let {
            tv_game_title.text = it.leagueName
            updateMenu(it)
        }
        tabCode?.let {
            checkSportGuideState(it)
        }
    }

    override var startTime: Long = 0
    override var timer: Timer = Timer()
    var isGamePause = false

    override var timerHandler: Handler = Handler(Looper.myLooper()!!) {
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
                    GameType.BB.key, GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.BM.key -> true
                    else -> {
                        false
                    }
                }
            ) {
                sportToolBarTopFragment.tv_match_time.isVisible = false
                cancelTimer()
                return@Handler false
            }
            sportToolBarTopFragment.tv_match_time.apply {
                if (needCountStatus(
                        matchOdd?.matchInfo?.socketMatchStatus, matchOdd?.matchInfo?.leagueTime
                    )
                ) {
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
//                collaps_toolbar.tv_toolbar_match_time.text = text
//                collaps_toolbar.tv_toolbar_match_time.isVisible = isVisible
            }
        }
        return@Handler false
    }


    override fun onResume() {
        super.onResume()
        startTimer()
    }

    override fun onPause() {
        super.onPause()
        live_view_tool_bar.stopPlayer()
        cancelTimer()
    }

    override fun onStop() {
        super.onStop()
        unSubscribeChannelEventAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWebView()
        viewModel.clearLiveInfo()
        live_view_tool_bar.release()

    }


    private fun initUI() {
        oddsDetailListAdapter =
            OddsDetailListAdapter(OnOddClickListener { odd, oddsDetail, scoPlayCateNameForBetInfo ->
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
            }).apply {
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
            oddsDetailListAdapter?.setPreloadItem()
        }

        rv_cat.apply {
            adapter = tabCateAdapter
            layoutManager =
                ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator?.changeDuration = 0
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
        }
        iv_arrow.apply {
            isSelected = true
            setOnClickListener {
                isSelected = !isSelected
                oddsDetailListAdapter?.oddsDetailDataList?.forEach {
                    it.isExpand = isSelected
                }
                oddsDetailListAdapter?.notifyDataSetChanged()
            }
        }
        matchInfo?.id?.let {
            setupAnalyze(it)
            setupInput()
        }
        isShowOdd(true)
//        initAnim()
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun initObserve() {

        viewModel.matchLiveInfo.observe(this) {
            it?.peekContent()?.let { matchRound ->
                if (lin_live.isVisible) {
                    live_view_tool_bar.liveUrl =
                        if (matchRound.pullRtmpUrl.isNotEmpty()) matchRound.pullRtmpUrl else matchRound.pullFlvUrl
                    if (intoLive) {
                        showLive()
                    }
                }
            }
        }

        viewModel.isLogin.observe(this) {
            setupInput()
        }

        viewModel.notifyLogin.observe(this) {
            showLoginNotify()
        }

        viewModel.userInfo.observe(this) { userInfo ->
            oddsDetailListAdapter?.discount = userInfo?.discount ?: 1.0F
        }

        viewModel.videoUrl.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { url ->
                if (lin_video.isVisible) {
                    live_view_tool_bar.videoUrl = url
                }
            }
        }

        viewModel.animeUrl.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { url ->
                if (lin_anime.isVisible) {
                    live_view_tool_bar.animeUrl = url
                }
            }
        }

        viewModel.showBetInfoSingle.observe(this) {
            it.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }

        viewModel.liveLoginInfo.observe(this) {
            it.getContentIfNotHandled()?.let {
                sConfigData?.liveChatHost?.let { host ->
                    loginChat(host, it)
                }
            }
        }

        viewModel.oddsDetailResult.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                if (result.success) {
                    result.setupPlayCateTab()
                    try {
                        val selectedPosition = tabCateAdapter.selectedPosition
                        val dataList = tabCateAdapter.dataList
                        if (selectedPosition < dataList.size) {
                            oddsDetailListAdapter?.notifyDataSetChangedByCode(dataList[selectedPosition].code)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return@observe
                    }
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
                        tv_toolbar_home_name.text = matchInfo.homeName ?: ""
                        tv_toolbar_away_name.text = matchInfo.awayName ?: ""
                        sportToolBarTopFragment.setupMatchInfo(matchInfo, true)
                        sportChartFragment.updateMatchInfo(matchInfo)
                    }
                    setupLiveView(result.oddsDetailData?.matchOdd?.matchInfo?.liveVideo)
                } else {
                    showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                }
            }
        }

        viewModel.oddsDetailList.observe(this) {
            it.peekContent().let { list ->
                if (list.isNotEmpty()) {
                    oddsDetailListAdapter?.removePreloadItem()
                    oddsDetailListAdapter?.oddsDetailDataList = list
                }
            }
        }

        viewModel.betInfoList.observe(this) {
            it.peekContent().let { list ->
                oddsDetailListAdapter?.betInfoList = list
            }
        }


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
                        epsSize, oddsDetailListAdapter.oddsDetailDataList.removeAt(
                            oddsDetailListAdapter.oddsDetailDataList.indexOf(
                                it
                            )
                        )
                    )
                }
                oddsDetailListAdapter.notifyDataSetChanged()
            }
        }



        viewModel.showBetUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true) {
                showSnackBarBetUpperLimitNotify(
                    getString(R.string.bet_notify_max_limit)
                ).setAnchorView(R.id.parlayFloatWindow).show()
            }
        }
        viewModel.showBetBasketballUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true) {
                showSnackBarBetUpperLimitNotify(
                    getString(R.string.bet_basketball_notify_max_limit)
                ).setAnchorView(R.id.parlayFloatWindow).show()
            }
        }

    }

    fun showLive() {
        live_view_tool_bar.liveUrl?.let {
            live_view_tool_bar.isVisible = true
            collaps_toolbar.isVisible = true
            vpContainer.isVisible = false
            live_view_tool_bar.showLive()
            showChatWebView(true)
        }
    }


    /**
     * 点击事件
     */
    fun clickButton() {
//        btn_odd.setOnClickListener { isShowOdd(true) }
//        btn_analyze.setOnClickListener { isShowOdd(false) }

    }

    private fun isShowOdd(isShowOdd: Boolean) {
        val selectColor = ContextCompat.getColor(this, R.color.color_025BE8)
        val nomalColor = ContextCompat.getColor(this, R.color.color_6C7BA8)
//        btn_odd.setTextColor(if (isShowOdd) selectColor else nomalColor)
//        viewBtOdd.isVisible = isShowOdd
        rv_detail.isVisible = isShowOdd
        lin_categroy.isVisible = isShowOdd
        vDivider.isVisible = isShowOdd

//        btn_analyze.setTextColor(if (!isShowOdd) selectColor else nomalColor)
//        viewBtnAnalyze.isVisible = !isShowOdd
        ns_analyze.isVisible = !isShowOdd

    }


    private fun initSocketObserver() {
        unSubscribeChannelHallAll()
        unSubscribeChannelEventAll()
        setupSportStatusChange(this) {
            if (it) {
                finish()
            }
        }
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
                            unSubscribeChannelEvent(matchId)
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
                                tv_toolbar_home_name.text = matchInfo?.homeName ?: ""
                                tv_toolbar_away_name.text = matchInfo?.awayName ?: ""
                                sportToolBarTopFragment.setupMatchInfo(matchOdd.matchInfo)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    sportChartFragment.updateMatchInfo(matchOdd.matchInfo)
                                },300)
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
                matchInfo?.let {
                    SocketUpdateUtil.updateMatchInfoClockByDetail(it, matchClockEvent)
                }
            }
        }

        receiver.matchOddsChange.observe(this) {
            it?.getContentIfNotHandled()?.let { matchOddsChangeEvent ->
                oddsDetailListAdapter?.oddsDetailDataList?.let { oddsDetailListDataList ->
                    SocketUpdateUtil.updateMatchOddsMap(oddsDetailListDataList,
                        matchOddsChangeEvent,
                        viewModel.favorPlayCateList.value?.find { playCate -> playCate.gameType == matchInfo?.gameType })
                        ?.let { updatedDataList ->
                            oddsDetailListAdapter?.oddsDetailDataList = updatedDataList
                        } ?: run {
                        var needUpdate = false
                        oddsDetailListDataList.forEachIndexed { index, oddsDetailListData ->
                            if (SocketUpdateUtil.updateMatchOdds(
                                    oddsDetailListData, matchOddsChangeEvent
                                ) && oddsDetailListData.isExpand
                            ) {
                                needUpdate = true
                                updateBetInfo(oddsDetailListData, matchOddsChangeEvent)
                            }
                        }
                        if (needUpdate) {
                            oddsDetailListAdapter?.notifyDataSetChanged()
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
                            oddsDetailListData, globalStopEvent
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
                    indexOf(find { date ->
                        date.gameType == it.playCateCode //命名待優化 此處gameType並非球種 而為玩法code
                    }?.apply {
                        this.oddArrayList.forEach { odd ->
                            odd?.status = BetStatus.DEACTIVATED.code
                        }
                    }).let { index ->
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
        return betListFragment?.isVisible ?: false
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
            //如果是从篮球末位比分进入，拿到数据后，自动切换到篮球末位比分到tab下
            if (tabCateAdapter.dataList.isEmpty() && tabCode == MatchType.END_SCORE.postValue) {
                playCateTypeList.indexOfFirst { it.code == tabCode }.let {
                    if (it >= 0) {
                        tabCateAdapter.selectedPosition = it
                    }
                }
            }
            tabCateAdapter.dataList = playCateTypeList
            (rv_cat.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
                rv_cat, RecyclerView.State(), tabCateAdapter.selectedPosition
            )
        } else {
            rv_cat.visibility = View.GONE
        }
    }


    /** 初始化监听 **/
    private fun initAllObserve() {
        initObserve()
        initSocketObserver()
    }


    var showEmoji = false
    var onMini = true
    fun setUpBetBarVisible() {
        binding.parlayFloatWindow.isVisible = !BetInfoRepository.betInfoList.value?.peekContent()
            .isNullOrEmpty() && (!showEmoji && onMini)
    }


    /**
     * 检查篮球末尾比分 新手引导是否已经展示过了
     */
    private fun checkSportGuideState(code: String) {
        if (code == MatchType.END_SCORE.postValue) {
            if (KvUtils.decodeBooleanTure(KvUtils.BASKETBALL_GUIDE_TIP_FLAG, false)) {
                dsgView.visibility = GONE
            } else {
                dsgView.visibility = VISIBLE
            }
        }
    }


}
