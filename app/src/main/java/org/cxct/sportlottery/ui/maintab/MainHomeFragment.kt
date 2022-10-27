package org.cxct.sportlottery.ui.maintab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.gyf.immersionbar.ImmersionBar
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import kotlinx.android.synthetic.main.fragment_main_home.*
import kotlinx.android.synthetic.main.hot_card_game_include.*
import kotlinx.android.synthetic.main.hot_gaming_include.*
import kotlinx.android.synthetic.main.hot_handicap_include.*
import kotlinx.android.synthetic.main.hot_live_match_include.*
import kotlinx.android.synthetic.main.item_hot_handicap.*
import kotlinx.android.synthetic.main.item_hot_handicap.rv_handicap_item
import kotlinx.android.synthetic.main.tab_item_home_open.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
import kotlinx.android.synthetic.main.view_toolbar_home.btn_login
import kotlinx.android.synthetic.main.view_toolbar_home.btn_register
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.index.home.HomeLiveData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.network.third_game.third_games.hot.HandicapData
import org.cxct.sportlottery.network.third_game.third_games.hot.HotMatchLiveData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.publicity.PublicityAnnouncementMarqueeAdapter
import org.cxct.sportlottery.ui.game.publicity.PublicityMenuData
import org.cxct.sportlottery.ui.game.publicity.PublicityPromotionItemData
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.news.NewsActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.widget.HomeBannerIndicator
import org.greenrobot.eventbus.EventBus
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainHomeFragment :
    BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {
    private  var tabSelectTitleList = mutableListOf<String>()
    private  var tabSelectIconList = mutableListOf<Int>()
    private  var tabUnSelectIconList = mutableListOf<Int>()
    private var hotDataList = mutableListOf<HotMatchLiveData>()


    private var hotHandicapList = mutableListOf<HandicapData>()

    companion object {
        fun newInstance(): MainHomeFragment {
            val args = Bundle()
            val fragment = MainHomeFragment()
            fragment.arguments = args
            return fragment
        }
    }
    private val homeHotLiveAdapter by lazy {//热门直播
        HotLiveAdapter(HotLiveAdapter.ItemClickListener{ data ->

            tv_first_half_game.text = "data.half"
            tv_match_time.text = "12:00"
            tv_match_name.text = data.league.name
            tv_match_type_name.text = data.sportName
            context?.let {
                Glide.with(it)
                    .load(data.matchInfo.frontCoverUrl)
                    .apply(RequestOptions().placeholder(R.drawable.img_avatar_default))
                    .into(iv_live_type)
                Glide.with(it)
                    .load(data.matchInfo.streamerIcon)
                    .apply(RequestOptions().placeholder(R.drawable.img_avatar_default))
                    .into(iv_avatar_live)
            }
            tv_introduction.text = data.matchInfo.StreamerName
        })
    }

    private val hotHandicapAdapter by lazy {
        HotHandicapAdapter(mutableListOf())
    }
    private val hotElectronicAdapter by lazy{//电子
        HomeElectronicAdapter(mutableListOf())
    }
    private val homeChessAdapter by lazy{//棋牌
        HomeChessAdapter(mutableListOf())
    }
    private val mPublicityVersionUpdateViewModel: VersionUpdateViewModel by viewModel()
    private lateinit var mainHomeMenuAdapter: MainHomeMenuAdapter
    private val homeRecommendAdapter by lazy {
        HomeRecommendAdapter(
            HomeRecommendListener(
                onItemClickListener = {
                    goLoginPage()
                },
                onGoHomePageListener = {
                    checkCreditSystemLogin { goGamePage() }
                },
                onClickBetListener = { gameType, matchType, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap, playCateMenuCode ->
                    if (mIsEnabled) {
                        avoidFastDoubleClick()
                        addOddsDialog(
                            gameType,
                            matchType,
                            matchInfo,
                            odd,
                            playCateCode,
                            playCateName,
                            betPlayCateNameMap,
                            playCateMenuCode
                        )
                    }
                },
                onClickFavoriteListener = {
                    viewModel.pinFavorite(FavoriteType.MATCH, it)
                },
                onClickStatisticsListener = { matchId ->

                }, onClickPlayTypeListener = { gameType, matchType, matchId, matchInfoList ->
                    checkCreditSystemLogin {
                        matchInfoList.find {
                            TextUtils.equals(matchId, it.id)
                        }?.let {
                            navOddsDetailFragment(matchType!!, it)
                        }
                    }
                },onClickLiveIconListener = {gameType, matchType, matchId, matchInfoList ->

                }
            ) { gameType, matchType, matchId, matchInfoList ->
                if (viewModel.checkLoginStatus()) {
                    matchInfoList.find {
                        TextUtils.equals(matchId, it.id)
                    }?.let {
                        navOddsDetailFragment(matchType!!, it)
                    }
                }
            }
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_main_home, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getConfigData()
        viewModel.getGameEntryConfig(1, null)
        viewModel.getHandicapConfig(1)
        viewModel.getHotLiveList()
        initView()
        initObservable()
        queryData()
        initSocketObservers()
//        getTabDate()
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()
        viewModel.getRecommend()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getRecommend()
        }
    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()
    }

    private fun initView() {
        initToolBar()
        iv_customer_service.setOnClickListener {
            clickCustomService(requireContext(), childFragmentManager)
        }
        initRecommendView()
        showChangeFragment()
        getTabDate()
        nsv_home.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener {
                _, _, scrollY, _, oldScrollY ->
            ll_come_back.visibility = if (scrollY > 800) View.VISIBLE else View.GONE
        })
        ll_come_back.setOnClickListener {
            nsv_home.post{
                nsv_home.fullScroll(ScrollView.FOCUS_UP)
            }
        }
        ll_hot_live_more.setOnClickListener {
            (parentFragment as HomeFragment).onTabClickByPosition(1)
        }
        ll_hot_handicap_more.setOnClickListener {
            (parentFragment as HomeFragment).onTabClickByPosition(2)
        }
        ll_hot_elect.setOnClickListener {
            (parentFragment as HomeFragment).onTabClickByPosition(5)
        }
        ll_poker_more.setOnClickListener {
            (parentFragment as HomeFragment).onTabClickByPosition(4)
        }
    }

    fun initToolBar() {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        iv_menu_left.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(true))
            (activity as MainTabActivity).showLeftFrament(0)
        }
        btn_register.setOnClickListener {
            startActivity(Intent(requireActivity(), RegisterOkActivity::class.java))
        }
        btn_login.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
