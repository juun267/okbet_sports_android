package org.cxct.sportlottery.ui.maintab

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_home_live.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.greenrobot.eventbus.EventBus

class HomeLiveFragment :
    BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    companion object {
        fun newInstance(): HomeLiveFragment {
            val args = Bundle()
            val fragment = HomeLiveFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val homeTabAdapter by lazy {
        HomeTabAdapter(HomeTabAdapter.getItems(), 1).apply {
            setOnItemClickListener { adapter, view, position ->
                (parentFragment as HomeFragment).onTabClickByPosition(position)
            }
        }
    }
    private val homeLiveAdapter by lazy {
        HomeLiveAdapter(
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
                },
                onClickLiveIconListener = {
                        gameType, matchType, matchId, matchInfoList ->
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
        return inflater.inflate(R.layout.fragment_home_live, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getConfigData()
        initView()
        initObservable()
        initSocketObservers()
        viewModel.getRecommend()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getRecommend()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun initView() {
        initToolBar()
        initTabView()
        initListView()

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
        if (viewModel == null) {
            return
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                homeLiveAdapter.oddsType = oddsType
            }
        }
        viewModel.publicityRecommend.observe(viewLifecycleOwner) { event ->
            event?.peekContent()?.let { recommendList ->
                hideLoading()
                if (recommendList.isEmpty()) return@observe //推薦賽事為empty不顯示
                recommendList.forEach { recommend ->
                    // 將儲存的賠率表指定的賽事列表裡面
                    val leagueOddFromMap = leagueOddMap[recommend.leagueId]
                    leagueOddFromMap?.let {
                        recommend.oddsMap = leagueOddFromMap.oddsMap
                    }
                }
                homeLiveAdapter.data = recommendList
                //先解除全部賽事訂
                unSubscribeChannelHallAll()
                subscribeQueryData(recommendList)
            }
        }

        viewModel.betInfoList.observe(viewLifecycleOwner) { event ->
            event.peekContent().let { betInfoList ->
                homeLiveAdapter.betInfoList = betInfoList
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
                val targetList = homeLiveAdapter.data
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
                    homeLiveAdapter.data = targetList
                }
            }
        }

        receiver.matchClock.observe(viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val targetList = homeLiveAdapter.data
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
                    homeLiveAdapter.data = targetList
                }
            }
        }
        receiver.matchOddsChange.observe(viewLifecycleOwner) {

        }
        receiver.oddsChangeListener =
            ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
                val targetList = homeLiveAdapter.data
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
                            homeLiveAdapter.notifyItemChanged(index, recommend)
                        }
                    }
                }
            }

        receiver.matchOddsLock.observe(viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                val targetList = homeLiveAdapter.data
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
                val targetList = homeLiveAdapter.data
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
                subscribeQueryData(homeLiveAdapter.data)
            }
        }

        receiver.closePlayCate.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                homeLiveAdapter.data.forEach { recommend ->
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
                homeLiveAdapter.notifyDataSetChanged()
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



    private fun initTabView() {
        with(rv_tab_home) {
            if (layoutManager == null) {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
            if (adapter == null) {
                adapter = homeTabAdapter
            }
        }
    }

    private fun initListView() {
        with(rv_live) {
            if (layoutManager == null) {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            }
            if (adapter == null) {
                adapter = homeLiveAdapter
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

    private fun setupLogin() {
        viewModel.isLogin.value?.let {
            btn_register.isVisible = !it
            btn_login.isVisible = !it
//            lin_search.visibility = if (it) View.VISIBLE else View.GONE
            ll_user_money.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }
}
