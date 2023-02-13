package org.cxct.sportlottery.ui.maintab

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.gyf.immersionbar.ImmersionBar
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import kotlinx.android.synthetic.main.fragment_main_home.*
import kotlinx.android.synthetic.main.fragment_main_left.*
import kotlinx.android.synthetic.main.fragment_sport_list.*
import kotlinx.android.synthetic.main.hot_card_game_include.*
import kotlinx.android.synthetic.main.hot_gaming_include.*
import kotlinx.android.synthetic.main.hot_handicap_include.*
import kotlinx.android.synthetic.main.hot_live_match_include.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
import kotlinx.android.synthetic.main.view_toolbar_home.btn_login
import kotlinx.android.synthetic.main.view_toolbar_home.btn_register
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.event.MoneyEvent
import org.cxct.sportlottery.extentions.isEmptyStr
import org.cxct.sportlottery.extentions.load
import org.cxct.sportlottery.extentions.toIntS
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.network.third_game.third_games.hot.HandicapData
import org.cxct.sportlottery.network.third_game.third_games.hot.HotMatchInfo
import org.cxct.sportlottery.network.third_game.third_games.hot.HotMatchLiveData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.dialog.ThirdGameDialog
import org.cxct.sportlottery.ui.game.publicity.PublicityAnnouncementMarqueeAdapter
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.news.NewsActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.widget.OKVideoPlayer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainHomeFragment :
    BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class), OKVideoPlayer.OnOkListener
    {

    private  var mMatchInfo: MatchInfo?=null
    interface TimerListener {
        fun onTimerUpdate(timeMillis: Long)
    }
    var listener: TimerListener? = null

    companion object {
        fun newInstance(): MainHomeFragment {
            val fragment = MainHomeFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }
    private val homeHotLiveAdapter by lazy {//热门直播

        HotLiveAdapter(HotLiveAdapter.ItemClickListener { data ->

            if (!data.matchInfo.statusName18n.isNullOrEmpty()) {
                tv_first_half_game.text = data.matchInfo.statusName18n
                tv_first_half_game.setBackgroundResource(R.drawable.bg_radius_100_text)
            } else {
                tv_first_half_game.setBackgroundResource(0)
            }

            tv_match_name.text = data.league.name
            tv_match_type_name.text = data.sportName
            tv_introduction.text =  data.matchInfo.streamerName ?: getString(R.string.okbet_live_name)

            iv_live_type.load("${data.matchInfo.frontCoverUrl}", R.drawable.icon_novideodata)
            iv_live_type.load("${data.matchInfo.streamerIcon}", R.drawable.icon_avatar)

            mMatchInfo = data.matchInfo
            if (data.matchInfo.pullRtmpUrl.isNullOrEmpty()) {
                data.matchInfo.roundNo?.let { viewModel.getLiveInfo(it, 0) }
            } else {
                playMatchVideo(data.matchInfo)
            }
        })
    }

    private val hotHandicapAdapter by lazy {
        HotHandicapAdapter(this, mutableListOf()).apply {
            homeRecommendListener = HomeRecommendListener(

                onItemClickListener = { matchInfo ->
                    if (isCreditSystem() && viewModel.isLogin.value != true) {
                        getMainTabActivity().showLoginNotify()
                    }else{
                        matchInfo?.let {
                            navOddsDetailFragment(MatchType.IN_PLAY, it)
                        }
                    }
                },
                onGoHomePageListener = {

                },
                onClickBetListener = { gameType, matchType, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap, playCateMenuCode ->
                    if (!mIsEnabled) {
                        return@HomeRecommendListener
                    }

                    avoidFastDoubleClick()
                    if (isCreditSystem() &&  viewModel.isLogin.value != true) {
                        getMainTabActivity().showLoginNotify()
                        return@HomeRecommendListener
                    }

                    addOddsDialog(gameType,
                        matchType,
                        matchInfo,
                        odd,
                        playCateCode,
                        playCateName,
                        betPlayCateNameMap,
                        playCateMenuCode
                    )

                },
                onClickFavoriteListener = {

                },
                onClickStatisticsListener = { _ ->

                }, onClickPlayTypeListener = { _, _, _, _ ->

                }, onClickLiveIconListener = { _, _, _, _ ->

                }
            ) { _, _, _, _ ->

            }
        }
    }
    private val hotElectronicAdapter by lazy {//电子
        HomeElectronicAdapter(mutableListOf())
    }
    private val homeChessAdapter by lazy {//棋牌
        HomeChessAdapter(mutableListOf())
    }
    private val mPublicityVersionUpdateViewModel: VersionUpdateViewModel by viewModel()
    private val mHandicapCodeList by lazy {
        resources.getStringArray(R.array.handicap_type_list).mapIndexed { index, data ->
            StatusSheetData((index + 1).toString(), data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_main_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getGameEntryConfig(1, null)

        viewModel.getHotLiveList()
        viewModel.getLiveRoundCount()
        initView()
        initObservable()
        queryData()
        initSocketObservers()
//        viewModel.getHandicapConfig(hotHandicapAdapter.playType.toInt())
        EventBusUtil.targetLifecycle(this)
    }

    override fun onResume() {
        super.onResume()
//        LogUtil.d("onResume")
        iv_publicity.startPlayLogic()
        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()
//        LogUtil.d("onPause")
        iv_publicity.onVideoPause()
        rv_marquee.stopAuto()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getLiveRoundCount()
            viewModel.getHotLiveList()
            onRefreshMoney()
            viewModel.getHandicapConfig(hotHandicapAdapter.playType.toInt())
            viewModel.getGameEntryConfig(1, null)
            setupOddsChangeListener()
            iv_publicity.setUp(mMatchInfo?.pullRtmpUrl, true, "");
            iv_publicity.startPlayLogic()
        } else {
            iv_publicity.onVideoPause()
        }
    }

    private fun initView() {

        initToolBar()
        initPlayView()

        iv_customer_service.setOnClickListener {
            clickCustomService(requireContext(), childFragmentManager)
        }

        MainHomeItemHelper.fillingItems(tabLinearLayout, ::onTabClick)

        initHotHandicap()
        initListView()

        nsv_home.setupBackTop(iv_top, 180.dp)

        view_action.setOnClickListener {
            if (mMatchInfo == null) return@setOnClickListener

            SportDetailActivity.startActivity(requireContext(),
                matchInfo = mMatchInfo!!,
                matchType = MatchType.IN_PLAY,
                true)
        }

        ll_hot_live_more.setOnClickListener { getHomeFragment().onTabClickByPosition(1)}

        ll_hot_handicap_more.setOnClickListener {
            getMainTabActivity().jumpToTheSport(MatchType.IN_PLAY, GameType.ALL)
        }
        ll_hot_elect.setOnClickListener { getHomeFragment().onTabClickByPosition(4) }
        ll_poker_more.setOnClickListener { getHomeFragment().onTabClickByPosition(5)}
    }

    fun initToolBar() {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        iv_menu_left.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(true))
            getMainTabActivity().showLeftFrament(0, 0)
        }
        btn_register.setOnClickListener {
            requireActivity().startRegister()
        }
        btn_login.setOnClickListener {
            requireActivity().startLogin()
        }
        iv_money_refresh.setOnClickListener {
            iv_money_refresh.startAnimation(RotateAnimation(0f,
                720f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f).apply {
                duration = 1000
            })
            viewModel.getMoney()
        }

        setupLogin()
    }
    @SuppressLint("SetTextI18n")
    private fun initObservable() {
        if (viewModel == null) {
            return
        }
        viewModel.isLogin.observe(viewLifecycleOwner) {
            setupLogin()
        }
        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let {
                tv_home_money.text = "${sConfigData?.systemCurrencySign} ${TextUtil.format(it)}"
            }
        }
        viewModel.liveRoundCount.observe(viewLifecycleOwner) {
           // tv_live_count.text = it
            tv_hot_live_find_more.text = getString(R.string.see_more) + (if (it == "0") "" else it)
        }

//        viewModel.userMoney.observe(viewLifecycleOwner) {
//
//        }
        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                hotHandicapAdapter.oddsType = oddsType
            }
        }

        viewModel.betInfoList.observe(viewLifecycleOwner) { event ->
            event.peekContent().let { betInfoList ->
                hotHandicapAdapter.betInfoList = betInfoList
            }
        }
        viewModel.gotConfig.observe(viewLifecycleOwner) { event ->
            event.peekContent().let { isReload ->
                if (isReload) {
                    setupBanner()
                    viewModel.getPublicityPromotion()
                }
                viewModel.getSportMenuFilter()
                if (ThirdGameDialog.firstShow) {
                    ThirdGameDialog().apply {
                        onClick = {
                            (this@MainHomeFragment.parentFragment as HomeFragment).onTabClickByPosition(
                                HomeTabAdapter.getItems()
                                    .indexOfFirst { it.name == R.string.home_on_game })
                        }
                        onDismiss = {
                            MultiLanguagesApplication.showPromotionPopupDialog(requireActivity())
                        }
                    }.show(childFragmentManager, ThirdGameDialog::class.simpleName)
                } else {
                    MultiLanguagesApplication.showPromotionPopupDialog(requireActivity())
                }
            }
        }