//        lin_search.setOnClickListener {
//            startActivity(Intent(requireActivity(), SportSearchtActivity::class.java))
//        }
        setupLogin()
    }
    private fun initObservable() {
        if(viewModel == null){
            return
        }
        viewModel.isLogin.observe(viewLifecycleOwner) {
            setupLogin()
        }
        viewModel.userInfo.observe(viewLifecycleOwner) {
//            val newDiscount = userInfo?.discount ?: 1.0F
//            viewModel.publicityUpdateDiscount(mPublicityAdapter.discount, newDiscount)
//            mPublicityAdapter.discount = newDiscount
        }
        viewModel.userMoney.observe(viewLifecycleOwner) {

        }
        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                homeRecommendAdapter.oddsType = oddsType
            }
        }
        viewModel.publicityRecommend.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { recommendList ->
                hideLoading()
                if (recommendList.isEmpty()) return@observe //推薦賽事為empty不顯示
                recommendList.forEach { recommend ->
                    // 將儲存的賠率表指定的賽事列表裡面
                    val leagueOddFromMap = leagueOddMap[recommend.leagueId]
                    leagueOddFromMap?.let {
                        recommend.oddsMap = leagueOddFromMap.oddsMap
                    }
                }
                homeRecommendAdapter.data = recommendList

                //先解除全部賽事訂
                unSubscribeChannelHallAll()
                subscribeQueryData(recommendList)
            }
        }

        viewModel.betInfoList.observe(viewLifecycleOwner) { event ->
            event.peekContent().let { betInfoList ->
                homeRecommendAdapter.betInfoList = betInfoList
            }
        }
        viewModel.gotConfig.observe(viewLifecycleOwner) { event ->
            event.peekContent().let { isReload ->
                if (isReload) {
                    setupBanner()
                    viewModel.getPublicityPromotion()
                }
                viewModel.getSportMenuFilter()
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
//        //文字跑马灯
//        viewModel.publicityPromotionAnnouncementList.observe(viewLifecycleOwner) {
//            //非信用盤才顯示優惠活動跑馬燈
//            if (!isCreditSystem())
//                if (it.isNotEmpty()) mPublicityAdapter.addPromotionAnnouncementList(it)
//        }
//
        viewModel.publicityPromotionList.observe(viewLifecycleOwner) {
            //非信用盤才顯示優惠活動
//            if (!isCreditSystem())
//                if (it.isNotEmpty()) {
//                    lin_activity.visibility = View.VISIBLE
//                    setupActivity(it)
//                } else {
//                    lin_activity.visibility = View.GONE
//                }
        }

        viewModel.publicityMenuData.observe(viewLifecycleOwner) {
            // setupType(it)
        }
        viewModel.homeGameData.observe(viewLifecycleOwner) {

        }
//
        viewModel.enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisible)
                enterThirdGame(it)
        }
