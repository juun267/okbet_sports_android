package org.cxct.sportlottery.ui.game.publicity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentPublicityBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.CustomSecurityDialog
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameActivity.Companion.ARGS_PUBLICITY_SPORT_ENTRANCE
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.profileCenter.AppearanceActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

//TODO 推薦賽事點擊更新、優惠活動清單payloads更新
/**
 * @app_destination 宣傳頁
 */
class PublicityNewFragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class), View.OnClickListener {
    //TODO 新首頁的版本更新為暫時顯示於此處代替真人遊戲, 待往後需求有真人遊戲時會將版本更新移除, 故先暫時直接使用VersionUpdateViewModel不將其重構為Rpository
    private val mPublicityVersionUpdateViewModel: VersionUpdateViewModel by viewModel()

    // This property is only valid between onCreateView and onDestroyView.
    private var _binding: FragmentPublicityBinding? = null

    private val binding get() = _binding!!
    private var customSecurityDialog: CustomSecurityDialog? = null

    private var isNewestDataFromApi = false
    private var mRecommendList: List<Recommend> = listOf()
    private val mPublicityAdapter =
        GamePublicityNewAdapter(
            GamePublicityNewAdapter.PublicityAdapterNewListener(
                onLogoClickListener = {
                    removeBetListFragment()
//                    if (sConfigData?.thirdOpen == FLAG_OPEN) {
//                        MainActivity.reStart(activity ?: requireActivity())
//                    }
                },
                onLanguageBlockClickListener = {
                    goSwitchLanguagePage()
                },
                onNoticeClickListener = {
                    clickNotice()
                },
                onMenuClickListener = {
                    clickMenu()
                },
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
                    showStatistics(matchId)
                }, onClickPlayTypeListener = { gameType, matchType, matchId, matchInfoList ->
                    checkCreditSystemLogin { navOddsDetailFragment(gameType, matchType, matchId, matchInfoList) }
                }, onClickLiveIconListener = { gameType, matchType, matchId, matchInfoList ->
                    if (viewModel.checkLoginStatus()) {
                        navOddsDetailFragment(gameType, matchType, matchId, matchInfoList)
                    }
                },
                onClickAnimationIconListener = { gameType, matchType, matchId, matchInfoList ->
                    if (viewModel.checkLoginStatus()) {
                        navOddsDetailFragment(gameType, matchType, matchId, matchInfoList)
                    }
                },
                //新版宣傳頁
                onGoNewsPageListener = {
                    clickNews()
                },
                onSportMenuListener = { sportMenu ->
                    checkCreditSystemLogin { enterTheSport(sportMenu) }
                },
                //第三方遊戲跳轉
                onGoThirdGamesListener = {
                    avoidFastDoubleClick()
                    if (viewModel.isLogin.value != true) {
                        showLoginNotify()
                    } else {
                        viewModel.requestEnterThirdGame(it)
                    }
                },
                onClickFAQsListener = {
                    context?.let {
                        JumpUtil.toInternalWeb(
                            it,
                            Constants.getFAQsUrl(it),
                            resources.getString(R.string.faqs)
                        )
                    }
                },
                onClickAffiliateListener = {
                    context?.let {
                        JumpUtil.toInternalWeb(
                            it,
                            Constants.getAffiliateUrl(it),
                            resources.getString(R.string.btm_navigation_affiliate)
                        )
                    }
                },
                onClickContactListener = {
                    context?.let {
                        clickCustomService(it, parentFragmentManager)
                    }
                },
                onClickPromotionListener = {
                    context?.let {
                        JumpUtil.toInternalWeb(
                            it,
                            Constants.getPromotionUrl(
                                viewModel.token,
                                LanguageManager.getSelectLanguage(it)
                            ),
                            getString(R.string.promotion)
                        )
                    }
                },
                onClickVersionUpdateListener = {
                    startActivity(Intent(activity, VersionUpdateActivity::class.java))
                },
                onClickAppearanceListener = {
                    startActivity(Intent(activity, AppearanceActivity::class.java))
                }
            )
        )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPublicityBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getConfigData()
        initOnClickListener()
        initRecommendView()
        initTitle()
        initBottomView()
        initObservers()
        initSocketObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        clearObservers()
    }

    private fun clearObservers() {
        viewModel.userInfo.removeObservers(viewLifecycleOwner)
        viewModel.oddsType.removeObservers(viewLifecycleOwner)
        viewModel.publicityRecommend.removeObservers(viewLifecycleOwner)
        viewModel.betInfoList.removeObservers(viewLifecycleOwner)

        receiver.serviceConnectStatus.removeObservers(viewLifecycleOwner)
        receiver.matchStatusChange.removeObservers(viewLifecycleOwner)
        receiver.matchClock.removeObservers(viewLifecycleOwner)
        receiver.oddsChange.removeObservers(viewLifecycleOwner)
        receiver.matchOddsLock.removeObservers(viewLifecycleOwner)
        receiver.leagueChange.removeObservers(viewLifecycleOwner)
        receiver.globalStop.removeObservers(viewLifecycleOwner)
        receiver.producerUp.removeObservers(viewLifecycleOwner)
    }

    private fun getConfigData() {
        viewModel.getConfigData()
    }

    private fun initOnClickListener() {
        binding.rvPublicity.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        avoidFastDoubleClick()
        with(binding) {
            when (v) {
                rvPublicity -> {
                    goLoginPage()
                }
            }
        }
    }

    private fun removeBetListFragment() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).removeBetListFragment()
        }
    }

    private fun clickNotice() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).fragmentClickNotice()
        }
    }

    private fun clickNews() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).fragmentClickNews()
        }
    }

    private fun goSwitchLanguagePage() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).goSwitchLanguagePage()
        }
    }

    private fun showLoginNotify() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).showLoginNotify()
        }
    }

    private fun initRecommendView() {
        with(binding.rvPublicity) {
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = mPublicityAdapter
        }
    }

    private fun initTitle() {
        with(mPublicityAdapter) {
            addTitle()
            addPublicityMenu(PublicityMenuData())
            addAnnouncement()
//            addSubTitle() //熱門推薦bar需常駐
            addPreload()
        }
    }

    private fun initBottomView() {
        mPublicityAdapter.addBottomView()
    }

    private fun initObservers() {
        viewModel.isLogin.observe(viewLifecycleOwner) { isLogin ->
            mPublicityAdapter.isLogin = isLogin
        }

        viewModel.infoCenterRepository.unreadNoticeList.observe(viewLifecycleOwner) {
            mPublicityAdapter.hasNotice = it.isNotEmpty()
        }

        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            val newDiscount = userInfo?.discount ?: 1.0F
            viewModel.publicityUpdateDiscount(mPublicityAdapter.discount, newDiscount)
            mPublicityAdapter.discount = newDiscount
        }

        viewModel.oddsType.observe(viewLifecycleOwner) {
            it?.let { oddsType ->
                mPublicityAdapter.oddsType = oddsType
            }
        }

        viewModel.publicityRecommend.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { recommendList ->
                hideLoading()
                isNewestDataFromApi = true
                mRecommendList = recommendList
                mPublicityAdapter.removeData(GamePublicityNewAdapter.PreloadItem())
                val adapterRecommendListData = mPublicityAdapter.getRecommendListData()
                recommendList.forEach { recommend ->
                    adapterRecommendListData.firstOrNull { adapterRecommend -> adapterRecommend.id == recommend.id }
                        ?.let { oldRecommend ->
                            recommend.matchInfo?.status = oldRecommend.matchInfo?.status
                            recommend.matchInfo?.statusName18n = oldRecommend.matchInfo?.statusName18n
                            recommend.matchInfo?.socketMatchStatus = oldRecommend.matchInfo?.socketMatchStatus
                            recommend.matchInfo?.leagueTime = oldRecommend.matchInfo?.leagueTime
                            recommend.runningTime = oldRecommend.runningTime
                        }
                }
                //新版宣傳頁
                if (recommendList.isEmpty()) return@observe //推薦賽事為empty不顯示
                mPublicityAdapter.addRecommendList(recommendList)
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(recommendList)
            }
        }

        viewModel.betInfoList.observe(viewLifecycleOwner) { event ->
            event?.peekContent()?.let { betList ->
                val targetList = getNewestRecommendData()
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
                        mPublicityAdapter.updateRecommendListData(targetList)
                    }
                }
            }
        }

        viewModel.favorMatchList.observe(viewLifecycleOwner) { favorMatchList ->
            val targetList = getNewestRecommendData()
            targetList.forEachIndexed { index, recommend ->
                var needUpdate = false
                val isFavorite = favorMatchList.contains(recommend.matchInfo?.id)
                if (recommend.matchInfo?.isFavorite != isFavorite) {
                    recommend.matchInfo?.isFavorite = isFavorite
                    needUpdate = true
                }
                if (needUpdate) {
                    mPublicityAdapter.updateRecommendData(index, recommend)
                }
            }
        }

        viewModel.gotConfig.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { isReload ->
                if (isReload) {
                    mPublicityAdapter.updateToolbarBannerImage()
                }
            }
        }

        viewModel.sportMenuFilterList.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                queryData()
            }
        }

        //新版宣傳頁
        viewModel.messageListResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { messageListResult ->
                val titleList: MutableList<String> = mutableListOf()
                messageListResult.rows?.forEach { data ->
                    if (data.type.toInt() == 1) titleList.add(data.title + " - " + data.message)
                }
                mPublicityAdapter.updateAnnouncementData(titleList)
            }
        }

        viewModel.publicityPromotionAnnouncementList.observe(viewLifecycleOwner) {
            //非信用盤才顯示優惠活動跑馬燈
            if (!isCreditSystem())
                if (it.isNotEmpty()) mPublicityAdapter.addPromotionAnnouncementList(it)
        }

        viewModel.publicityPromotionList.observe(viewLifecycleOwner) {
            //非信用盤才顯示優惠活動
            if (!isCreditSystem())
                if (it.isNotEmpty()) mPublicityAdapter.addPublicityPromotionList(it)
        }

        viewModel.publicityMenuData.observe(viewLifecycleOwner) {
            mPublicityAdapter.addPublicityMenu(it)
        }

        viewModel.enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisible)
                enterThirdGame(it)
        }

        viewModel.errorPromptMessage.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()
                ?.let { message -> showErrorPromptDialog(getString(R.string.prompt), message) {} }
        }

        mPublicityVersionUpdateViewModel.appVersionState.observe(viewLifecycleOwner) {
            viewModel.updateMenuVersionUpdatedStatus(it)
        }
    }

    // TODO subscribe leagueChange: 此處尚無需實作邏輯, 看之後有沒有相關需求
    private fun initSocketObservers() {
        receiver.serviceConnectStatus.observe(viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
//                    loading()
                    viewModel.getSportMenuFilter()
                    subscribeSportChannelHall()
                }
            }
        }

        receiver.matchStatusChange.observe(viewLifecycleOwner) { event ->
            event?.let { matchStatusChangeEvent ->
                val targetList = getNewestRecommendData()
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
                    updateRecommendListData(targetList)
                }
            }
        }

        receiver.matchClock.observe(viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val targetList = getNewestRecommendData()
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
                    updateRecommendListData(targetList)
                }
            }
        }

        receiver.oddsChange.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { oddsChangeEvent ->
                val targetList = getNewestRecommendData()
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

                        if (isNewestDataFromApi)
                            isNewestDataFromApi = false
                    }
                }
                mPublicityAdapter.removeData(GamePublicityAdapter.PreloadItem())
                if (needUpdate) {
                    updateRecommendListData(targetList)
                }
            }
        }

        receiver.matchOddsLock.observe(viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                val targetList = getNewestRecommendData()
                var needUpdate = false // 紀錄是否需要更新整個推薦賽事清單

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(recommend, matchOddsLockEvent)
                    ) {
                        needUpdate = true
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }

                if (needUpdate) {
                    updateRecommendListData(targetList)
                }
            }
        }

        receiver.leagueChange.observe(viewLifecycleOwner) {
            it?.let { leagueChangeEvent ->
                viewModel.publicityLeagueChange(leagueChangeEvent)
            }
        }

        receiver.globalStop.observe(viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                val targetList = getNewestRecommendData()
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
                    updateRecommendListData(targetList)
                }
            }
        }

        receiver.producerUp.observe(viewLifecycleOwner) {
            it?.let {
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(mPublicityAdapter.getRecommendListData())
            }
        }

        receiver.closePlayCate.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                mPublicityAdapter.getRecommendListData().forEach { recommend ->
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
                mPublicityAdapter.notifyDataSetChanged()
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

    /**
     * 賠率排序
     */
    private fun OddsChangeEvent.sortOddsMap() {
        this.odds.forEach { (_, value) ->
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
        viewModel.getPublicityPromotion()
        viewModel.getRecommend()
        viewModel.getMenuThirdGame()
    }

    private fun goLoginPage() {
        startActivity(Intent(context, LoginActivity::class.java))
    }

    private fun goRegisterPage() {
        startActivity(Intent(context, RegisterActivity::class.java))
    }

    private fun goDepositPage() {
        startActivity(Intent(context, RegisterActivity::class.java))
    }

    private fun goWithdrawPage() {
        startActivity(Intent(context, RegisterActivity::class.java))
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
        playCateMenuCode: String?
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
                    is GamePublicityActivity -> fragmentActivity.showFastBetFragment(fastBetDataBean)
                }
            }
        }
    }

    private fun showStatistics(matchId: String?) {
        StatisticsDialog.newInstance(matchId, StatisticsDialog.StatisticsClickListener { clickMenu() })
            .show(childFragmentManager, StatisticsDialog::class.java.simpleName)
    }

    private fun navOddsDetailFragment(
        gameTypeCode: String,
        matchType: MatchType?,
        matchId: String?,
        matchInfoList: List<MatchInfo>
    ) {
        val gameType = GameType.getGameType(gameTypeCode)
        val navMatchType = matchType ?: MatchType.DETAIL
        if (gameType != null && matchId != null) {
            findNavController().navigate(
                PublicityFragmentDirections.actionPublicityFragmentToOddsDetailLiveFragment(
                    navMatchType,
                    gameType,
                    matchId
                )
            )
        }
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

    private fun getNewestRecommendData(): List<Recommend> =
        if (isNewestDataFromApi) mRecommendList else mPublicityAdapter.getRecommendListData()


    private fun updateRecommendList(index: Int, recommend: Recommend) {
        with(binding) {
            if (rvPublicity.scrollState == RecyclerView.SCROLL_STATE_IDLE && !rvPublicity.isComputingLayout) {
                mPublicityAdapter.updateRecommendData(index, recommend)
            }
        }
    }

    /**
     * 更新推薦賽事整個清單
     */
    private fun updateRecommendListData(recommendList: List<Recommend>) {
        with(binding) {
            if (rvPublicity.scrollState == RecyclerView.SCROLL_STATE_IDLE && !rvPublicity.isComputingLayout) {
                mPublicityAdapter.updateRecommendListData(recommendList)
            }
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
                    RegisterActivity::class.java
                )
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
        startActivity(
            Intent(activity, GameActivity::class.java).putExtra(
                ARGS_PUBLICITY_SPORT_ENTRANCE,
                PublicitySportEntrance(matchType, gameType)
            )
        )
    }
}