//
        //新版宣傳頁
        viewModel.messageListResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { messageListResult ->
                val titleList: MutableList<String> = mutableListOf()
                messageListResult.rows?.forEach { data ->
                    if (data.type.toInt() == 1) titleList.add(data.title + " - " + data.message)
                }
                setupAnnouncement(titleList)
            }
        }

        viewModel.enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisible)
                enterThirdGame(it)
        }


        viewModel.homeGameData.observe(viewLifecycleOwner) {
            it?.let { gameList->
                //棋牌
                val mHotChessList = gameList.filter { data->
                    data.gameType?.equals("1") == true
                }
                if (mHotChessList.isNullOrEmpty()){
                    hot_card_game_include.visibility = View.GONE

                    view1.visibility = View.GONE
                }else{
                    view1.visibility = View.VISIBLE
                    hot_card_game_include.visibility = View.VISIBLE

                }
                homeChessAdapter.setNewData(mHotChessList?.toMutableList())

                //电子
                val mHotelList = gameList.filter {data->
                    data.gameType?.equals("2") == true
                }
                if (mHotelList.isNullOrEmpty()){
                    hot_gaming_include.visibility = View.GONE
                    view2.visibility = View.GONE
                }else{
                    hot_gaming_include.visibility = View.VISIBLE
                    view2.visibility = View.VISIBLE
                }
                hotElectronicAdapter.setNewData(mHotelList?.toMutableList())
            }
        }
        viewModel.hotLiveData.observe(viewLifecycleOwner){ list->
            if (list.isNullOrEmpty()){
                hot_live_match.visibility = View.GONE
            }else{
                list[0].apply {
                        tv_match_type_name.text = sportName
                        tv_match_name.text = league.name
                        tv_first_half_game.text = matchInfo.statusName18n
                        tv_match_time.text = runningTime
                        context?.let {mContext->

                            Glide.with(mContext)
                                .load(matchInfo.frontCoverUrl)
                                .apply(RequestOptions().placeholder(R.drawable.icon_novideodata))
                                .into(iv_live_type)
                            Glide.with(mContext)
                                .load(matchInfo.streamerIcon)
                                .apply(RequestOptions().placeholder(R.drawable.icon_avatar))
                                .into(iv_avatar_live)

                        }
                    mMatchInfo = matchInfo
                    tv_introduction.text = matchInfo.streamerName

                    matchInfo.roundNo?.let {
                        viewModel.getLiveInfo(it, 0)
                    }
                }
                    if(homeHotLiveAdapter.data.isNullOrEmpty()){
                        homeHotLiveAdapter.mSelectedId = list.firstOrNull()?.matchInfo?.id
                    }
                    homeHotLiveAdapter.data = list

                     //订阅直播
                     subScribeLiveData(list)

                    rv_match_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                    rv_match_list.adapter = homeHotLiveAdapter
            }

        }
        //热门盘口
        viewModel.hotHandicap.observe(viewLifecycleOwner) {
            val list = it.getContentIfNotHandled()
           if ( list.isNullOrEmpty()){
               rv_hot_handicap.visibility = View.GONE
           }else{
               rv_hot_handicap.visibility = View.VISIBLE
               list.let {
                   hideLoading()
                   it.forEach { handi ->
                       handi.matchInfos.forEach { hotdata ->
                           hotdata.getBuildMatchInfo()
                           hotdata.leagueId = handi.league.id
                           hotdata.oddsSort = handi.oddsSort
                           // 將儲存的賠率表指定的賽事列表裡面
                           val leagueOddFromMap = leagueOddMap[hotdata.id]
                           leagueOddFromMap?.let {
                               hotdata.oddsMap = leagueOddFromMap.oddsMap
                           }
                       }
                   }
                   hotHandicapAdapter.data.forEach { item->
                       item.matchInfos.forEach {
                           unSubscribeChannelHall(it.gameType, it.id)
                       }
                   }

                   hotHandicapAdapter.setNewInstance(list?.toMutableList())
                   //订阅赛事
                   subscribeQueryData(list)
               }
           }
        }
        viewModel.homeMatchLiveInfo.observe(viewLifecycleOwner) { event ->
            event?.peekContent()?.let { matchRound ->
                homeHotLiveAdapter.data.forEachIndexed { index, hotMatchLiveData ->
                    if (hotMatchLiveData.matchInfo.roundNo == matchRound.roundNo) {
                        hotMatchLiveData.matchInfo.pullRtmpUrl = matchRound.pullRtmpUrl
                        hotMatchLiveData.matchInfo.pullFlvUrl = matchRound.pullFlvUrl
                        homeHotLiveAdapter.notifyItemChanged(index, hotMatchLiveData)
                        mMatchInfo = hotMatchLiveData.matchInfo
                        playMatchVideo(hotMatchLiveData.matchInfo)
                    }
                }
            }
        }
    }

