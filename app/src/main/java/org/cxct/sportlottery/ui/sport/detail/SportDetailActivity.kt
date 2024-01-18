package org.cxct.sportlottery.ui.sport.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import com.google.android.material.appbar.AppBarLayout.LayoutParams.*
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
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityDetailSportBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.service.MatchOddsRepository
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.ui.sport.detail.adapter.*
import org.cxct.sportlottery.ui.sport.detail.adapter2.OddsDetailListAdapter
import org.cxct.sportlottery.ui.sport.detail.fragment.SportChartFragment
import org.cxct.sportlottery.ui.sport.detail.fragment.SportToolBarTopFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.view.DetailSportGuideView
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import splitties.bundle.put
import timber.log.Timber
import java.util.*


class SportDetailActivity : BaseSocketActivity<SportViewModel>(SportViewModel::class),
    TimerManager {

    private val binding by lazy { ActivityDetailSportBinding.inflate(layoutInflater) }

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
            matchInfo?.let { intent.putExtra("matchInfo", it.toJson()) }
            matchId?.let { intent.putExtra("matchId", it) }
            intent.putExtra("intoLive", intoLive)
            intent.putExtra("tabCode", tabCode)
            intent.putExtra("matchType",
                when {
                    matchType!= null -> matchType
                    matchInfo!= null && TimeUtil.isTimeInPlay(matchInfo.startTime) -> MatchType.IN_PLAY
                    else -> MatchType.DETAIL
                }
            )
            context.startActivity(intent)
        }
    }


    private var matchType: MatchType = MatchType.DETAIL
    private var intoLive = false
    private lateinit var oddsAdapter: OddsDetailListAdapter
    private val tabCateAdapter:  TabCateAdapter by lazy {
        TabCateAdapter(OnItemSelectedListener {
            tabCateAdapter.selectedPosition = it
            (rv_cat.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
                rv_cat, RecyclerView.State(), tabCateAdapter.selectedPosition
            )
            viewModel.oddsDetailResult.value?.peekContent()?.oddsDetailData?.matchOdd?.playCateTypeList?.getOrNull(it)?.code?.let { code ->
                checkSportGuideState(code)
                oddsAdapter.notifyDataSetChangedByCode(code)
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
                showFullScreen(enable)
            }

            override fun onClose(){
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


    fun initToolBar() = binding.run {
        setStatusbar(R.color.color_FFFFFF,true)
        ivBack.setOnClickListener {
            if (liveViewToolBar.isFullScreen) {
                liveViewToolBar.showFullScreen(false)
                showFullScreen(false)
                return@setOnClickListener
            }

            if (vpContainer.isVisible) {
                onBackPressed()
                return@setOnClickListener
            }

            if (intoLive) {
                finish()
                return@setOnClickListener
            }

            selectMenuTab(-1)
            vpContainer.visible()
            liveViewToolBar.stopPlay()
            liveViewToolBar.gone()
            binding.collapsToolbar.gone()
            setScrollEnable(true)
        }

        //滚球中并且指定球类显示比分板
        val needShowBoard = TimeUtil.isTimeInPlay(matchInfo?.startTime) && when (matchInfo?.gameType) {
            GameType.FT.key, GameType.BK.key, GameType.TN.key, GameType.BM.key, GameType.TT.key, GameType.VB.key -> true
            else -> false
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
            setIndicatorColor(context.getColor(R.color.color_FFFFFF), context.getColor(R.color.color_025BE8)
            )
            val height = 4.dp
            itemWidth = 10.dp
            itemHeight = height
            mRadius = itemWidth.toFloat()
            setSpacing(height)
            resetItemCount(2)
        }

        binding.detailToolBarViewPager.registerOnPageChangeCallback(object: OnPageChangeCallback() {
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

        ivFavorite.setOnClickListener { viewModel.pinFavorite(FavoriteType.MATCH, matchInfo?.id) }
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
                width = MATCH_PARENT
                height = if (enable) MATCH_PARENT else WRAP_CONTENT
            } else {
                width = MATCH_PARENT
                height = MATCH_PARENT
            }
        }

    }

    /**
     * 设置是否可以滑动折叠
     */
    private fun setScrollEnable(enable: Boolean) {
        (app_bar_layout.getChildAt(0).layoutParams as LayoutParams).apply {
            scrollFlags = if (enable) (SCROLL_FLAG_SCROLL)
            else SCROLL_FLAG_NO_SCROLL
        }
    }

    private fun setResetScrollEnable(enable: Boolean) {
        (app_bar_layout.getChildAt(0).layoutParams as LayoutParams).apply {
            scrollFlags = if (enable) (SCROLL_FLAG_SCROLL or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or SCROLL_FLAG_SNAP)
                else SCROLL_FLAG_NO_SCROLL
        }
    }


    private fun setupLiveView() {
        live_view_tool_bar.setupToolBarListener(liveToolBarListener)
    }

     fun initBottomNavigation() {
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
        setBg(binding.tvLiveStream, binding.ivLiveStream, showLive)
        val showVideo = matchInfo.liveVideo == 1
        setBg(binding.tvVideo, binding.ivVideo, showVideo)
         val showAnim = !(matchInfo.trackerId.isNullOrEmpty())
        setBg(binding.tvAnim, binding.ivAnim, showAnim)

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

     fun showBetListPage() {
        betListFragment = BetListFragment.newInstance(object : BetListFragment.BetResultListener {
            override fun onBetResult(betResultData: Receipt?, betParlayList: List<ParlayOdd>, isMultiBet: Boolean) {
                showBetReceiptDialog(betResultData, betParlayList, isMultiBet, R.id.fl_bet_list)
            }
        })

        supportFragmentManager.beginTransaction()
            .add(R.id.fl_bet_list, betListFragment!!)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()
    }


     fun updateBetListCount(num: Int) {
        setUpBetBarVisible()
        binding.parlayFloatWindow.updateCount(num.toString())
        Timber.e("num: $num")
        if (num > 0) viewModel.getMoneyAndTransferOut()
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
        matchInfo?.let { subscribeChannelEvent(it.id,it.gameType) }
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
        oddsAdapter = OddsDetailListAdapter(OnOddClickListener { odd, oddsDetail, scoPlayCateNameForBetInfo ->
            scoPlayCateNameForBetInfo?.let {
                odd.spread = tranByPlayCode(this,odd.playCode, null,null,null)
            }

            val matchOdd = matchOdd ?: return@OnOddClickListener
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
                otherPlayCateName = scoPlayCateNameForBetInfo,
                categoryCode = matchInfo?.categoryCode
            )
            viewModel.updateMatchBetListData(fastBetDataBean)
        })

        oddsAdapter.oddsDetailListener =  {
            viewModel.pinFavorite(FavoriteType.PLAY_CATE, it, matchInfo?.gameType)
        }
        oddsAdapter.sportCode = GameType.getGameType(matchInfo?.gameType)
        rv_detail.addItemDecoration(SpaceItemDecoration(this, R.dimen.margin_4))
        (rv_detail.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rv_detail.adapter = oddsAdapter
        rv_detail.setLinearLayoutManager()
        oddsAdapter.setPreloadItem()

        rv_cat.apply {
            adapter = tabCateAdapter
            layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator?.changeDuration = 0
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
        }

        linArrow.isSelected = true
        binding.ivArrow.rotationAnimation(0f)
        linArrow.setOnClickListener {
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                binding.ivArrow.rotationAnimation(0f)
            } else {
                binding.ivArrow.rotationAnimation(180f)
            }
            oddsAdapter.oddsDetailDataList.forEach { it.isExpand = linArrow.isSelected }
            oddsAdapter.notifyDataSetChanged()
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

    private fun initObserve() {

        viewModel.matchLiveInfo.observe(this) {
            val matchRound = it?.peekContent() ?: return@observe
            live_view_tool_bar.liveUrl = if (matchRound.pullRtmpUrl.isNotEmpty()) matchRound.pullRtmpUrl else matchRound.pullFlvUrl
            if (intoLive) {
                showLive()
            }
        }
        viewModel.betInfoList.observe(this) {
            updateBetListCount(it.peekContent().size)
        }
        viewModel.notifyLogin.observe(this) {
            showLoginSnackbar()
        }
        viewModel.notifyMyFavorite.observe(this) {
            it.getContentIfNotHandled()?.let { result ->
                showFavoriteNotify(result)
            }
        }

        viewModel.videoUrl.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { live_view_tool_bar.videoUrl = it }
        }

        viewModel.animeUrl.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { live_view_tool_bar.animeUrl = it }
        }

        viewModel.showBetInfoSingle.observe(this) {
            it.getContentIfNotHandled()?.let { showBetListPage() }
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
                    oddsAdapter.notifyDataSetChangedByCode(dataList[selectedPosition].code)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@observe
            }

            rv_detail.post { rv_detail.requestLayout() }
            matchOdd = result.oddsDetailData?.matchOdd
            setupLiveView()

            val matchInfo = result.oddsDetailData?.matchOdd?.matchInfo ?: return@observe
            if (this.matchInfo == null){
                this.matchInfo = matchInfo
            }
            //region 配置主客隊名稱給內部Item使用
            matchInfo.homeName?.let { home ->
                oddsAdapter.homeName = home
            }
            matchInfo.awayName.let { away ->
                oddsAdapter.awayName = away
            }
            //endregion
            tv_toolbar_home_name.text = matchInfo.homeName ?: ""
            tv_toolbar_away_name.text = matchInfo.awayName ?: ""
            sportToolBarTopFragment.updateMatchInfo(matchInfo, true)
            if (matchId != null) {
                tv_game_title.text = matchInfo.leagueName
                updateMenu(matchInfo)
                ivFavorite.isSelected = matchInfo.isFavorite
                oddsAdapter.sportCode = GameType.getGameType(matchInfo.gameType)
                oddsAdapter.notifyDataSetChanged()
            }


            Handler(Looper.getMainLooper()).postDelayed({
                sportChartFragment.updateMatchInfo(matchInfo)
            }, 300)
        }

        viewModel.oddsDetailList.observe(this) {
            val list = it.peekContent()
            if (list.isNullOrEmpty()) {
                return@observe
            }

            oddsAdapter.setPreloadItem()
            //如果是末位比分，小节比分就折叠起来
            if (tabCode == MatchType.END_SCORE.postValue) {
                val filtered = list.filter { it.gameType.isEndScoreType() }
                filtered.forEachIndexed { index, oddsDetailListData ->
                    oddsDetailListData.isExpand = (index == 0 )
                }
            }
            oddsAdapter.oddsDetailDataList = list
        }

        viewModel.betInfoList.observe(this) {
            oddsAdapter.betInfoList = it.peekContent()
        }


        viewModel.oddsType.observe(this) {
            oddsAdapter.oddsType = it
        }

        viewModel.favorPlayCateList.observe(this) {
       
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

            val pinList = oddsAdapter.oddsDetailDataList.filter {
                playCateCodeList?.contains(it.gameType) ?: false
            }.sortedByDescending { oddsDetailListData ->
                playCateCodeList?.indexOf(oddsDetailListData.gameType)
            }

            val epsSize = oddsAdapter.oddsDetailDataList.groupBy {
                it.gameType == PlayCate.EPS.value
            }[true]?.size ?: 0

            oddsAdapter.oddsDetailDataList.sortBy { it.originPosition }
            oddsAdapter.oddsDetailDataList.forEach { it.isPin = false }
            pinList.forEach {
                it.isPin = true
                oddsAdapter.oddsDetailDataList.add(
                    epsSize, oddsAdapter.oddsDetailDataList.removeAt(
                        oddsAdapter.oddsDetailDataList.indexOf(it)
                    )
                )
            }

            oddsAdapter.notifyDataSetChanged()
        }

        viewModel.showBetUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true) {
                showLoginSnackbar(R.string.bet_notify_max_limit,R.id.parlayFloatWindow)
            }
        }

        viewModel.showBetBasketballUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true) {
                showLoginSnackbar(R.string.bet_basketball_notify_max_limit,R.id.parlayFloatWindow)
            }
        }

    }

    private fun showLive() {
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
        setupSportStatusChange(this) { if (it) { finish() } }

        MatchOddsRepository.observerMatchStatus(this) { matchStatusChangeEvent->
            val matchStatusCO = matchStatusChangeEvent.matchStatusCO?.takeIf { ms -> ms.matchId == matchInfo?.id } ?: return@observerMatchStatus

            //從滾球以外的狀態轉變為滾球時, 重新獲取一次賽事資料, 看是否有新的直播或動畫url
            if (matchType != MatchType.IN_PLAY) {
                matchType = MatchType.IN_PLAY
                unSubscribeChannelEvent(matchId)
                getData()
            }


            val matchOdd = matchOdd ?: return@observerMatchStatus
            var isNeedUpdate = SocketUpdateUtil.updateMatchStatus(
                gameType = matchStatusCO.gameType,
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

        receiver.matchClock.observe(this) { matchClockEvent->
            if (matchClockEvent == null || matchClockEvent.matchId != matchInfo?.id) {
                return@observe
            }

            val updateTime = when (matchInfo?.gameType) {
                GameType.FT.key -> matchClockEvent.matchTime
                GameType.BK.key, GameType.RB.key, GameType.AFT.key -> matchClockEvent.remainingTimeInPeriod
                else -> null
            }

            isGamePause = (matchClockEvent.stopped == 1)
            updateTime?.let { time ->
                startTime = time.toLong()
                matchType = if (TimeUtil.isTimeInPlay(startTime)) MatchType.IN_PLAY else MatchType.DETAIL
            }
            matchInfo?.let { SocketUpdateUtil.updateMatchInfoClockByDetail(it, matchClockEvent) }
        }

        MatchOddsRepository.observerMatchOdds(this) {
            val oddsDetailListDataList = oddsAdapter.oddsDetailDataList ?: return@observerMatchOdds
            if (oddsDetailListDataList.isEmpty()) {
                return@observerMatchOdds
            }
            val playCate = viewModel.favorPlayCateList.value?.find { playCate -> playCate.gameType == matchInfo?.gameType }
            val updatedDataList = SocketUpdateUtil.updateMatchOddsMap(oddsDetailListDataList,it,playCate)?:return@observerMatchOdds
            oddsAdapter.oddsDetailDataList = updatedDataList
        }
        receiver.matchOddsLock.collectWith(lifecycleScope) {
            //比對收到 matchOddsLock event 的 matchId
            if (matchInfo?.id != it.matchId) {
                return@collectWith
            }
            val oddsDetailListDataList = oddsAdapter.oddsDetailDataList
            oddsDetailListDataList.forEachIndexed { index, oddsDetailListData ->
                if (SocketUpdateUtil.updateOddStatus(oddsDetailListData)) {
                    oddsAdapter.notifyItemChanged(index)
                }
            }
        }

        receiver.globalStop.observe(this) {
            val globalStopEvent = it ?: return@observe
            oddsAdapter.oddsDetailDataList.forEachIndexed { index, oddsDetailListData ->
                if (SocketUpdateUtil.updateOddStatus(oddsDetailListData, globalStopEvent) && oddsDetailListData.isExpand) {
                    oddsAdapter.notifyItemChanged(index)
                }
            }
        }

        receiver.producerUp.observe(this) {
            it?.let {
                unSubscribeChannelEventAll()
                matchInfo?.let {
                    subscribeChannelEvent(it.id,it.gameType)
                }
            }
        }

        receiver.closePlayCate.observe(this) { event ->
            val oddsDataList = oddsAdapter.oddsDetailDataList
            val closeEvent = event?.getContentIfNotHandled() ?: return@observe
            if (matchInfo?.gameType != closeEvent.gameType) return@observe
            val index = oddsDataList.indexOf(oddsDataList.find { it.gameType == closeEvent.playCateCode }
                    ?.apply { oddArrayList.forEach { it?.status = BetStatus.DEACTIVATED.code } })
            if (index < 0) return@observe
            oddsAdapter.notifyItemChanged(index)
        }
    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(oddsDetailListData: OddsDetailListData, matchOddsChangeEvent: MatchOddsChangeEvent) {
        if (getBetListPageVisible()) {
            return
        }

        //尋找是否有加入注單的賠率項
        if (oddsDetailListData.oddArrayList.any { it?.isSelected == true }) {
            viewModel.updateMatchOdd(matchOddsChangeEvent)
        }
    }

    open fun getBetListPageVisible(): Boolean {
        return betListFragment?.isVisible ?: false
    }


    private fun getData() {
        matchInfo?.let {
            viewModel.getOddsDetail(it.id)
            subscribeChannelEvent(it.id, it.gameType)
        }
        matchId?.let {
            viewModel.getOddsDetail(it)
            subscribeChannelEvent(it)
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
            val index = playCateTypeList.indexOfFirst { it.code == tabCode }
            if (index >= 0) {
                tabCateAdapter.selectedPosition = index
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
        if (code != MatchType.END_SCORE.postValue) {
            return
        }

        if (DetailSportGuideView.isPlayed()) {
            dsgView?.gone()
            return
        }

        if (dsgView == null) {
            dsgView = DetailSportGuideView(this)
            val parent = parlayFloatWindow.parent as ViewGroup
            parent.addView(dsgView, fl_bet_list.indexOfChild(parlayFloatWindow), ViewGroup.LayoutParams(-1, -1))
        }
        dsgView!!.visible()
    }

    private fun selectMenuTab(position:Int) {
        listOf(binding.ivLiveStream, binding.ivVideo, binding.ivAnim).forEachIndexed { index, imageView ->
            imageView.isSelected = position == index
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
