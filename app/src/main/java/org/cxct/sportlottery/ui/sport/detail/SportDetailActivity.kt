package org.cxct.sportlottery.ui.sport.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_detail_sport.*
import kotlinx.android.synthetic.main.activity_detail_sport.fl_bet_list
import kotlinx.android.synthetic.main.activity_detail_sport.parlayFloatWindow
import kotlinx.android.synthetic.main.activity_main_tab.*
import kotlinx.android.synthetic.main.bet_bar_layout.view.*
import kotlinx.android.synthetic.main.content_baseball_status.*
import kotlinx.android.synthetic.main.fragment_sport_list2.view.*
import kotlinx.android.synthetic.main.view_toolbar_detail_collaps1.*
import kotlinx.android.synthetic.main.view_toolbar_detail_collaps1.view.*
import kotlinx.android.synthetic.main.view_toolbar_detail_live.*
import kotlinx.android.synthetic.main.view_toolbar_detail_live.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.*
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
import org.cxct.sportlottery.service.MatchOddsRepository
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.ui.maintab.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.ui.sport.detail.adapter.*
import org.cxct.sportlottery.ui.sport.detail.fragment.SportChartFragment
import org.cxct.sportlottery.ui.sport.detail.fragment.SportToolBarTopFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.view.DetailSportGuideView
import org.cxct.sportlottery.view.DividerItemDecorator
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
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
            matchInfo: MatchInfo?=null,
            matchId: String? = null,
            matchType: MatchType? = null,
            intoLive: Boolean = false,
            tabCode: String? = null,
        ) {
            val intent = Intent(context, SportDetailActivity::class.java)
            matchInfo?.let {
                intent.putExtra("matchInfo", it.toJson())
            }
            matchId?.let {
                intent.putExtra("matchId", it)
            }
            intent.putExtra("matchType", when{
                matchType!=null -> matchType
                matchInfo!=null&&TimeUtil.isTimeInPlay(matchInfo.startTime) -> MatchType.IN_PLAY
                else->MatchType.DETAIL
            })
            intent.putExtra("intoLive", intoLive)
            intent.putExtra("tabCode", tabCode)
            context.startActivity(intent)
        }
    }


    private var matchType: MatchType = MatchType.DETAIL
    private var intoLive = false
    private var oddsDetailListAdapter: OddsDetailListAdapter? = null
    private val tabCateAdapter:  TabCateAdapter by lazy {
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
    val tvToolbarNoStart by lazy { binding.collapsToolbar.linNoStart }

    private val liveToolBarListener by lazy {
        object : DetailLiveViewToolbar.LiveToolBarListener {
            override fun onFullScreen(enable: Boolean) {
                if (enable) {
                    showFullScreen(enable)
                } else {
                    showFullScreen(enable)
                }
            }


            override fun onClose(){
                avoidFastDoubleClick()
                iv_back.performClick()
            }
        }
    }

    private var betListFragment: BetListFragment? = null
    private var matchOdd: MatchOdd? = null
    private var matchInfo: MatchInfo? = null

    //进来后默认切到指定tab
    private val tabCode by lazy { intent.getStringExtra("tabCode") }
    private val matchId by lazy { intent.getStringExtra("matchId") }
    private lateinit var topBarFragmentList: List<Fragment>
    private lateinit var sportToolBarTopFragment: SportToolBarTopFragment
    private lateinit var sportChartFragment: SportChartFragment

    private var delayObserver: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initData()
        initToolBar()
        initUI()
        initBottomNavigation()
        getData()
        bindSportMaintenance()

        /**
         * 在observer的回调中有直接通过id方式访问SportToolBarTopFragment中的view,
         * 如果网络回调较快主线程出现卡顿由SportToolBarTopFragment还没完成相关View的创建并加到Activity的视图树中，此时就会出现空指针
         * 所以这里延迟订阅网络回调，避免该问题发生
         */
        delayObserver = Runnable {
            initAllObserve()
            delayObserver = null
        }
        binding.root.postDelayed(delayObserver, 300)
    }


    override fun initToolBar() = binding.run {
        setStatusbar(R.color.color_FFFFFF,true)
        ivBack.setOnClickListener {
            if (liveViewToolBar.isFullScreen) {
                liveViewToolBar.showFullScreen(false)
                showFullScreen(false)
            } else {
                if (vpContainer.isVisible) {
                    onBackPressed()
                } else {
                    if (intoLive) {
                        finish()
                    } else {
                        selectMenuTab(-1)
                        vpContainer.visible()
                        liveViewToolBar.stopPlay()
                        liveViewToolBar.gone()
                        binding.collapsToolbar.gone()
                        setScrollEnable(true)
                    }
                }
            }
        }
        //滚球中并且指定球类显示比分板
        val needShowBoard = TimeUtil.isTimeInPlay(matchInfo?.startTime)&&when(matchInfo?.gameType){
            GameType.FT.key, GameType.BK.key, GameType.TN.key, GameType.BM.key, GameType.TT.key, GameType.VB.key->true
            else->false
        }
        binding.detailToolBarViewPager.isUserInputEnabled = needShowBoard
        binding.flRdContainer.isVisible = needShowBoard

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
            DetailTopFragmentStateAdapter(this@SportDetailActivity, topBarFragmentList.toMutableList())
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
            }
        })

        ivFavorite.setOnClickListener {
            viewModel.pinFavorite(FavoriteType.MATCH, matchInfo?.id)
        }

        viewModel.detailNotifyMyFavorite.observe(this@SportDetailActivity) {
            if (it.first == (matchInfo?.id ?: "")) {
                ivFavorite.isSelected = it.second
            }
        }

        ivFavorite.isSelected = matchInfo?.isFavorite ?: false
        binding.ivRefresh.setOnClickListener {
            it.isEnabled = false
            removeObserver()  // 订阅之前移除之前的订阅
            initObserve() // 之前的逻辑，重新订阅
            it.rotationAnimation(it.rotation + 720f, 1000) { it.isEnabled = true}
        }

        binding.appBarLayout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            private var first = true
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                if (state === State.COLLAPSED) { //折叠状态
                    binding.collapsToolbar.visible()
                    if (first) { // 第一次折叠的时候底部会被挡住一截，这里以曲线救国的方式简单解决改问题
                        first = false
                        binding.rvDetail.setPadding(0, 0, 0, 40.dp)
                        binding.rvDetail.postDelayed({ binding.rvDetail.setPadding(0, 0, 0, 0) }, 400)
                    }
                } else {
                    binding.collapsToolbar.visibility = binding.liveViewToolBar.visibility
                }
            }
        })

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


        live_view_tool_bar.updateLayoutParams {
            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height =
                    if (enable) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
            } else {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

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



    private fun setupLiveView() {
        live_view_tool_bar.setupToolBarListener(liveToolBarListener)
    }

    private fun loginChat(host: String, chatLiveLoginData: ChatLiveLoginData?) {
        if (chatLiveLoginData == null) {
            var builder = StringBuilder("$host?")
            builder.append("device=android")
            builder.append("&lang=" + LanguageManager.getSelectLanguage(this).key)
            LogUtil.d("builder=$builder")
//            wv_chat.loadUrl(builder.toString())
        } else {
            var builder = StringBuilder("$host?")
            builder.append("room=" + matchInfo?.roundNo)
            builder.append("&uid=" + chatLiveLoginData.userData?.userId)
            builder.append("&token=" + URLEncoder.encode(chatLiveLoginData.liveToken))
            builder.append("&role=" + 1)
            builder.append("&device=android")
            builder.append("&lang=" + LanguageManager.getSelectLanguage(this).key)
            LogUtil.d("builder=$builder")
//            wv_chat.loadUrl(builder.toString())
        }
        Log.d("hjq", "loginChat=" + host)
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
        val setBg: (TextView, ImageView, Boolean) -> Unit = { tv, iv , isHave ->
            if (isHave) {
                tv.setTextColor(getColor(R.color.color_000000))
            } else {
                tv.setTextColor(getColor(R.color.color_BEC7DC))
            }
            iv.isEnabled = isHave
        }

        val showLive = matchInfo.isLive == 1
        setBg(
            binding.tvLiveStream, binding.ivLiveStream, showLive
        )

        val showVideo = matchInfo.liveVideo == 1
        setBg(
            binding.tvVideo, binding.ivVideo, matchInfo.liveVideo == 1
        )

        val showAnim =
            !(matchInfo.trackerId.isNullOrEmpty()) && MultiLanguagesApplication.getInstance()
                ?.getGameDetailAnimationNeedShow() == true
        setBg(
            binding.tvAnim, binding.ivAnim, showAnim
        )



        if (showLive) {
            setOnClickListeners(binding.ivLiveStream, binding.tvLiveStream) {
                if (binding.ivLiveStream.isSelected) {
                    return@setOnClickListeners
                }
                if (!viewModel.getLoginBoolean() && sConfigData?.noLoginWitchVideoOrAnimation == 1) {
                    AppManager.currentActivity().startLogin()
                    return@setOnClickListeners
                }
                selectMenuTab(0)
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
                if (binding.ivVideo.isSelected) {
                    return@setOnClickListeners
                }
                if (!viewModel.getLoginBoolean() && sConfigData?.noLoginWitchVideoOrAnimation == 1) {
                    AppManager.currentActivity().startLogin()
                    return@setOnClickListeners
                }
                selectMenuTab(1)
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
                if (binding.ivAnim.isSelected) {
                    return@setOnClickListeners
                }
                if (!viewModel.getLoginBoolean() && sConfigData?.noLoginWitchVideoOrAnimation == 1) {
                    AppManager.currentActivity().startLogin()
                    return@setOnClickListeners
                }
                selectMenuTab(2)
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
        binding.parlayFloatWindow.updateCount(num.toString())
        Timber.e("num: $num")
        if (num > 0) viewModel.getMoneyAndTransferOut()
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            anchorView = binding.snackbarHolder
            show()
        }
    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
        snackBarMyFavoriteNotify?.apply {
            anchorView = binding.snackbarHolder
            show()
        }

    }

    override fun navOneSportPage(thirdGameCategory: ThirdGameCategory?) {

    }

    private fun initData() {
        matchInfo = intent.getStringExtra("matchInfo")?.fromJson<MatchInfo>()
        matchType = intent.getSerializableExtra("matchType") as MatchType
        intoLive = intent.getBooleanExtra("intoLive", false)
        matchInfo?.let {
            tv_game_title.text = it.leagueName
            startTime = (it.leagueTime?:0).toLong()
            if (it.gameType == GameType.ES.key){
                setESportTheme()
            }
            updateMenu(it)
        }
        tabCode?.let {
            checkSportGuideState(it)
        }
    }

    override var startTime: Long = 0
    override var timer: Timer = Timer()
    var isGamePause = false

    override var timerHandler: Handler = Handler(Looper.getMainLooper()) {
        var timeMillis = startTime * 1000L
        if (!TimeUtil.isTimeInPlay(matchOdd?.matchInfo?.startTime)) {
            return@Handler false
        }

        if (!isGamePause&&startTime>0) {
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
        if (matchInfo?.gameType == GameType.BB.key
            || matchInfo?.gameType == GameType.TN.key
            || matchInfo?.gameType == GameType.VB.key
            || matchInfo?.gameType == GameType.TT.key
            || matchInfo?.gameType == GameType.BM.key) {

            sportToolBarTopFragment.setMatchTimeEnable(false)
            cancelTimer()
            return@Handler false
        }
       val needCount= needCountStatus(matchInfo?.socketMatchStatus, matchInfo?.leagueTime)
        if (needCount) {
            if (timeMillis >= 1000) {
                sportToolBarTopFragment.updateMatchTime(TimeUtil.longToMmSs(timeMillis))
                sportToolBarTopFragment.setMatchTimeEnable(true)
                startTime = timeMillis / 1000L
            } else {
                sportToolBarTopFragment.updateMatchTime(getString(R.string.time_null))
                sportToolBarTopFragment.setMatchTimeEnable(false)
            }
        } else {
            sportToolBarTopFragment.updateMatchTime(getString(R.string.time_null))
            sportToolBarTopFragment.setMatchTimeEnable(false)
        }

        return@Handler false
    }


    override fun onResume() {
        super.onResume()
        matchInfo?.let {
            subscribeChannelEvent(it.id)
        }
        startTimer()
    }

    override fun onPause() {
        super.onPause()
        live_view_tool_bar.stopPlay()
        cancelTimer()
    }

    override fun onStop() {
        super.onStop()
        unSubscribeChannelEventAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearLiveInfo()
        live_view_tool_bar.release()
        delayObserver?.let { binding.root.removeCallbacks(it) }
    }


    private fun initUI() {
        oddsDetailListAdapter =
            OddsDetailListAdapter(OnOddClickListener { odd, oddsDetail, scoPlayCateNameForBetInfo ->
                if (mIsEnabled) {
                    avoidFastDoubleClick()
                    scoPlayCateNameForBetInfo?.let {
                        odd.spread = tranByPlayCode(this,odd.playCode, null,null,null)
                    }
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
            if (itemDecorationCount==0) {
                addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_4))
            }
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            adapter = oddsDetailListAdapter
            setLinearLayoutManager()
            oddsDetailListAdapter?.setPreloadItem()
        }

        rv_cat.apply {
            adapter = tabCateAdapter
            layoutManager =
                ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator?.changeDuration = 0
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
        }
        linArrow.apply {
            isSelected = true
            binding.ivArrow.rotationAnimation(0f)
            setOnClickListener {
                isSelected = !isSelected
                if (isSelected){
                    binding.ivArrow.rotationAnimation(0f)
                }else{
                    binding.ivArrow.rotationAnimation(180f)
                }
                oddsDetailListAdapter?.oddsDetailDataList?.forEach {
                    it.isExpand = isSelected
                }
                oddsDetailListAdapter?.notifyDataSetChanged()
            }
        }
        isShowOdd(true)
//        initAnim()

        rv_detail.setupBackTop(ivBackTop, 300.dp,tabCode)
    }

    private fun removeObserver() {
        viewModel.matchLiveInfo.removeObservers(this)
        viewModel.isLogin.removeObservers(this)
        viewModel.notifyLogin.removeObservers(this)
        viewModel.userInfo.removeObservers(this)
        viewModel.videoUrl.removeObservers(this)
        viewModel.animeUrl.removeObservers(this)
        viewModel.showBetInfoSingle.removeObservers(this)
        viewModel.oddsDetailResult.removeObservers(this)
        viewModel.oddsDetailList.removeObservers(this)
        viewModel.betInfoList.removeObservers(this)
        viewModel.oddsType.removeObservers(this)
        viewModel.favorPlayCateList.removeObservers(this)
        viewModel.showBetUpperLimit.removeObservers(this)
        viewModel.showBetBasketballUpperLimit.removeObservers(this)

    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun initObserve() {

        viewModel.matchLiveInfo.observe(this) {
            it?.peekContent()?.let { matchRound ->
                live_view_tool_bar.liveUrl =
                    if (matchRound.pullRtmpUrl.isNotEmpty()) matchRound.pullRtmpUrl else matchRound.pullFlvUrl
                if (intoLive) {
                    showLive()
                }
            }
        }

        viewModel.notifyLogin.observe(this) {
            showLoginNotify()
        }

        viewModel.userInfo.observe(this) { userInfo ->
            oddsDetailListAdapter?.discount = userInfo?.discount ?: 1.0F
        }

        viewModel.videoUrl.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { live_view_tool_bar.videoUrl = it }
        }

        viewModel.animeUrl.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { live_view_tool_bar.animeUrl = it }
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
            val result = it?.getContentIfNotHandled() ?: return@observe
            if (!result.success) {
                showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                return@observe
            }

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

            rv_detail.post { rv_detail.requestLayout() }
            matchOdd = result.oddsDetailData?.matchOdd

            result.oddsDetailData?.matchOdd?.matchInfo?.let { matchInfo ->
                if (this.matchInfo==null){
                    this.matchInfo = matchInfo
                }
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
                sportToolBarTopFragment.updateMatchInfo(matchInfo, true)
                if (matchId!=null){
                    tv_game_title.text = matchInfo.leagueName
                    updateMenu(matchInfo)
                    ivFavorite.isSelected = matchInfo.isFavorite
                    oddsDetailListAdapter?.sportCode = GameType.getGameType(matchInfo?.gameType)
                    oddsDetailListAdapter?.notifyDataSetChanged()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    sportChartFragment.updateMatchInfo(matchInfo)
                }, 300)
            }
            setupLiveView()
        }

        viewModel.oddsDetailList.observe(this) {
            it.peekContent().let { list ->
                if (list.isNotEmpty()) {
                    oddsDetailListAdapter?.removePreloadItem()
                    //如果是末位比分，小节比分就折叠起来
                    if (tabCode == MatchType.END_SCORE.postValue){
                        list.filter { it.gameType.isEndScoreType()}?.forEachIndexed { index, oddsDetailListData ->
                            oddsDetailListData.isExpand = (index==0)
                        }
                    }
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
        }
    }


    private fun isShowOdd(isShowOdd: Boolean) {
        rv_detail.isVisible = isShowOdd
        lin_categroy.isVisible = isShowOdd
        vDivider.isVisible = isShowOdd
    }


    private fun initSocketObserver() {
        unSubscribeChannelHallAll()
//        unSubscribeChannelEventAll()
        setupSportStatusChange(this) {
            if (it) {
                finish()
            }
        }

        MatchOddsRepository.observerMatchStatus(this) { matchStatusChangeEvent->
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
                            sportToolBarTopFragment.updateMatchInfo(matchOdd.matchInfo)
                            Handler(Looper.getMainLooper()).postDelayed({
                                sportChartFragment.updateMatchInfo(matchOdd.matchInfo)
                            }, 300)
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
        MatchOddsRepository.observerMatchOdds(this) {
                oddsDetailListAdapter?.oddsDetailDataList?.let { oddsDetailListDataList ->
                    SocketUpdateUtil.updateMatchOddsMap(oddsDetailListDataList,
                        it,
                        viewModel.favorPlayCateList.value?.find { playCate -> playCate.gameType == matchInfo?.gameType })
                        ?.let { updatedDataList ->
                            oddsDetailListAdapter?.oddsDetailDataList = updatedDataList
                        } ?: run {
                        var needUpdate = false
                        oddsDetailListDataList.forEachIndexed { index, oddsDetailListData ->
                            if (SocketUpdateUtil.updateMatchOdds(
                                    oddsDetailListData, it
                                ) && oddsDetailListData.isExpand
                            ) {
                                needUpdate = true
                                updateBetInfo(oddsDetailListData, it)
                            }
                        }
                        if (needUpdate) {
                            oddsDetailListAdapter?.notifyDataSetChanged()
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
        matchId?.let {
            viewModel.getOddsDetail(it).run {
                subscribeChannelEvent(it)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun OddsDetailResult.setupPlayCateTab() {
        val playCateTypeList = this.oddsDetailData?.matchOdd?.playCateTypeList
        if (playCateTypeList?.isNotEmpty() != true) {
            rv_cat.gone()
            return
        }

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
    }


    /** 初始化监听 **/
    private fun initAllObserve() {
        initObserve()
        initSocketObserver()
    }

    private fun setUpBetBarVisible() {
        binding.parlayFloatWindow.isVisible = !BetInfoRepository.betInfoList.value?.peekContent()
            .isNullOrEmpty()
    }

    private var dsgView: DetailSportGuideView? = null
    /**
     * 检查篮球末尾比分 新手引导是否已经展示过了
     */
    private fun checkSportGuideState(code: String) {
        if (code == MatchType.END_SCORE.postValue) {
            if (KvUtils.decodeBooleanTure(KvUtils.BASKETBALL_GUIDE_TIP_FLAG, false)) {
                dsgView?.visibility = GONE
            } else {
                if (dsgView == null) {
                    dsgView = DetailSportGuideView(this)
                    val parent = parlayFloatWindow.parent as ViewGroup
                    parent.addView(dsgView, fl_bet_list.indexOfChild(parlayFloatWindow), ViewGroup.LayoutParams(-1, -1))
                }
                dsgView!!.visibility = VISIBLE
            }
        }
    }

    private fun selectMenuTab(position:Int){
        listOf(binding.ivLiveStream,binding.ivVideo,binding.ivAnim).forEachIndexed { index, imageView ->
            imageView.isSelected = position==index
        }
    }

    /**
     * 设置电竞主题样式
     */
    private fun setESportTheme()=binding.run{
        clToolContent.setBackgroundResource(R.color.color_EEF3FC)
        viewToolCenter.setBackgroundResource(R.color.color_D4E1F1)
        linCategroy.setBackgroundResource(R.drawable.bg_gradient_detail_tab)
    }

}
