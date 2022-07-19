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
import org.cxct.sportlottery.databinding.FragmentPublicityBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.CustomSecurityDialog
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.addScrollListenerForBottomNavBar

/**
 * @app_destination 宣傳頁
 */
class PublicityNewFragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class), View.OnClickListener {
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
                    clickMenu()
                },
                onItemClickListener = {
                    goLoginPage()
                },
                onGoHomePageListener = {
                    goGamePage()
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
                    navOddsDetailFragment(gameType, matchType, matchId, matchInfoList)
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
            addScrollListenerForBottomNavBar(
                onScrollDown = {
                    MultiLanguagesApplication.mInstance.setIsScrollDown(it)
                }
            )
        }
    }

    private fun initTitle() {
        with(mPublicityAdapter) {
            addTitle()
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
                        mPublicityAdapter.updateRecommendData(index, recommend)
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
            mPublicityAdapter.addPromotionAnnouncementList(it)
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
        viewModel.getAnnouncement()
        viewModel.getPromotionAnnouncement()
        viewModel.getRecommend()
        viewModel.getThirdGame()
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
}