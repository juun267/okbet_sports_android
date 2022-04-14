package org.cxct.sportlottery.ui.game.publicity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityGamePublicityBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.betList.FastBetFragment
import org.cxct.sportlottery.ui.game.betList.receipt.BetReceiptFragment
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity.Companion.FROM_ACTIVITY
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MetricsUtil
import org.cxct.sportlottery.util.PlayCateMenuFilterUtils
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.parceler.Parcels

class GamePublicityActivity : BaseBottomNavActivity<GamePublicityViewModel>(GamePublicityViewModel::class),
    View.OnClickListener {
    private lateinit var binding: ActivityGamePublicityBinding

    companion object {
        const val IS_FROM_PUBLICITY = "isFromPublicity"
        const val PUBLICITY_GAME_TYPE = "publicityGameType"
        const val PUBLICITY_MATCH_ID = "publicityMatchId"
        const val PUBLICITY_MATCH_TYPE = "publicityMatchType"
        const val PUBLICITY_MATCH_LIST = "publicityMatchList"

        fun reStart(context: Context) {
            val intent = Intent(context, GamePublicityActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private var isNewestDataFromApi = false
    private var mRecommendList: List<Recommend> = listOf()
    private val mPublicityAdapter =
        GamePublicityAdapter(
            GamePublicityAdapter.PublicityAdapterListener(
                onItemClickListener = {
                    goLoginPage()
                },
                onGoHomePageListener = {
                    goGamePage()
                },
                onClickBetListener = { gameType, matchType, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap, playCateMenuCode ->
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
                },
                onShowLoginNotify = {
                    showLoginNotify()
                },
                onClickStatisticsListener = { matchId ->
                    showStatistics(matchId)
                }, onClickPlayTypeListener = { gameType, matchType, matchId, matchInfoList ->
                    navDetailFragment(gameType, matchType, matchId, matchInfoList)
                })
        )

    private var betListFragment = BetListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamePublicityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initBaseFun()
        initObservers()
        initSocketObservers()
    }

    override fun onResume() {
        super.onResume()

        getSportMenuFilter()
    }

    private fun initViews() {
        initToolBar()
        initServiceButton()
        initOnClickListener()
        initRecommendView()
        initTitle()
        initBottomView()
    }

    private fun initBaseFun() {
        initMenu()
        initBottomNavigation()
    }

    override fun initToolBar() {
        with(binding) {
            publicityToolbar.ivLanguage.setImageResource(LanguageManager.getLanguageFlag(this@GamePublicityActivity))
            publicityToolbar.tvLanguage.text = LanguageManager.getLanguageStringResource(this@GamePublicityActivity)
        }
    }

    private fun initServiceButton() {
        binding.btnFloatingService.setView(this)
    }

    private fun initOnClickListener() {
        //region view bottom
        binding.tvRegister.setOnClickListener(this)
        binding.tvLogin.setOnClickListener(this)
        //endregion
        //region tool bar
        binding.publicityToolbar.ivLogo.setOnClickListener(this)
        binding.publicityToolbar.blockLanguage.setOnClickListener(this)
        setupNoticeButton(binding.publicityToolbar.ivNotice)
        binding.publicityToolbar.ivMenu.setOnClickListener(this)
        //endregion
        binding.rvPublicity.setOnClickListener(this)
    }

    private fun initRecommendView() {
        with(binding.rvPublicity) {
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = mPublicityAdapter
            itemAnimator = null
        }
    }

    private fun initTitle() {
        with(mPublicityAdapter) {
            addTitle()
            addSubTitle()
        }
    }

    private fun initBottomView() {
        mPublicityAdapter.addBottomView()
    }

    private fun initObservers() {
        viewModel.oddsType.observe(this, {
            it?.let { oddsType ->
                mPublicityAdapter.oddsType = oddsType
            }
        })

        viewModel.publicityRecommend.observe(this, { event ->
            event?.getContentIfNotHandled()?.let { result ->
                hideLoading()
                isNewestDataFromApi = true
                mRecommendList = result.recommendList
                mPublicityAdapter.addRecommend(result.recommendList)
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(result.recommendList)
            }
        })

        viewModel.showBetInfoSingle.observe(this) { event ->
            event?.getContentIfNotHandled()?.let {
                if (it) {
                    if (viewModel.getIsFastBetOpened()) {
//                        showFastBetFragment()
                    } else {
                        showBetListPage()
                    }
                }
            }
        }

        viewModel.betInfoList.observe(this) { event ->
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
    }

    // TODO subscribe leagueChange: 此處尚無需實作邏輯, 看之後有沒有相關需求
    private fun initSocketObservers() {
        receiver.serviceConnectStatus.observe(this) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    loading()
                    queryData()
                }
            }
        }

        receiver.matchStatusChange.observe(this, { event ->
            event?.let { matchStatusChangeEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    val matchList = listOf(recommend).toMutableList()
                    if (SocketUpdateUtil.updateMatchStatus(
                            recommend.gameType,
                            matchList as MutableList<org.cxct.sportlottery.network.common.MatchOdd>,
                            matchStatusChangeEvent,
                            this
                        )
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.matchClock.observe(this, {
            it?.let { matchClockEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    if (
                        SocketUpdateUtil.updateMatchClock(
                            recommend,
                            matchClockEvent
                        )
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.oddsChange.observe(this, { event ->
            event?.let { oddsChangeEvent ->
                SocketUpdateUtil.updateMatchOdds(oddsChangeEvent)
                val targetList = getNewestRecommendData()
                targetList.forEachIndexed { index, recommend ->
                    if (recommend.id == oddsChangeEvent.eventId) {
                        recommend.sortOddsMap()
                        recommend.updateOddsSort() //篩選玩法

                        //region 翻譯更新
                        oddsChangeEvent.playCateNameMap?.let { playCateNameMap ->
                            recommend.playCateNameMap?.putAll(playCateNameMap)
                        }
                        oddsChangeEvent.betPlayCateNameMap?.let { betPlayCateNameMap ->
                            recommend.betPlayCateNameMap?.putAll(betPlayCateNameMap)
                        }
                        //endregion

                        if (SocketUpdateUtil.updateMatchOdds(this, recommend, oddsChangeEvent)) {
                            recommend.sortOddsByMenu()
                            updateRecommendList(index, recommend)
                        }

                        if (isNewestDataFromApi)
                            isNewestDataFromApi = false
                    }
                }
            }
        })

        receiver.matchOddsLock.observe(this, {
            it?.let { matchOddsLockEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(recommend, matchOddsLockEvent)
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.globalStop.observe(this, {
            it?.let { globalStopEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(
                            recommend,
                            globalStopEvent
                        )
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.producerUp.observe(this, {
            it?.let {
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(mPublicityAdapter.getRecommendData())
            }
        })
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
            return
        }
    }


    private fun queryData() {
        viewModel.getRecommend()
    }

    private fun getSportMenuFilter() {
        viewModel.getSportMenuFilter()
    }

    private fun subscribeChannelHall(recommend: Recommend) {
        subscribeChannelHall(recommend.gameType, recommend.id)
    }

    private fun subscribeQueryData(recommendList: List<Recommend>) {
        recommendList.forEach { subscribeChannelHall(it) }
    }

    private fun getNewestRecommendData(): List<Recommend> =
        if (isNewestDataFromApi) mRecommendList else mPublicityAdapter.getRecommendData()


    private fun updateRecommendList(index: Int, recommend: Recommend) {
        with(binding) {
            if (rvPublicity.scrollState == RecyclerView.SCROLL_STATE_IDLE && !rvPublicity.isComputingLayout) {
                mPublicityAdapter.updateRecommendData(index, recommend)
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
                showFastBetFragment(fastBetDataBean)

                /*viewModel.updateMatchBetList(
                    matchType,
                    gameType,
                    playCateCode,
                    playCateName,
                    matchInfo,
                    odd,
                    ChannelType.HALL,
                    betPlayCateNameMap,
                    playCateMenuCode
                )*/
            }
        }
    }

    //region 開啟(快捷)投注單
    //跟進GameActivity開啟投注單方式
    private fun showFastBetFragment(fastBetDataBean: FastBetDataBean) {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit,
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit
            )

        val fastBetFragment = FastBetFragment()
        val bundle = Bundle()
        bundle.putParcelable("data", Parcels.wrap(fastBetDataBean))
        fastBetFragment.arguments = bundle

        transaction
            .add(binding.flBetList.id, fastBetFragment)
            .addToBackStack(FastBetFragment::class.java.simpleName)
            .commit()
    }

    override fun showBetListPage() {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit,
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit
            )

        betListFragment =
            BetListFragment.newInstance(object : BetListFragment.BetResultListener {
                override fun onBetResult(betResultData: Receipt?, betParlayList: List<ParlayOdd>) {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.push_right_to_left_enter,
                            R.anim.pop_bottom_to_top_exit,
                            R.anim.push_right_to_left_enter,
                            R.anim.pop_bottom_to_top_exit
                        )
                        .add(
                            binding.flBetList.id,
                            BetReceiptFragment.newInstance(betResultData, betParlayList)
                        )
                        .addToBackStack(BetReceiptFragment::class.java.simpleName)
                        .commit()
                }

            })

        transaction
            .add(binding.flBetList.id, betListFragment, BetListFragment::class.java.simpleName)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()

    }

    private fun removeBetListFragment() {
        supportFragmentManager.beginTransaction().remove(betListFragment).commit()
    }
    //endregion

    private fun showStatistics(matchId: String?) {
        StatisticsDialog.newInstance(matchId)
            .show(supportFragmentManager, StatisticsDialog::class.java.simpleName)
    }

    private fun navDetailFragment(
        gameType: String,
        matchType: MatchType?,
        matchId: String?,
        matchInfoList: List<MatchInfo>
    ) {
        unSubscribeChannelHallAll()

        startActivity(Intent(this, GameActivity::class.java).apply {
            putExtra(IS_FROM_PUBLICITY, true)
            putExtra(PUBLICITY_GAME_TYPE, gameType)
            putExtra(PUBLICITY_MATCH_TYPE, matchType)
            putExtra(PUBLICITY_MATCH_ID, matchId)
            val matchInfoArrayList = arrayListOf<MatchInfo>()
            matchInfoArrayList.addAll(matchInfoList)
            putParcelableArrayListExtra(PUBLICITY_MATCH_LIST, matchInfoArrayList)
        })
    }

    private fun Recommend.sortOddsMap() {
        this.oddsMap?.forEach { (_, value) ->
            if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                value?.sortBy {
                    it?.marketSort
                }
            }
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

    /**
     * 篩選玩法
     * 更新翻譯、排序
     * */
    private fun Recommend.updateOddsSort() {
        val nowGameType = gameType
        val playCateMenuCode = menuList.firstOrNull()?.code
        val oddsSortFilter = PlayCateMenuFilterUtils.filterOddsSort(nowGameType, playCateMenuCode)
        val playCateNameMapFilter = PlayCateMenuFilterUtils.filterPlayCateNameMap(nowGameType, playCateMenuCode)

        oddsSort = oddsSortFilter
        playCateNameMap = playCateNameMapFilter
    }

    override fun onClick(v: View?) {
        avoidFastDoubleClick()
        with(binding) {
            when (v) {
                tvRegister -> {
                    goRegisterPage()
                }
                tvLogin -> {
                    goLoginPage()
                }
                publicityToolbar.ivLogo -> {
                    removeBetListFragment()
                }
                publicityToolbar.blockLanguage -> {
                    goSwitchLanguagePage()
                }
                publicityToolbar.ivMenu -> {
                    if (drawerLayout.isDrawerOpen(viewNavRight.navRight)) drawerLayout.closeDrawers()
                    else {
                        drawerLayout.openDrawer(viewNavRight.navRight)
                        viewModel.getMoney()
                    }
                }
                rvPublicity -> {
                    goLoginPage()
                }
            }
        }
    }

    private fun goRegisterPage() {
        startActivity(Intent(this@GamePublicityActivity, RegisterActivity::class.java))
    }

    private fun goLoginPage() {
        startActivity(Intent(this@GamePublicityActivity, LoginActivity::class.java))
    }

    private fun goGamePage() {
        GameActivity.reStart(this)
        finish()
    }

    private fun goSwitchLanguagePage() {
        startActivity(Intent(this@GamePublicityActivity, SwitchLanguageActivity::class.java).apply {
            putExtra(FROM_ACTIVITY, this@GamePublicityActivity::class.java)
        })
    }

    override fun initMenu() {
        //關閉側邊欄滑動行為
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        //選單選擇結束要收起選單
        val menuFrag =
            supportFragmentManager.findFragmentById(binding.viewNavRight.fragmentMenu.id) as MenuFragment
        menuFrag.setDownMenuListener { binding.drawerLayout.closeDrawers() }
        binding.viewNavRight.navRight.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬
    }

    override fun initBottomNavigation() {
        viewModel.getTransNum()
        binding.gameBottomNavigation.sportBottomNavigation.setNavigationItemClickListener {
            when (it) {
                R.id.navigation_sport -> {
                    viewModel.navGame()
                    true
                }
                R.id.navigation_game -> {
                    viewModel.navMyFavorite()
                    false
                }
                R.id.item_bet_list -> {
                    viewModel.navShoppingCart()
                    false
                }
                R.id.navigation_account_history -> {
                    viewModel.navAccountHistory()
                    false
                }
                R.id.navigation_transaction_status -> {
                    viewModel.navTranStatus()
                    false
                }
                else -> false
            }
        }
    }

    override fun updateUiWithLogin(isLogin: Boolean) {
        with(binding) {
            if (isLogin) {
                gameBottomNavigation.sportBottomNavigation.visibility = View.VISIBLE
                publicityToolbar.ivNotice.visibility = View.VISIBLE
                publicityToolbar.ivMenu.visibility = View.VISIBLE

                publicityToolbar.blockLanguage.visibility = View.GONE

                viewBottom.layoutParams.height = 55.dp
            } else {
                gameBottomNavigation.sportBottomNavigation.visibility = View.GONE
                publicityToolbar.ivNotice.visibility = View.GONE
                publicityToolbar.ivMenu.visibility = View.GONE

                publicityToolbar.blockLanguage.visibility = View.VISIBLE

                viewBottom.layoutParams.height = 60.dp
            }
        }
    }

    override fun updateOddsType(oddsType: OddsType) {
        //盤口顯示方式已調整至右側邊欄
    }

    override fun updateBetListCount(num: Int) {
        binding.gameBottomNavigation.sportBottomNavigation.setBetCount(num)
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            setAnchorView(binding.viewBottom.id)
            show()
        }
    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
        snackBarMyFavoriteNotify?.apply {
            setAnchorView(binding.viewBottom.id)
            show()
        }
    }

    override fun navOneSportPage(thirdGameCategory: ThirdGameCategory?) {
        if (thirdGameCategory != null) {
            val intent = Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
            startActivity(intent)

            return
        }

        goGamePage()
    }

}