//    private var connectFailed = false
    //用户缓存最新赔率，方便当从api拿到新赛事数据时，赋值赔率信息给新赛事
    private val leagueOddMap = HashMap<String, HotMatchInfo>()
    private fun initSocketObservers() {
        receiver.serviceConnectStatus.observe(viewLifecycleOwner) {
//            if (it == ServiceConnectStatus.RECONNECT_FREQUENCY_LIMIT) {
//                connectFailed = true
//                return@observe
//            }

            if (it == ServiceConnectStatus.CONNECTED) {
//                connectFailed = false
                unSubscribeChannelHallSport()
                unSubscribeChannelHallAll()
                viewModel.getHandicapConfig(hotHandicapAdapter.playType.toInt())
            }
        }

        //观察比赛状态改变
        receiver.matchStatusChange.observe(viewLifecycleOwner) { event ->

            event?.let { matchStatusChangeEvent ->
                hotHandicapAdapter.data.forEachIndexed { index, handicapData ->

                    var needUpdate = false
                    handicapData.matchInfos.iterator().let {

                        while (!needUpdate && it.hasNext()) {

                            val next = it.next()
                            if (SocketUpdateUtil.updateMatchStatus(next.gameType,
                                    handicapData.matchInfos as MutableList<MatchOdd>,
                                    matchStatusChangeEvent,
                                    context)) {

                                needUpdate = true
                            }
                        }
                    }
//                    handicapData.matchInfos.forEach { hotMatchInfo ->
//
//                        if (SocketUpdateUtil.updateMatchStatus(
//                                hotMatchInfo.gameType,
//                                handicapData.matchInfos as MutableList<MatchOdd>,
//                                matchStatusChangeEvent,
//                                context
//                            )
//                        ) {
//                            needUpdate = true
//                            return@forEach
//                        }
//                    }
                    if (needUpdate) {
                        hotHandicapAdapter.notifyItemChanged(index)
                    }
                }

                val targetList = homeHotLiveAdapter.data
                var needUpdate = false // 记录是否要更新赛事清单
                 targetList.forEachIndexed { index, hotMatchLiveData ->

                     var matchList = listOf(hotMatchLiveData).toMutableList()
                     if (hotMatchLiveData.matchInfo.id==matchStatusChangeEvent.matchStatusCO?.matchId){

                     }
                     if (SocketUpdateUtil.updateMatchStatus(
                             hotMatchLiveData.matchInfo.gameType,
                             matchList as MutableList<MatchOdd>,
                             matchStatusChangeEvent,
                             context
                     )){

                         needUpdate = true
                         return@forEachIndexed
                     }
                 }
                if (needUpdate) {
                    homeHotLiveAdapter.data = targetList
                }
            }
        }

        receiver.matchClock.observe(viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val targetList = hotHandicapAdapter.data
                targetList.forEachIndexed { index, handicapData ->
                    var needUpdate = false
                    handicapData.matchInfos.forEach{ hotMatchInfo->
                        if (
                            SocketUpdateUtil.updateMatchClock(
                                hotMatchInfo,
                                matchClockEvent
                            )
                        ) {
                            needUpdate = true

                        }
                    }
                    if (needUpdate)
                        hotHandicapAdapter.notifyItemChanged(index)
                }
            }
        }
