package org.cxct.sportlottery.ui.maintab

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.gyf.immersionbar.ImmersionBar
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import kotlinx.android.synthetic.main.fragment_main_home.*
import kotlinx.android.synthetic.main.view_home_menu_game.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
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
import org.cxct.sportlottery.ui.sport.search.SportSearchtActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.widget.DepthPageTransformer
import org.cxct.sportlottery.widget.HomeBannerIndicator
import org.greenrobot.eventbus.EventBus
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainHomeFragment() : BaseBottomNavigationFragment<SportViewModel>(SportViewModel::class) {

    companion object {
        fun newInstance(): MainHomeFragment {
            val args = Bundle()
            val fragment = MainHomeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val mPublicityVersionUpdateViewModel: VersionUpdateViewModel by viewModel()
    private lateinit var mainHomeMenuAdapter: MainHomeMenuAdapter
    private lateinit var mRecommendList: List<Recommend>
    private val homeRecommendAdapter by lazy {
        HomeRecommendAdapter(
            HomeRecommendAdapter.HomeRecommendListener(
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
                    mRecommendList.find {
                        TextUtils.equals(matchId, it.id)
                    }?.let { recommend ->
                        recommend.matchInfo?.let {
                            navOddsDetailFragment(recommend.matchType!!, it)
                        }
                    }
                }, onClickPlayTypeListener = { gameType, matchType, matchId, matchInfoList ->
                    checkCreditSystemLogin {
                        matchInfoList.find {
                            TextUtils.equals(matchId, it.id)
                        }?.let {
                            navOddsDetailFragment(matchType!!, it)
                        }
                    }
                }, onClickLiveIconListener = { gameType, matchType, matchId, matchInfoList ->
                    if (viewModel.checkLoginStatus()) {
                        matchInfoList.find {
                            TextUtils.equals(matchId, it.id)
                        }?.let {
                            navOddsDetailFragment(matchType!!, it)
                        }
                    }
                },
                onClickAnimationIconListener = { gameType, matchType, matchId, matchInfoList ->
                    if (viewModel.checkLoginStatus()) {
                        matchInfoList.find {
                            TextUtils.equals(matchId, it.id)
                        }?.let {
                            navOddsDetailFragment(matchType!!, it)
                        }
                    }
                }
            )
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
        initView()
        initObservable()
        queryData()
        initSocketObservers()
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()
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
    }

    fun initToolBar() {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        iv_menu_left.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(true))
        }
        btn_login.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
        lin_search.setOnClickListener {
            startActivity(Intent(requireActivity(), SportSearchtActivity::class.java))
        }
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
                mRecommendList = recommendList
                val adapterRecommendListData = homeRecommendAdapter.getRecommendListData()
                recommendList.forEach { recommend ->
                    adapterRecommendListData.firstOrNull { adapterRecommend -> adapterRecommend.id == recommend.id }
                        ?.let { oldRecommend ->
                            recommend.matchInfo?.status = oldRecommend.matchInfo?.status
                            recommend.matchInfo?.statusName18n =
                                oldRecommend.matchInfo?.statusName18n
                            recommend.matchInfo?.socketMatchStatus =
                                oldRecommend.matchInfo?.socketMatchStatus
                            recommend.matchInfo?.leagueTime = oldRecommend.matchInfo?.leagueTime
                            recommend.runningTime = oldRecommend.runningTime
                        }
                }
                //新版宣傳頁
                if (recommendList.isEmpty()) return@observe //推薦賽事為empty不顯示
                homeRecommendAdapter.setupRecommendItem(recommendList)
                //先解除全部賽事訂
                unSubscribeChannelHallAll()
                subscribeQueryData(recommendList)
            }
        }

        viewModel.betInfoList.observe(viewLifecycleOwner) { event ->
            event.peekContent().let { betList ->
                val targetList = homeRecommendAdapter.getRecommendListData()
                targetList.forEachIndexed { index, recommend ->
                    var needUpdate = false
                    recommend.oddsMap?.values?.forEach { oddList ->
                        oddList?.forEach { odd ->
                            val newSelectStatus = betList.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd?.id
                            }
                            if (odd?.isSelected != newSelectStatus) {
                                odd?.isSelected = newSelectStatus
                                needUpdate = true
                            }
                        }
                    }
                    if (needUpdate) {
                        updateRecommend(targetList)
                    }
                }
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
            if (!isCreditSystem())
                if (it.isNotEmpty()) {
                    lin_activity.visibility = View.VISIBLE
                    setupActivity(it)
                } else {
                    lin_activity.visibility = View.GONE
                }
        }

        viewModel.publicityMenuData.observe(viewLifecycleOwner) {
            setupType(it)
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
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getRecommend()
        }
    }

    private fun queryData() {
        mPublicityVersionUpdateViewModel.checkAppVersion()
        viewModel.getPublicitySportMenu()
        viewModel.getAnnouncement()
        viewModel.getConfigData()
        viewModel.getRecommend()
        viewModel.getMenuThirdGame()
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
            .setIndicator(HomeBannerIndicator(requireContext()));
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
        banner_activity.addBannerLifecycleObserver(this) //添加生命周期观察者
            .setAdapter(HomeActivityAdapter(list))
            .setOnBannerListener { data, position ->
                data?.let {
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        Constants.getPromotionDetailUrl(
                            viewModel.token,
                            (data as PublicityPromotionItemData).id,
                            LanguageManager.getSelectLanguage(requireContext())
                        ),
                        getString(R.string.promotion))
                }
            }
    }
    private fun setupType(publicityMenuData: PublicityMenuData) {
        rg_type.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                when (checkedId) {
                    R.id.rbtn_sport -> {
                        lin_menu_game.isVisible = false
                        rv_type_list.isVisible = true
                    }
                    R.id.rbtn_egame -> {
                        rv_type_list.isVisible = false
                        lin_menu_game.isVisible = true
                    }
                }
            }
        })
        mainHomeMenuAdapter = MainHomeMenuAdapter(mutableListOf())
        var rvChiild = rv_type_list.getChildAt(0) as RecyclerView
        rvChiild.setPadding(0, 0, 40.dp, 0)
        rvChiild.clipToPadding = false
        rv_type_list.offscreenPageLimit = 3
        rv_type_list.setPageTransformer(DepthPageTransformer())
        rv_type_list.adapter = mainHomeMenuAdapter
        rv_type_list.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == mainHomeMenuAdapter.itemCount - 1) {
                    rv_type_list.currentItem = mainHomeMenuAdapter.itemCount - 2
                }
            }
        })
        mainHomeMenuAdapter.setOnItemClickListener { adapter, view, position ->
            publicityMenuData.sportMenuDataList?.let {
                enterTheSport(it[position])
            }
        }

        publicityMenuData?.sportMenuDataList?.let {
            mainHomeMenuAdapter.setNewData(it.toMutableList())
            mainHomeMenuAdapter.removeAllFooterView()
            mainHomeMenuAdapter.addFooterView(LayoutInflater.from(requireContext())
                .inflate(R.layout.item_main_home_empty, null))
            mainHomeMenuAdapter.footerLayout.apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
            }
        }
        lin_menu_game.apply {
            ivThirdGame.setImageResource(R.drawable.bg_egame)
            ivThirdGame.setOnClickListener {
                publicityMenuData?.eGameMenuData?.let { thirdDictValues ->
                    if (viewModel.isLogin.value != true) {
                        (activity as MainTabActivity).showLoginNotify()
                    } else {
                        viewModel.requestEnterThirdGame(thirdDictValues)
                    }
                }
            }
        }

    }

    // TODO subscribe leagueChange: 此處尚無需實作邏輯, 看之後有沒有相關需求
    private fun initSocketObservers() {
        receiver.serviceConnectStatus.observe(viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
//                    loading()
                    subscribeSportChannelHall()
                }
            }
        }

        receiver.matchStatusChange.observe(viewLifecycleOwner) { event ->
            event?.let { matchStatusChangeEvent ->
                val targetList = homeRecommendAdapter.getRecommendListData()
                var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

                targetList.forEachIndexed { index, recommend ->
                    val matchList = listOf(recommend).toMutableList()
                    if (SocketUpdateUtil.updateMatchStatus(
                            recommend.gameType,
                            matchList as MutableList<org.cxct.sportlottery.network.common.MatchOdd>,
                            matchStatusChangeEvent,
                            context
                        )
                    ) {
                        needUpdate = true
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
                if (needUpdate) {
                    updateRecommend(targetList)
                }
            }
        }

        receiver.matchClock.observe(viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val targetList = homeRecommendAdapter.getRecommendListData()
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
                    updateRecommend(targetList)
                }
            }
        }

        receiver.oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            val targetList = homeRecommendAdapter.getRecommendListData()
            var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

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
                            needUpdate = true
                        }
                    }
                }
                if (needUpdate) {
                    updateRecommend(targetList)
                }
        }

        receiver.matchOddsLock.observe(viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                val targetList = homeRecommendAdapter.getRecommendListData()
                var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(recommend, matchOddsLockEvent)
                    ) {
                        needUpdate = true
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }

                if (needUpdate) {
                    updateRecommend(targetList)
                }
            }
        }

//        receiver.leagueChange.observe(viewLifecycleOwner) {
//            it?.let { leagueChangeEvent ->
//                viewModel.publicityLeagueChange(leagueChangeEvent)
//            }
//        }

        receiver.globalStop.observe(viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                val targetList = homeRecommendAdapter.getRecommendListData()
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

                if (needUpdate) {
                    updateRecommend(targetList)
                }
            }
        }

        receiver.producerUp.observe(viewLifecycleOwner) {
            it?.let {
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(homeRecommendAdapter.getRecommendListData())
            }
        }

        receiver.closePlayCate.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                homeRecommendAdapter.getRecommendListData().forEach { recommend ->
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

    private fun initRecommendView() {
        with(rv_recommend) {
            if (layoutManager == null) {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
            if (adapter == null) {
                adapter = homeRecommendAdapter
            }
        }
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
                getString(R.string.error),
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
        btn_login.visibility = if (viewModel.isLogin.value == true) View.GONE else View.VISIBLE
    }

    private fun updateRecommend(recommendList: List<Recommend>) {
        viewModel.oddsType.value?.let {
            homeRecommendAdapter.setupRecommendItem(recommendList = recommendList)
        }
    }

}