//
//        viewModel.errorPromptMessage.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()
//                ?.let { message -> showErrorPromptDialog(getString(R.string.prompt), message) {} }
//        }
//
//        mPublicityVersionUpdateViewModel.appVersionState.observe(viewLifecycleOwner) {
//            viewModel.updateMenuVersionUpdatedStatus(it)
//        }

        viewModel.homeGameData.observe(viewLifecycleOwner) {
            it?.let { gameList->
                //棋牌
                val mHotChessList = gameList.filter { data->
                    data.gameType?.equals("1") == true
                }
                homeChessAdapter.setNewData(mHotChessList)

                //电子
                val mHotelList = gameList.filter {data->
                    data.gameType?.equals("2") == true
                }
                hotElectronicAdapter.setNewData(mHotelList)
            }
        }
        viewModel.hotLiveData.observe(viewLifecycleOwner){ list->
            if (list.isNullOrEmpty()){
                hot_live_match.visibility = View.GONE
            }else{
                list[0].apply {
                    tv_match_name.text = matchInfo.leagueName?:""
                        tv_match_type_name.text = sportName
                        tv_first_half_game.text = "half"
                        tv_match_time.text = "12:00"

                        context?.let {mContext->
                            Glide.with(mContext)
                                .load(matchInfo.frontCoverUrl)
                                .apply(RequestOptions().placeholder(R.drawable.img_avatar_default))
                                .into(iv_live_type)
                            Glide.with(mContext)
                                .load(matchInfo.streamerIcon)
                                .apply(RequestOptions().placeholder(R.drawable.img_avatar_default))
                                .into(iv_avatar_live)

                        }

                        tv_introduction.text = matchInfo.StreamerName
                }
                    homeHotLiveAdapter.data = list
                    rv_match_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                    rv_match_list.adapter = homeHotLiveAdapter
            }
        }
    }

    //用户缓存最新赔率，方便当从api拿到新赛事数据时，赋值赔率信息给新赛事
    private val leagueOddMap = HashMap<String, Recommend>()
    private fun initSocketObservers() {
        receiver.serviceConnectStatus.observe(viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    subscribeSportChannelHall()
                    viewModel.getRecommend()
                }
            }
        }

        receiver.matchStatusChange.observe(viewLifecycleOwner) { event ->
            event?.let { matchStatusChangeEvent ->
                val targetList = homeRecommendAdapter.data
                var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

                targetList.forEachIndexed { index, recommend ->
                    val matchList = listOf(recommend).toMutableList()
                    if (SocketUpdateUtil.updateMatchStatus(
                            recommend.gameType,
                            matchList as MutableList<MatchOdd>,
                            matchStatusChangeEvent,
                            context
                        )
                    ) {
                        needUpdate = true
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
                if (needUpdate) {
                    homeRecommendAdapter.data = targetList
                }
            }
        }

        receiver.matchClock.observe(viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val targetList = homeRecommendAdapter.data
                var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

                targetList.forEachIndexed { index, recommend ->
                    if (
                        SocketUpdateUtil.updateMatchClock(
                            recommend,
                            matchClockEvent
                        )
                    ) {
                        needUpdate = true
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }

                if (needUpdate) {
                    homeRecommendAdapter.data = targetList
                }
            }
        }
        receiver.matchOddsChange.observe(viewLifecycleOwner) {

        }
        receiver.oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            val targetList = homeRecommendAdapter.data
                targetList.forEachIndexed { index, recommend ->
                    if (recommend.id == oddsChangeEvent.eventId) {
                        recommend.sortOddsMap()
                        //region 翻譯更新
                        oddsChangeEvent.playCateNameMap?.let { playCateNameMap ->
                            recommend.playCateNameMap?.putAll(playCateNameMap)
                        }
                        oddsChangeEvent.betPlayCateNameMap?.let { betPlayCateNameMap ->
                            recommend.betPlayCateNameMap?.putAll(betPlayCateNameMap)
                        }
                        //endregion
                        if (SocketUpdateUtil.updateMatchOdds(context, recommend, oddsChangeEvent)) {
                            updateBetInfo(recommend, oddsChangeEvent)
                            leagueOddMap[recommend.leagueId] = recommend
                            homeRecommendAdapter.notifyItemChanged(index, recommend)
                        }
                    }
                }
        }

        receiver.matchOddsLock.observe(viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                val targetList = homeRecommendAdapter.data
                var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(recommend, matchOddsLockEvent)
                    ) {
                        needUpdate = true
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }

//                if (needUpdate) {
//                    homeRecommendAdapter.data = targetList
//                }
            }
        }