//        receiver.matchOddsChange.observe(viewLifecycleOwner) {
//
//        }
        setupOddsChangeListener()

        receiver.matchOddsLock.observe(viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                // 紀錄是否需要更新整個推薦賽事清單
                hotHandicapAdapter.data.forEachIndexed { index, handicapData ->
                    var needUpdate = false
                    handicapData.matchInfos.forEach { hotMatchInfo ->
                        if (SocketUpdateUtil.updateOddStatus(
                                hotMatchInfo, matchOddsLockEvent
                            )
                        ) {
                            needUpdate = true
                            //TODO 更新邏輯待補，跟進GameV3Fragment
                        }
                    }
                    if (needUpdate) {
                        hotHandicapAdapter.notifyItemChanged(index)
                    }
                }

            }
        }


        receiver.globalStop.observe(viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                hotHandicapAdapter.data.forEachIndexed { index, handicapData ->
                    var needUpdate = false
                    handicapData.matchInfos.forEach {
                        if (SocketUpdateUtil.updateOddStatus(
                                it,
                                globalStopEvent
                            )
                        ) {
                            needUpdate = true
                            //TODO 更新邏輯待補，跟進GameV3Fragment
                        }
                    }
                    if (needUpdate) {
                        hotHandicapAdapter.notifyItemChanged(index)
                    }
                }
            }
        }

        receiver.producerUp.observe(viewLifecycleOwner) {
            it?.let {
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(hotHandicapAdapter.data)
                subScribeLiveData(homeHotLiveAdapter.data)
            }
        }

        receiver.closePlayCate.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                hotHandicapAdapter.data.forEach { handicapData ->
                    handicapData.matchInfos.forEach { hotMatchInfo ->
                        if (hotMatchInfo.gameType == it.gameType) {
                            hotMatchInfo.oddsMap?.forEach{map->
                                if (map.key == it.playCateCode) {
                                    map.value?.forEach { odd ->
                                    odd?.status = BetStatus.DEACTIVATED.code
                                    }
                                }
                            }
                        }

                    }
                }
                hotHandicapAdapter.notifyDataSetChanged()
            }
        }

    }

    fun setupOddsChangeListener() {
        receiver.oddsChangeListener = mOddsChangeListener
    }

    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            hotHandicapAdapter.data.forEachIndexed { index, handicap ->
                var needUpdate = false
                handicap.matchInfos.forEach { hotMatchInfo ->
                    if (hotMatchInfo.id == oddsChangeEvent.eventId) {
                        hotMatchInfo.sortOddsMap()
                        //region 翻譯更新
                        oddsChangeEvent.playCateNameMap?.let { playCateNameMap ->
                            hotMatchInfo.playCateNameMap?.putAll(playCateNameMap)
                        }
                        oddsChangeEvent.betPlayCateNameMap?.let { betPlayCateNameMap ->
                            hotMatchInfo.betPlayCateNameMap?.putAll(betPlayCateNameMap)
                        }
                        //endregion
                        if (SocketUpdateUtil.updateMatchOddsNew(context,
                                hotMatchInfo,
                                oddsChangeEvent)
                        ) {
                            updateBetInfo(hotMatchInfo, oddsChangeEvent)
                            leagueOddMap[hotMatchInfo.id] = hotMatchInfo
                            needUpdate = true
                        }
                    }
                }
                if (needUpdate) {
                    hotHandicapAdapter.notifyItemChanged(index)
                }
            }
        }
    }

    private fun HotMatchInfo.sortOddsMap() {
        this.oddsMap?.forEach { (_, value) ->
            if ((value?.size
                    ?: 0) > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)
            ) {
                value?.sortBy {
                    it?.marketSort
                }
            }
        }
    }


    private fun queryData() {
        mPublicityVersionUpdateViewModel.checkAppVersion()
        viewModel.getPublicitySportMenu()
        viewModel.getAnnouncement()
        viewModel.getConfigData()
        viewModel.getMoney()
    }

    private fun setupBanner() {

        var imageList = sConfigData?.imageList?.filter { it.imageType == 2 }

        if (imageList.isNullOrEmpty()) {
            banner.setBackgroundResource(R.drawable.img_banner01)
        }

        imageList?.let { list->
            val enable = list.size > 1
            rll_left_right.isVisible = enable
            banner.isAutoLoop(enable)
        }

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.img_banner01)
            .error(R.drawable.img_banner01)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontTransform()

        (banner as Banner<ImageData, BannerImageAdapter<ImageData>>)
            .setAdapter(object : BannerImageAdapter<ImageData>(imageList) {
                override fun onBindView(
                    holder: BannerImageHolder,
                    data: ImageData?,
                    position: Int,
                    size: Int) {

                    val url = sConfigData?.resServerHost + data?.imageName1
                    Glide.with(holder.itemView)
                        .load(url)
                        .apply(requestOptions)
                        .into(holder.imageView)

                    holder.imageView.setOnClickListener {
                        data?.imageLink?.let {
                            if (it.isNotBlank()) {
                                JumpUtil.toExternalWeb(requireContext(), it)
                            }
                        }
                    }
                }
            })
        //    .setIndicator(HomeBannerIndicator(requireContext()));

        //可能是我想复杂了 左右按钮只是为了区分是否只要一个图片 所以点击方法先注释掉


