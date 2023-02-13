

            //0401需求先隱藏特優賠率
//            val tabEps = tabLayout.getTabAt(7)?.customView
//            tabEps?.tv_title?.setText(R.string.home_tab_eps)
//            tabEps?.tv_number?.text = countEps.toString()

            //英文 越南文稍微加寬padding 不然會太擠
            if (LanguageManager.getSelectLanguage(this) != LanguageManager.Language.ZH) {
                for (i in 0 until tabLayout.tabCount) {
                    tabLayout.getTabAt(i)?.customView.apply {
                        this?.setPadding(8.dp, 0, 16.dp, 0)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val matchTypeTabPositionMap = mapOf<MatchType, Int>(
        //20220728 隱藏主頁
        /*MatchType.MAIN to 0,
        MatchType.IN_PLAY to 1,
        MatchType.AT_START to 2,
        MatchType.TODAY to 3,
        MatchType.EARLY to 4,
        MatchType.OUTRIGHT to 5,
        MatchType.PARLAY to 6,
        MatchType.EPS to 7*/
        MatchType.IN_PLAY to 0,
        MatchType.AT_START to 1,
        MatchType.TODAY to 2,
        MatchType.EARLY to 3,
        MatchType.PARLAY to 4,
        MatchType.OUTRIGHT to 5,
        MatchType.EPS to 6,
        MatchType.MAIN to 99
    )

    /**
     * 根據MatchTypeTabPositionMap獲取MatchType的tab position
     *
     * @see MatchTypeTabPositionMap
     */
    private fun getMatchTypeTabPosition(matchType: MatchType?): Int? = when (matchType) {
        null -> {
            Timber.e("Unable to get $matchType tab position")
            null
        }
        else -> {
            when (val tabPosition = matchTypeTabPositionMap[matchType]) {
                null -> {
                    Timber.e("There is not tab position of $matchType")
                    null
                }
                else -> {
                    tabPosition
                }
            }
        }
    }

    private fun selectTab(position: Int?) {
        if (position == null) return

        when (position) {
            getMatchTypeTabPosition(MatchType.MAIN) -> {
                viewModel.switchMatchType(MatchType.MAIN)
            }
            getMatchTypeTabPosition(MatchType.IN_PLAY) -> {
                viewModel.switchMatchType(MatchType.IN_PLAY)
            }
            getMatchTypeTabPosition(MatchType.AT_START) -> {
                viewModel.switchMatchType(MatchType.AT_START)
            }
            getMatchTypeTabPosition(MatchType.TODAY) -> {
                viewModel.switchMatchType(MatchType.TODAY)
            }
            getMatchTypeTabPosition(MatchType.EARLY) -> {
                viewModel.switchMatchType(MatchType.EARLY)
            }
            getMatchTypeTabPosition(MatchType.OUTRIGHT) -> {
                /**
                 * 若mOutrightLeagueId有值的話, 此行為為主頁點擊聯賽跳轉至冠軍頁, 跳轉行為於HomeFragment處理
                 *
                 * @see org.cxct.sportlottery.ui.game.home.HomeFragment.navGameOutright
                 */
                if (mOutrightLeagueId.isNullOrEmpty()) {
                    viewModel.switchMatchType(MatchType.OUTRIGHT)
                }
            }
            getMatchTypeTabPosition(MatchType.PARLAY) -> {
                viewModel.switchMatchType(MatchType.PARLAY)
            }
            getMatchTypeTabPosition(MatchType.EPS) -> {
                viewModel.switchMatchType(MatchType.EPS)
            }
        }
    }

    private fun navDetailLiveFragment(
        matchID: String,
        gameType: GameType,
        matchType: MatchType? = null,
        matchList: ArrayList<MatchInfo>? = null
    ) {
        val detailMatchType = matchType ?: MatchType.DETAIL
        when (mNavController.currentDestination?.id) {
            R.id.homeFragment -> {
                val action = HomeFragmentDirections.actionHomeFragmentToOddsDetailLiveFragment(
                    detailMatchType,
                    gameType,
                    matchID
                )
                mNavController.navigate(action)
            }
            R.id.gameV3Fragment -> {
                val action = GameV3FragmentDirections.actionGameV3FragmentToOddsDetailLiveFragment(
                    detailMatchType,
                    gameType,
                    matchID
                )
                mNavController.navigate(action)
            }
            R.id.gameLeagueFragment -> {
                val action = HomeFragmentDirections.actionHomeFragmentToOddsDetailLiveFragment(
                    detailMatchType,
                    gameType,
                    matchID
                )
                mNavController.navigate(action)
            }
        }
    }

    private fun navGameFragment(matchType: MatchType) {
        //TODO 確認有什麼情況下會是MatchType.MAIN的，若沒有的話可以濾掉
        when (mNavController.currentDestination?.id) {
            R.id.homeFragment -> {
                val action = HomeFragmentDirections.actionHomeFragmentToGameFragment(matchType)
                mNavController.navigate(action)
            }
            R.id.gameV3Fragment -> {
                val action = GameV3FragmentDirections.actionGameFragmentToGameFragment(matchType)
                val navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                mNavController.navigate(action, navOptions)
            }
            R.id.leagueFilterFragment -> {
                val action =
                    LeagueFilterFragmentDirections.actionLeagueFilterFragmentToGameV3Fragment(
                        matchType
                    )
                mNavController.navigate(action)
            }
            R.id.gameLeagueFragment -> {
                val action =
                    GameLeagueFragmentDirections.actionGameLeagueFragmentToGameV3Fragment(matchType)
                mNavController.navigate(action)
            }
            R.id.gameOutrightMoreFragment -> {
                val action =
                    GameOutrightMoreFragmentDirections.actionGameOutrightMoreFragmentToGameV3Fragment(
                        matchType
                    )
                mNavController.navigate(action)
            }
            R.id.oddsDetailLiveFragment -> {
                val action =
                    OddsDetailLiveFragmentDirections.actionOddsDetailLiveFragmentToGameV3Fragment(
                        matchType
                    )
                mNavController.navigate(action)
            }
        }
    }

    private fun navHomeFragment() {
        //TODO 此處有個隱藏的Bug, 在進到這個fun前, currentDestination都已經因為觀察到curMatchType的變化進入navGameFragment()而移動至gameV3Fragment
        when (mNavController.currentDestination?.id) {

            R.id.homeFragment -> {
            }
            R.id.gameV3Fragment -> {
                val action = GameV3FragmentDirections.actionGameV3FragmentToHomeFragment()
                val navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                mNavController.navigate(action, navOptions)
            }
            R.id.leagueFilterFragment -> {
                val action =
                    LeagueFilterFragmentDirections.actionLeagueFilterFragmentToHomeFragment()
                mNavController.navigate(action)
            }

            R.id.gameLeagueFragment -> {
                val action =
                    GameLeagueFragmentDirections.actionGameLeagueFragmentToHomeFragment()
                mNavController.navigate(action)
            }

            R.id.gameOutrightMoreFragment -> {
                val action =
                    GameOutrightMoreFragmentDirections.actionGameOutrightMoreFragmentToHomeFragment()
                mNavController.navigate(action)
            }
            R.id.oddsDetailLiveFragment -> {
                val action =
                    OddsDetailLiveFragmentDirections.actionOddsDetailLiveFragmentToHomeFragment()
                mNavController.navigate(action)
            }
        }
    }

    override fun onBackPressed() {
        //返回鍵優先關閉投注單fragment
        if (supportFragmentManager.backStackEntryCount != 0) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
            return
        }
        //關閉drawer
        if (drawer_layout.isDrawerOpen(nav_right)) {
            drawer_layout.closeDrawers()
            return
        }
        if (sub_drawer_layout.isDrawerOpen(nav_left)) {
            sub_drawer_layout.closeDrawers()
            return
        }
        when (mNavController.currentDestination?.id) {
            R.id.gameV3Fragment -> {
                //特殊賽事返回時，不回到主頁
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.game_container) as NavHostFragment
                val gameV3Fragment =
                    navHostFragment.childFragmentManager.fragments.firstOrNull() as GameV3Fragment
                when (gameV3Fragment.arguments?.getSerializable("matchType") as MatchType) {
                    MatchType.OTHER -> {
                        goTab(tabLayout.selectedTabPosition)
                    }
                    MatchType.IN_PLAY -> {
                        //當前為滾球時，點back返回宣傳頁
                        GamePublicityActivity.reStart(this)
                    }
                    else -> {
                        matchTypeTabPositionMap[MatchType.IN_PLAY]?.let {
                            goTab(it)
                        }
                    }
                }
            }
            R.id.homeFragment -> {
                //首頁時，點back返回宣傳頁
                GamePublicityActivity.reStart(this)
            }
            else -> mNavController.navigateUp()
        }
    }

    //用戶登入公告訊息彈窗
    private var mNewsDialog: NewsDialog? = null
    private fun setNewsDialog(messageListResult: MessageListResult) {

        //未登入、遊客登入都要顯示彈窗
        //顯示規則：帳號登入前= 公告含登入前、帳號登入後= 公告含登入前+登入後
        var list = listOf<Row>()
        list = if (viewModel.isLogin.value == true)
            messageListResult.rows?.filter { it.type.toInt() != 1 } ?: listOf()
        else
            messageListResult.rows?.filter { it.type.toInt() == 3 } ?: listOf()

        if (!list.isNullOrEmpty()) {
            if (!MultiLanguagesApplication.getInstance()?.isNewsShow()!!) {
                mNewsDialog?.dismiss()
                mNewsDialog = NewsDialog(list)
                mNewsDialog?.show(supportFragmentManager, null)
                MultiLanguagesApplication.getInstance()?.setIsNewsShow(true)
            }
        }
    }

    private fun initObserve() {
        viewModel.userMoney.observe(this) {
            it?.let { money ->
                tv_balance.text = TextUtil.formatMoney(money)
            }
        }
        viewModel.settlementNotificationMsg.observe(this) {
            val message = it.getContentIfNotHandled()
            message?.let { messageNotnull -> view_notification.addNotification(messageNotnull) }
        }

        viewModel.isLogin.observe(this) {
            getAnnouncement()
        }

        //使用者沒有電話號碼
        viewModel.showPhoneNumberMessageDialog.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (!b) phoneNumCheckDialog(this, supportFragmentManager)
            }
        }

        viewModel.showBetUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true)
                snackBarBetUpperLimitNotify.apply {
                    setAnchorView(R.id.game_bottom_navigation)
                    show()
                }
        }

        viewModel.messageListResult.observe(this) {
            it.getContentIfNotHandled()?.let { result ->
                updateUiWithResult(result)
            }
        }

        viewModel.nowTransNum.observe(this) {
            navigation_transaction_status.trans_number.text = it.toString()
        }

        viewModel.specialEntrance.observe(this) {
            hideLoading()
            if (it?.couponCode.isNullOrEmpty()) {
                when (it?.entranceMatchType) {
                    MatchType.MAIN -> {
                        //do nothing
                    }
                    MatchType.OTHER -> {
                        goTab(3)
                    }
                    MatchType.DETAIL -> {
                        it.matchID?.let { matchId ->
                            navDetailLiveFragment(matchId, it.gameType ?: GameType.OTHER, it.gameMatchType)
                        }
                    }
                    else -> {
                        getMatchTypeTabPosition(it?.entranceMatchType)?.let { matchTypePosition ->
                            goTab(matchTypePosition)
                        }
                    }
                }
            } else if (it?.entranceMatchType == MatchType.DETAIL) {

            } else {
                navGameFragment(it!!.entranceMatchType)
            }
        }

        //distinctUntilChanged() -> 相同的matchType僅會執行一次，有變化才會observe
        viewModel.curMatchType.distinctUntilChanged().observe(this) {
            it?.let {
                val tabSelectedPosition = tabLayout.selectedTabPosition
                when (it) {
                    MatchType.MAIN -> {
                        if (tabSelectedPosition == getMatchTypeTabPosition(MatchType.MAIN))
                            navHomeFragment()
                    }
                    else -> {
                        //僅有要切換的MatchType與當前選中的Tab相同時才繼續進行後續的切頁行為, 避免快速切頁導致切頁邏輯進入無窮迴圈
                        when {
                            it == MatchType.IN_PLAY && tabSelectedPosition == getMatchTypeTabPosition(MatchType.IN_PLAY) ||
                                    it == MatchType.AT_START && tabSelectedPosition == getMatchTypeTabPosition(MatchType.AT_START) ||
                                    it == MatchType.TODAY && tabSelectedPosition == getMatchTypeTabPosition(MatchType.TODAY) ||
                                    it == MatchType.EARLY && tabSelectedPosition == getMatchTypeTabPosition(MatchType.EARLY) ||
                                    it == MatchType.OUTRIGHT && tabSelectedPosition == getMatchTypeTabPosition(MatchType.OUTRIGHT) ||
                                    it == MatchType.PARLAY && tabSelectedPosition == getMatchTypeTabPosition(MatchType.PARLAY)
                            -> {
                                navGameFragment(it)
                            }
                            else -> {
                                //do nothing
                            }
                        }

                    }
                }
            }
        }

        viewModel.sportMenuResult.observe(this) {
            hideLoading()
            updateUiWithResult(it)
        }

        viewModel.userInfo.observe(this) {
            updateAvatar(it?.iconUrl)
        }

        viewModel.errorPromptMessage.observe(this) {
            it.getContentIfNotHandled()
                ?.let { message -> showErrorPromptDialog(getString(R.string.prompt), message) {} }

        }

        viewModel.leagueSelectedList.observe(this) {
            game_submit.apply {
                visibility = if (it.isEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                text = getString(R.string.button_league_submit, it.size)
            }
        }

        viewModel.showBetInfoSingle.observe(this) {
            it?.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }

        viewModel.navPublicityPage.observe(this) {
            GamePublicityActivity.reStart(this)
        }
    }

    /**
     * 前往指定的賽事種類
     * @since 若已經在該賽事種類, 點擊Tab不會觸發OnTabSelectedListener
     */
    private fun goTab(tabPosition: Int) {
        if (tabLayout.selectedTabPosition != tabPosition) {
            //賽事類別Tab不在滾球時, 點擊滾球Tab
            tabLayout.getTabAt(tabPosition)?.select()
        } else {
            selectTab(tabPosition)
        }
    }

    fun setupBetData(fastBetDataBean: FastBetDataBean) {
        viewModel.updateMatchBetListData(fastBetDataBean)
    }

    fun showSwitchLanguageFragment() {
        startActivity(Intent(this@GameActivity, SwitchLanguageActivity::class.java))
    }

    fun dismissSwitchLanguageFragment() {
        if (isSwitchLanguageFragmentVisible()) {
            supportFragmentManager.popBackStack()
        }
    }

    private fun isSwitchLanguageFragmentVisible(): Boolean {
        val fragments: List<Fragment> = supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is SwitchLanguageFragment) {
                if (fragment.isVisible) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 初始化客服按鈕
     * 另外透過DestinationChangedListener控制客服按鈕出現或隱藏
     * @see navDestListener
     * @see updateServiceButtonVisibility
     */
    private fun initServiceButton() {
        //2022/4/21需求：客服只在首頁和宣傳頁、維護頁出現
        btn_floating_service.setView(this)
    }

    override fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {
            btn_login.visibility = View.GONE
            iv_menu.visibility = View.VISIBLE
            iv_notice.visibility = View.VISIBLE
            btn_register.visibility = View.GONE
            toolbar_divider.visibility = View.GONE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
        } else {
            btn_login.visibility = View.VISIBLE
            btn_register.visibility = View.VISIBLE
            toolbar_divider.visibility = View.VISIBLE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
            iv_menu.visibility = View.GONE
            iv_notice.visibility = View.GONE
            btn_register.setVisibilityByCreditSystem()
            toolbar_divider.setVisibilityByCreditSystem()
        }
    }

    override fun updateOddsType(oddsType: OddsType) {
        tv_odds_type.text = getString(oddsType.res)
    }

    override fun navOneSportPage(thirdGameCategory: ThirdGameCategory?) {
        if (thirdGameCategory != null) {
            val intent = Intent(this, MainActivity::class.java)
                .putExtra(ARGS_THIRD_GAME_CATE, thirdGameCategory)
            startActivity(intent)

            return
        }

        GamePublicityActivity.reStart(this)
    }

    private fun updateUiWithResult(messageListResult: MessageListResult?) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult?.let {
            it.rows?.forEach { data ->
                if (data.type.toInt() == 1) titleList.add(data.title + " - " + data.message)
            }

            mMarqueeAdapter.setData(titleList)

            if (messageListResult.success && titleList.size > 0) {
                rv_marquee.startAuto(false) //啟動跑馬燈
            } else {
                rv_marquee.stopAuto(true) //停止跑馬燈
            }
        }
    }

    private fun updateUiWithResult(sportMenuResult: SportMenuResult?) {
        if (sportMenuResult?.success == true) {
            refreshTabLayout(sportMenuResult)
        }
    }

    private fun updateAvatar(iconUrl: String?) {
        Glide.with(this).load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(
                iv_head
            ) //載入頭像
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
    }

    private fun updateSelectTabState(matchType: MatchType?) {
        matchTypeTabPositionMap[matchType]?.let {
            updateSelectTabState(it)
        }
    }

    private fun updateSelectTabState(position: Int) {
        val tab = tabLayout.getTabAt(position)?.customView

        tab?.let {
            clearSelectTabState()
            tabLayout.getTabAt(position)?.select()
            it.tv_title?.isSelected = true
            it.tv_number?.isSelected = true
        }
    }

    private fun clearSelectTabState() {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)?.customView

            tab?.tv_title?.isSelected = false
            tab?.tv_number?.isSelected = false
        }
    }

    private fun removeBetListFragment() {
        supportFragmentManager.beginTransaction().remove(betListFragment).commit()
    }

    private fun closeLeftMenu() {
        val leftMenuFragment = supportFragmentManager.findFragmentById(R.id.fl_left_menu) as LeftMenuFragment
        leftMenuFragment.clearLeftMenu()
        if (sub_drawer_layout.isDrawerOpen(nav_left)) sub_drawer_layout.closeDrawers()
    }

    override fun onCloseMenu() {
        super.onCloseMenu()

        closeLeftMenu()
    }

    override fun onDestroy() {
        expandCheckList.clear()
        HomePageStatusManager.clear()
        mNavController.removeOnDestinationChangedListener(navDestListener)
        super.onDestroy()
    }

    private fun setupDataSourceChange() {
        setDataSourceChangeEvent {
            viewModel.fetchDataFromDataSourceChange(
                matchTypeTabPositionMap.filterValues { it == tabLayout.selectedTabPosition }.entries.first().key
            )
        }
    }

    /**
     * 檢查是否有從宣傳頁入口跳轉的事件
     *
     * @see org.cxct.sportlottery.ui.game.publicity.PublicityNewFragment.jumpToTheSport
     */
    private fun checkPublicityEntranceEvent() {
        val publicitySportEntrance =
            intent.getSerializableExtra(ARGS_PUBLICITY_SPORT_ENTRANCE) as? PublicitySportEntrance
        publicitySportEntrance?.let {
            viewModel.navSpecialEntrance(it.matchType, it.gameType)
        }
    }

}