//        receiver.leagueChange.observe(viewLifecycleOwner) {
//            it?.let { leagueChangeEvent ->
//                viewModel.publicityLeagueChange(leagueChangeEvent)
//            }
//        }

        receiver.globalStop.observe(viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                val targetList = homeRecommendAdapter.data
                var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(
                            recommend,
                            globalStopEvent
                        )
                    ) {
                        needUpdate = true
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }

//                if (needUpdate) {
//                    homeRecommendAdapter.data = targetList
//                }
            }
        }

        receiver.producerUp.observe(viewLifecycleOwner) {
            it?.let {
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(homeRecommendAdapter.data)
            }
        }

        receiver.closePlayCate.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                homeRecommendAdapter.data.forEach { recommend ->
                    if (recommend.gameType == it.gameType) {
                        recommend.oddsMap?.forEach { map ->
                            if (map.key == it.playCateCode) {
                                map.value?.forEach { odd ->
                                    odd?.status = BetStatus.DEACTIVATED.code
                                }
                            }
                        }
                    }
                }
                homeRecommendAdapter.notifyDataSetChanged()
            }
        }

    }

    private fun Recommend.sortOddsMap() {
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
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_image_load)
            .error(R.drawable.ic_image_broken)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontTransform()

        var imageList = sConfigData?.imageList?.filter {
            it.imageType == 2
        }
        (banner as Banner<ImageData, BannerImageAdapter<ImageData>>)
            .setAdapter(object : BannerImageAdapter<ImageData>(imageList) {
                override fun onBindView(
                    holder: BannerImageHolder,
                    data: ImageData?,
                    position: Int,
                    size: Int,
                ) {

                    iv_right.setOnClickListener {
                        if (position<size-1){
                            position+1
                            LogUtil.d("$size")
                            LogUtil.d("$position")
                        }else{
                            position-(size-1)
                            LogUtil.d("$position")
                        }
                    }

                    val url = sConfigData?.resServerHost + data?.imageName1
                    Glide.with(holder.itemView)
                        .load(url)
                        .apply(requestOptions)
                        .into(holder.imageView)
                    holder.imageView.setOnClickListener {
                        data?.imageLink?.let {
                            JumpUtil.toExternalWeb(requireContext(), it)
                        }
                    }
                }
            })
        //    .setIndicator(HomeBannerIndicator(requireContext()));
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

    private fun setupActivity(list: List<PublicityPromotionItemData>) {
//        banner_activity.addBannerLifecycleObserver(this) //添加生命周期观察者
//            .setAdapter(HomeActivityAdapter(list))
//            .setOnBannerListener { data, position ->
//                data?.let {
//                    JumpUtil.toInternalWeb(
//                        requireContext(),
//                        Constants.getPromotionDetailUrl(
//                            viewModel.token,
//                            (data as PublicityPromotionItemData).id,
//                            LanguageManager.getSelectLanguage(requireContext())
//                        ),
//                        getString(R.string.promotion))
//                }
//            }
    }
    private fun setupType(publicityMenuData: PublicityMenuData) {
//        rg_type.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
//            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
//                when (checkedId) {
//                    R.id.rbtn_sport -> {
//                        lin_menu_game.isVisible = false
//                        rv_type_list.isVisible = true
//                    }
//                    R.id.rbtn_egame -> {
//                        rv_type_list.isVisible = false
//                        lin_menu_game.isVisible = true
//                    }
//                }
//            }
//        })
//        mainHomeMenuAdapter = MainHomeMenuAdapter(mutableListOf())
//        var rvChiild = rv_type_list.getChildAt(0) as RecyclerView
//        rvChiild.setPadding(0, 0, 40.dp, 0)
//        rvChiild.clipToPadding = false
//        rv_type_list.offscreenPageLimit = 3
//        rv_type_list.setPageTransformer(DepthPageTransformer())
//        rv_type_list.adapter = mainHomeMenuAdapter
//        rv_type_list.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                if (position == mainHomeMenuAdapter.itemCount - 1) {
//                    rv_type_list.currentItem = mainHomeMenuAdapter.itemCount - 2
//                }
//            }
//        })
//        mainHomeMenuAdapter.setOnItemClickListener { adapter, view, position ->
//            publicityMenuData.sportMenuDataList?.let {
//                enterTheSport(it[position])
//            }
//        }
//
//        publicityMenuData?.sportMenuDataList?.let {
//            mainHomeMenuAdapter.setNewData(it.toMutableList())
//            mainHomeMenuAdapter.removeAllFooterView()
//            mainHomeMenuAdapter.addFooterView(LayoutInflater.from(requireContext())
//                .inflate(R.layout.item_main_home_empty, null))
//            mainHomeMenuAdapter.footerLayout.apply {
//                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT)
//            }
//        }
//        lin_menu_game.apply {
//            ivThirdGame.setImageResource(R.drawable.bg_egame)
//            ivThirdGame.setOnClickListener {
//                if (viewModel.isLogin.value != true) {
//                    (activity as MainTabActivity).showLoginNotify()
//                } else {
//                    viewModel.requestEnterThirdGame(publicityMenuData?.eGameMenuData)
//                }
//            }
//        }

    }

    private fun initRecommendView() {
//        with(rv_recommend) {
//            if (layoutManager == null) {
//                layoutManager =
//                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//            }
//            if (adapter == null) {
//                adapter = homeRecommendAdapter
//            }
//        }
    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(recommend: Recommend, oddsChangeEvent: OddsChangeEvent) {
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

    private fun goLoginPage() {
        startActivity(Intent(context, LoginActivity::class.java))
    }


    private fun goGamePage() {
        GameActivity.reStart(activity ?: requireActivity())
        activity?.finish()
    }

    private fun addOddsDialog(
        gameTypeCode: String,
        matchType: MatchType,
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        playCateMenuCode: String?,
    ) {
        val gameType = GameType.getGameType(gameTypeCode)
        gameType?.let {
            matchInfo?.let { matchInfo ->
                val fastBetDataBean = FastBetDataBean(
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
                )
                when (val fragmentActivity = activity) {
                    is MainTabActivity -> fragmentActivity.setupBetData(fastBetDataBean)
                }
            }
        }
    }


    private fun navOddsDetailFragment(
        matchType: MatchType,
        matchInfo: MatchInfo,
    ) {
        SportDetailActivity.startActivity(requireContext(),
            matchInfo = matchInfo,
            matchType = matchType)
    }

    /**
     * 根據menuList的PlayCate排序賠率玩法
     */
    //TODO 20220323 等新版socket更新方式調整完畢後再確認一次此處是否需要移動至別處進行
    private fun Recommend.sortOddsByMenu() {
        val sortOrder = this.menuList.firstOrNull()?.playCateList?.map { it.code }

        oddsMap?.let { map ->
            val filterPlayCateMap = map.filter { sortOrder?.contains(it.key) == true }
            val sortedMap = filterPlayCateMap.toSortedMap(compareBy<String> {
                sortOrder?.indexOf(it)
            }.thenBy { it })

            map.clear()
            map.putAll(sortedMap)
        }
    }

    private fun subscribeQueryData(recommendList: List<Recommend>) {
        recommendList.forEach { subscribeChannelHall(it) }
    }

    private fun subscribeChannelHall(recommend: Recommend) {
        subscribeChannelHall(recommend.gameType, recommend.id)
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
                Intent(
                    context,
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
            sportMenu.entranceType?.let {
                jumpToTheSport(it, sportMenu.gameType)
            }
        } else {
            viewModel.setSportClosePromptMessage(
                MultiLanguagesApplication.appContext.getString(
                    sportMenu.gameType.string
                )
            )
        }
    }

    /**
     * 跳轉至體育指定球種
     */
    private fun jumpToTheSport(matchType: MatchType, gameType: GameType) {
        (activity as MainTabActivity).jumpToTheSport(matchType, gameType)
    }

    private fun setupLogin() {
        viewModel.isLogin.value?.let {
            btn_register.isVisible = !it
            btn_login.isVisible = !it
//            lin_search.visibility = if (it) View.VISIBLE else View.INVISIBLE
            ll_user_money.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }

    //获取tab数据
    private fun getTabDate(){

        //标题数据
        tabSelectTitleList.add("推荐")
        tabSelectTitleList.add("直播")
        tabSelectTitleList.add("体育")
        tabSelectTitleList.add("世界杯")
        tabSelectTitleList.add("棋牌")
        tabSelectTitleList.add("电子")
        //选中图片
        tabSelectIconList.add(R.drawable.icon_recommend)
        tabSelectIconList.add(R.drawable.live1)
        tabSelectIconList.add(R.drawable.sport1)
        tabSelectIconList.add(R.drawable.word_cup1)
        tabSelectIconList.add(R.drawable.live1)
        tabSelectIconList.add(R.drawable.sport1)
        //未选中图片
        tabUnSelectIconList.add(R.drawable.icon_un_recommend)
        tabUnSelectIconList.add(R.drawable.live0)
        tabUnSelectIconList.add(R.drawable.sport0)
        tabUnSelectIconList.add(R.drawable.word_cup0)
        tabUnSelectIconList.add(R.drawable.live0)
        tabUnSelectIconList.add(R.drawable.sport0)




        initListView()
    }

    fun initListView(){

            //热门电子游戏
        with(rv_egame){
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            }
            if (adapter == null) {
                adapter = hotElectronicAdapter
            }

            hotElectronicAdapter.setOnItemClickListener{adapter, view, position ->
                //点击跳转到哪里
                ToastUtil.showToast(activity,"电子$position")
            }
        }
        //棋牌
        with(rv_chess){
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            }
            if (adapter == null) {
                adapter = homeChessAdapter
            }

            homeChessAdapter.setOnItemClickListener { adapter, view, position ->
                //点击跳转到哪里
                ToastUtil.showToast(activity,"棋牌$position")
            }
        }
        with(rv_hot_handicap){
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            }
            if (adapter == null) {
                adapter = hotHandicapAdapter
            }
            hotHandicapAdapter.setNewData(hotHandicapList)
        }
    }

    //切换fragment
    fun showChangeFragment() {
        //点击直播跳转
        include_layout1.setOnClickListener {
            (parentFragment as HomeFragment).onTabClickByPosition(1)
        }
        //点击体育跳转
        include_layout2.setOnClickListener {
            (parentFragment as HomeFragment).onTabClickByPosition(2)
        }
        //点击世界杯跳转
        include_layout3.setOnClickListener {
            (parentFragment as HomeFragment).onTabClickByPosition(3)
        }
        //点击滚球跳转
        include_layout4.setOnClickListener {
//            ll_home_content.visibility = View.GONE
//            home_main_fragment.visibility = View.VISIBLE
//            childFragmentManager.beginTransaction()
//                .replace(R.id.home_main_fragment, HomeLiveFragment.newInstance())
//                .commit()
            (activity as MainTabActivity).jumpToTheSport(MatchType.IN_PLAY, GameType.FT)
        }
        //点击电子跳转
        include_layout5.setOnClickListener {
            (parentFragment as HomeFragment).onTabClickByPosition(4)
        }
        //点击棋牌跳转
        include_layout6.setOnClickListener {
            (parentFragment as HomeFragment).onTabClickByPosition(5)
        }

    }

}