//        banner.adapter.setOnBannerListener { data, position ->
//            iv_right.setOnClickListener {
//                if (position<banner.realCount -1){
//                    var count = banner.currentItem
//
//                    banner.currentItem = count+1
//                }else{
//                    banner.currentItem = 0
//                }
//            }
//            iv_left.setOnClickListener {
//                if (position == 0){
//                    banner.currentItem = banner.realCount -1
//                }else {
//                    banner.currentItem = banner.currentItem-1
//                }
//            }
//        }
    }

    private fun setupAnnouncement(titleList: List<String>) {
        var marqueeAdapter = PublicityAnnouncementMarqueeAdapter()
        lin_announcement.setOnClickListener {
            startActivity(Intent(requireActivity(), NewsActivity::class.java))
        }
        rv_marquee.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = marqueeAdapter
        }

        marqueeAdapter.setData(titleList.toMutableList())
        if (titleList.isNotEmpty()) {
            rv_marquee.startAuto(false) //啟動跑馬燈
        } else {
            rv_marquee.stopAuto(true) //停止跑馬燈
        }
    }



    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(recommend: HotMatchInfo, oddsChangeEvent: OddsChangeEvent) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (recommend.matchInfo?.id == oddsChangeEvent.eventId && recommend.oddsMap?.values?.any { oddList ->
                    oddList?.any { odd ->
                        odd?.isSelected == true
                    } == true
                } == true
            ) {
                viewModel.updateMatchOdd(oddsChangeEvent)
            }
        }
    }

    private fun addOddsDialog(
        gameTypeCode: String,
        matchType: MatchType,
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        playCateMenuCode: String?) {

        val gameType = GameType.getGameType(gameTypeCode)
        if (gameType == null || matchInfo == null || activity !is MainTabActivity) {
            return
        }

        getMainTabActivity().setupBetData(FastBetDataBean(
            matchType = matchType,
            gameType = gameType,
            playCateCode = playCateCode,
            playCateName = playCateName,
            matchInfo = matchInfo,
            matchOdd = null,
            odd = odd,
            subscribeChannelType = ChannelType.HALL,
            betPlayCateNameMap = betPlayCateNameMap,
            playCateMenuCode
        ))
    }

    private fun navOddsDetailFragment(matchType: MatchType, matchInfo: MatchInfo) {
        SportDetailActivity.startActivity(requireContext(),  matchInfo, matchType)
    }

    private fun subscribeQueryData(recommendList: List<HandicapData>) {
        recommendList.forEach { subscribeChannelHall(it) }
    }

    //热门盘口订阅
    private fun subscribeChannelHall(recommend: HandicapData) {
        recommend.matchInfos.forEach {
            subscribeChannelHall(it.gameType, it.id)
        }
    }

    //直播订阅
    private fun  subScribeLiveData(liveDataList: List<HotMatchLiveData> ){
        liveDataList.forEach { subscribeChannelHall(it.matchInfo.gameType, it.matchInfo.id) }
    }

    private fun enterThirdGame(result: EnterThirdGameResult) {
        hideLoading()
        when (result.resultType) {
            EnterThirdGameResult.ResultType.SUCCESS -> context?.run {
                JumpUtil.toThirdGameWeb(
                    this,
                    result.url ?: "",
                    thirdGameCategoryCode = result.thirdGameCategoryCode
                )
            }
            EnterThirdGameResult.ResultType.FAIL -> showErrorPromptDialog(
                getString(R.string.prompt),
                result.errorMsg ?: ""
            ) {}
            EnterThirdGameResult.ResultType.NEED_REGISTER -> context?.startActivity(
                Intent(context,
                     if (isOKPlat()) RegisterOkActivity::class.java else RegisterActivity::class.java)
            )
            EnterThirdGameResult.ResultType.GUEST -> showErrorPromptDialog(
                getString(R.string.error),
                result.errorMsg ?: ""
            ) {}
            EnterThirdGameResult.ResultType.NONE -> {
            }
        }
        if (result.resultType != EnterThirdGameResult.ResultType.NONE)
            viewModel.clearThirdGame()
    }

    /**
     * 檢查信用盤狀態下是否已登入
     * @param eventFun 處於信用盤時若已登入則執行該function, 若非信用盤則直接執行
     */
    private fun checkCreditSystemLogin(eventFun: () -> Unit) {
        if (isCreditSystem()) {
            if (viewModel.checkLoginStatus()) {
                eventFun()
            }
        } else {
            eventFun()
        }
    }

    /**
     * 點擊準備進入指定球種
     */
    private fun enterTheSport(sportMenu: SportMenu) {

        if (sportMenu.entranceType != null) {
            jumpToTheSport(sportMenu.entranceType!!, sportMenu.gameType)
            return
        }

        viewModel.setSportClosePromptMessage(
            MultiLanguagesApplication.appContext.getString(sportMenu.gameType.string)
        )
    }

    /**
     * 跳轉至體育指定球種
     */
    private fun jumpToTheSport(matchType: MatchType, gameType: GameType) {
        getMainTabActivity().jumpToTheSport(matchType, gameType)
    }

    private fun setupLogin() = viewModel.isLogin.value?.let {
        btn_register.isVisible = !it
        btn_login.isVisible = !it
        ll_user_money.visibility = if (it) View.VISIBLE else View.INVISIBLE
    }

    private fun initHotHandicap() {
        selector_order_status.setItemData(mHandicapCodeList as MutableList<StatusSheetData>)
        selector_order_status.setOnItemSelectedListener { statusSheetData ->
            if (statusSheetData.code.isEmptyStr()) {
                return@setOnItemSelectedListener
            }

            viewModel.getHandicapConfig(statusSheetData.code.toIntS())
            hotHandicapAdapter.playType = statusSheetData.code!!
        }
    }

    private fun initListView(){

        //热门电子游戏
        with(rv_egame) {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            }
            if (adapter == null) {
                adapter = hotElectronicAdapter
            }

            hotElectronicAdapter.setOnItemClickListener{adapter, view, position ->
                //点击跳转到哪里
                if (viewModel.isLogin.value != true) {
                    getMainTabActivity().showLoginNotify()
                } else {
                    viewModel.requestEnterThirdGame(hotElectronicAdapter.data[position])
                }
            }
        }

        //棋牌
        with(rv_chess){
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            if (adapter == null) {
                addItemDecoration(SpaceItemDecoration(context, R.dimen.recyclerview_news_item_dec_spec))
                adapter = homeChessAdapter
            }

            homeChessAdapter.setOnItemClickListener { _, _, position ->
                //点击跳转到哪里
                if (viewModel.isLogin.value != true) {
                    getMainTabActivity().showLoginNotify()
                } else {
                    viewModel.requestEnterThirdGame(homeChessAdapter.data[position])
                }
            }
        }

        with(rv_hot_handicap){
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            }
            if (adapter == null) {
                adapter = hotHandicapAdapter
            }
        }
    }

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private inline fun getHomeFragment() = parentFragment as HomeFragment

    private fun onTabClick(tabName: Int) {
        if (isCreditSystem()) {
            loginedRun(requireContext()) { changeFragment(tabName) }
        } else {
            changeFragment(tabName)
        }
    }

    private fun changeFragment(tabName: Int) = when(tabName)  {
        //点击直播跳转
        R.string.home_live -> getHomeFragment().onTabClickByPosition(1)
        //点击滚球跳转
        R.string.home_in_play -> getHomeFragment().onTabClickByPosition(2)
        //点击体育跳转
        R.string.home_sports -> getHomeFragment().onTabClickByPosition(3)
        // 点击真人跳转
        R.string.live -> getHomeFragment().onTabClickByPosition(4)
        // 点击老虎机跳转
        R.string.tiger_machine -> getHomeFragment().onTabClickByPosition(4)
        //点击棋牌跳转
        R.string.home_on_game -> getHomeFragment().onTabClickByPosition(5)
        //点击彩票跳转
//        R.string.lottery -> getHomeFragment().onTabClickByPosition(5)

        else -> { }
    }

    private fun initPlayView() {
        iv_publicity.setOnOkListener(this)
        iv_publicity.setIsTouchWigetFull(false)

    }

    private fun playMatchVideo(matchInfo: MatchInfo?){
        iv_live_type.visibility = View.VISIBLE
        if (matchInfo == null) {
            return
        }

        val it = matchInfo
        if (!it.pullRtmpUrl.isNullOrEmpty()) {
            iv_publicity.setUp(it.pullRtmpUrl, false, "");
        } else if (!it.pullFlvUrl.isNullOrEmpty()) {
            iv_publicity.setUp(it.pullFlvUrl, false, "");
        }
        if (!it.pullRtmpUrl.isNullOrEmpty()||!it.pullFlvUrl.isNullOrEmpty()) {
            iv_publicity.startPlayLogic()
        }
    }

     @Subscribe(threadMode = ThreadMode.MAIN)
     fun refreshMoney(moneyEvent: MoneyEvent){
         if (moneyEvent.refresh) {
             viewModel.getMoney()
         }
     }
    private fun onRefreshMoney(){
        iv_money_refresh.startAnimation(RotateAnimation(0f,
            720f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f).apply {
            duration = 1000
        })
        viewModel.getMoney()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        iv_publicity.release()
    }

    override fun onStartPrepared() {
        iv_live_type.visibility = View.VISIBLE
    }
    override fun onPrepared() {
        iv_live_type.visibility = View.INVISIBLE
    }

    override fun onError() {
        iv_live_type.visibility = View.VISIBLE
    }

}
