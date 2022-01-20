package org.cxct.sportlottery.ui.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.bottom_navigation_item.view.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import kotlinx.android.synthetic.main.motion_view_service_floating.*
import kotlinx.android.synthetic.main.motion_view_service_floating.view.*
import kotlinx.android.synthetic.main.sport_bottom_navigation.*
import kotlinx.android.synthetic.main.sport_bottom_navigation.view.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import kotlinx.android.synthetic.main.view_game_tab_match_type_v4.*
import kotlinx.android.synthetic.main.view_message.*
import kotlinx.android.synthetic.main.view_nav_left.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.betList.FastBetFragment
import org.cxct.sportlottery.ui.game.betList.receipt.BetReceiptFragment
import org.cxct.sportlottery.ui.game.filter.LeagueFilterFragmentDirections
import org.cxct.sportlottery.ui.game.hall.GameV3FragmentDirections
import org.cxct.sportlottery.ui.game.home.HomeFragmentDirections
import org.cxct.sportlottery.ui.game.league.GameLeagueFragmentDirections
import org.cxct.sportlottery.ui.game.menu.LeftMenuFragment
import org.cxct.sportlottery.ui.game.outright.GameOutrightFragmentDirections
import org.cxct.sportlottery.ui.game.outright.GameOutrightMoreFragmentDirections
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.MainActivity.Companion.ARGS_THIRD_GAME_CATE
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.main.news.NewsDialog
import org.cxct.sportlottery.ui.menu.ChangeLanguageDialog
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeDialog
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.odds.OddsDetailFragmentDirections
import org.cxct.sportlottery.ui.odds.OddsDetailLiveFragmentDirections
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MetricsUtil


class GameActivity : BaseBottomNavActivity<GameViewModel>(GameViewModel::class) {

    companion object {
        //切換語系，activity 要重啟才會生效
        fun reStart(context: Context) {
            val intent = Intent(context, GameActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private var betListFragment = BetListFragment()

    private val mMarqueeAdapter by lazy { MarqueeAdapter() }
    private val mNavController by lazy { findNavController(R.id.game_container) }
    private val navDestListener by lazy {
        NavController.OnDestinationChangedListener { _, destination, arguments ->
            when (destination.id) {
                R.id.homeFragment -> {
                    updateSelectTabState(0)
                }

                R.id.gameV3Fragment -> {
                    updateSelectTabState(arguments?.get("matchType") as MatchType)
                }

                R.id.gameLeagueFragment -> {
                    updateSelectTabState(arguments?.get("matchType") as MatchType)
                }

                R.id.gameOutrightFragment -> {
                    updateSelectTabState(MatchType.OUTRIGHT)
                }

                R.id.oddsDetailFragment -> {
                    updateSelectTabState(arguments?.get("matchType") as MatchType)
                }

                R.id.oddsDetailLiveFragment -> {
                    updateSelectTabState(MatchType.IN_PLAY)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setupNoticeButton(btn_notice)
        initToolBar()
        initMenu()
        initSubmitBtn()
        initBottomNavigation()
        initRvMarquee()
        initTabLayout()
        initObserve()
        initServiceButton()
        setFontTheme()

        queryData()
    }

    private fun setFontTheme() {
        when (LanguageManager.getSelectLanguage(this)) {
            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> {
                setTheme(R.style.ChineseTheme)
            }
            LanguageManager.Language.VI -> {
                setTheme(R.style.VietnamTheme)
            }
            else -> {
                setTheme(R.style.EnglishTheme)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()

        mNavController.addOnDestinationChangedListener(navDestListener)

    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()

        mNavController.removeOnDestinationChangedListener(navDestListener)
    }

    override fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)
        iv_logo.setOnClickListener {
            viewModel.navMainPage(ThirdGameCategory.MAIN)
            removeBetListFragment()
        }

        iv_language.setImageResource(LanguageManager.getLanguageFlag(this))

        //頭像 當 側邊欄 開/關
        iv_head.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
                viewModel.getMoney()
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this@GameActivity, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this@GameActivity, RegisterActivity::class.java))
        }

        tv_odds_type.setOnClickListener {
            ChangeOddsTypeDialog().show(supportFragmentManager, null)
        }

        iv_language.setOnClickListener {
            ChangeLanguageDialog().show(supportFragmentManager, null)
        }
    }

    override fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            //選單選擇結束要收起選單
            val menuFrag =
                supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener { drawer_layout.closeDrawers() }
            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬

            //左邊側邊攔v4
            btn_menu_left.setOnClickListener {
                LeftMenuFragment().show(
                    supportFragmentManager,
                    LeftMenuFragment::class.java.simpleName
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initSubmitBtn() {
        game_submit.setOnClickListener {
            viewModel.submitLeague()
        }
    }

    override fun initBottomNavigation() {
        viewModel.getTransNum()
        sport_bottom_navigation.setNavigationItemClickListener {
            when (it) {
                R.id.navigation_sport -> {
                    //TODO navigate sport home
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
                        .replace(
                            R.id.fl_bet_list,
                            BetReceiptFragment.newInstance(betResultData, betParlayList)
                        )
                        .addToBackStack(BetReceiptFragment::class.java.simpleName)
                        .commit()
                }

            })

        transaction
            .add(R.id.fl_bet_list, betListFragment)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()
    }

    override fun updateBetListCount(num: Int) {
        sport_bottom_navigation.setBetCount(num)
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            setAnchorView(R.id.game_bottom_navigation)
            show()
        }
    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
        snackBarMyFavoriteNotify?.apply {
            setAnchorView(R.id.game_bottom_navigation)
            show()
        }
    }

    //公告
    private fun initRvMarquee() {
        rv_marquee.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_marquee.adapter = mMarqueeAdapter
    }

    private fun initTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectTab(tab?.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                selectTab(tab?.position)
            }
        })
    }

    private fun refreshTabLayout(sportMenuResult: SportMenuResult?) {
        try {
            val countInPlay =
                sportMenuResult?.sportMenuData?.menu?.inPlay?.items?.sumBy { it.num } ?: 0
            val countAtStart =
                sportMenuResult?.sportMenuData?.atStart?.items?.sumBy { it.num } ?: 0
            val countToday =
                sportMenuResult?.sportMenuData?.menu?.today?.items?.sumBy { it.num } ?: 0
            val countEarly =
                sportMenuResult?.sportMenuData?.menu?.early?.items?.sumBy { it.num } ?: 0
            val countParlay =
                sportMenuResult?.sportMenuData?.menu?.parlay?.items?.sumBy { it.num } ?: 0
            val countOutright =
                sportMenuResult?.sportMenuData?.menu?.outright?.items?.sumBy { it.num } ?: 0
            val countEps =
                sportMenuResult?.sportMenuData?.menu?.eps?.items?.sumBy { it.num } ?: 0

            val tabAll = tabLayout.getTabAt(0)?.customView
            tabAll?.tv_title?.setText(R.string.home_tan_main)
            //2022/01/05 主頁數量規則從使用"串關數量"修改為"其他玩法的加總"
            tabAll?.tv_number?.text = countParlay.plus(countInPlay).plus(countAtStart).plus(countToday).plus(countEarly).plus(countOutright).plus(countEps).toString()

            val tabInPlay = tabLayout.getTabAt(1)?.customView
            tabInPlay?.tv_title?.setText(R.string.home_tab_in_play)
            tabInPlay?.tv_number?.text = countInPlay.toString()

            val tabAtStart = tabLayout.getTabAt(2)?.customView
            tabAtStart?.tv_title?.setText(R.string.home_tab_at_start)
            tabAtStart?.tv_number?.text = countAtStart.toString()

            val tabToday = tabLayout.getTabAt(3)?.customView
            tabToday?.tv_title?.setText(R.string.home_tab_today)
            tabToday?.tv_number?.text = countToday.toString()

            val tabEarly = tabLayout.getTabAt(4)?.customView
            tabEarly?.tv_title?.setText(R.string.home_tab_early)
            tabEarly?.tv_number?.text = countEarly.toString()

            val tabOutright = tabLayout.getTabAt(5)?.customView
            tabOutright?.tv_title?.setText(R.string.home_tab_outright)
            tabOutright?.tv_number?.text = countOutright.toString()

            val tabParlay = tabLayout.getTabAt(6)?.customView
            tabParlay?.tv_title?.setText(R.string.home_tab_parlay)
            tabParlay?.tv_number?.text = countParlay.toString()

            val tabEps = tabLayout.getTabAt(7)?.customView
            tabEps?.tv_title?.setText(R.string.home_tab_eps)
            tabEps?.tv_number?.text = countEps.toString()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun selectTab(position: Int?) {
        when (position) {
            0 -> {
                mNavController.popBackStack(R.id.homeFragment, false)
            }
            1 -> {
                viewModel.switchMatchType(MatchType.IN_PLAY)
                loading()
            }
            2 -> {
                viewModel.switchMatchType(MatchType.AT_START)
                loading()
            }
            3 -> {
                viewModel.switchMatchType(MatchType.TODAY)
                loading()
            }
            4 -> {
                viewModel.switchMatchType(MatchType.EARLY)
                loading()
            }
            5 -> {
                viewModel.switchMatchType(MatchType.OUTRIGHT)
                loading()
            }
            6 -> {
                viewModel.switchMatchType(MatchType.PARLAY)
                loading()
            }
            7 -> {
                viewModel.switchMatchType(MatchType.EPS)
                loading()
            }
        }
    }

    private fun navGameFragment(matchType: MatchType) {
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
            R.id.gameOutrightFragment -> {
                val action =
                    GameOutrightFragmentDirections.actionGameOutrightFragmentToGameV3Fragment(
                        matchType
                    )
                mNavController.navigate(action)
            }
            R.id.gameOutrightMoreFragment -> {
                val action =
                    GameOutrightMoreFragmentDirections.actionGameOutrightMoreFragmentToGameV3Fragment(
                        matchType
                    )
                mNavController.navigate(action)
            }
            R.id.oddsDetailFragment -> {
                val action =
                    OddsDetailFragmentDirections.actionOddsDetailFragmentToGameV3Fragment(matchType)
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

    override fun onBackPressed() {
        //返回鍵優先關閉投注單fragment
        if (supportFragmentManager.backStackEntryCount != 0) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
            return
        }
        when (mNavController.currentDestination?.id) {
            R.id.gameLeagueFragment, R.id.gameOutrightFragment, R.id.gameOutrightMoreFragment, R.id.oddsDetailFragment, R.id.oddsDetailLiveFragment, R.id.leagueFilterFragment -> {
                mNavController.navigateUp()
            }

            R.id.gameV3Fragment -> {
                tabLayout.getTabAt(0)?.select()
            }

            else -> {
                super.onBackPressed()
            }
        }
    }

    //用戶登入公告訊息彈窗
    private var mNewsDialog: NewsDialog? = null
    private fun setNewsDialog(messageListResult: MessageListResult) {
        //未登入、遊客登入都要顯示彈窗
        if (!messageListResult.rows.isNullOrEmpty()) {
            mNewsDialog?.dismiss()
            mNewsDialog = NewsDialog(messageListResult.rows)
            mNewsDialog?.show(supportFragmentManager, null)
        }
    }

    private fun initObserve() {
        viewModel.settlementNotificationMsg.observe(this) {
            val message = it.getContentIfNotHandled()
            message?.let { messageNotnull -> view_notification.addNotification(messageNotnull) }
        }

        viewModel.isLogin.observe(this) {
            getAnnouncement()
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
                setNewsDialog(result) //公告彈窗
            }
        }

        viewModel.nowTransNum.observe(this) {
            navigation_transaction_status.trans_number.text = it.toString()
        }

        viewModel.specialEntrance.observe(this) {
            hideLoading()
            if (it?.couponCode.isNullOrEmpty()) {
                when (it?.matchType) {
                    MatchType.IN_PLAY -> {
                        tabLayout.getTabAt(1)?.select()
                    }
                    MatchType.AT_START -> {
                        tabLayout.getTabAt(2)?.select()
                    }
                    MatchType.TODAY -> {
                        tabLayout.getTabAt(3)?.select()
                    }
                    MatchType.EARLY -> {
                        tabLayout.getTabAt(4)?.select()
                    }
                    MatchType.OUTRIGHT -> {
                        tabLayout.getTabAt(5)?.select()
                    }
                    MatchType.PARLAY -> {
                        tabLayout.getTabAt(6)?.select()
                    }
                    MatchType.EPS -> {
                        tabLayout.getTabAt(7)?.select()
                    }
                    MatchType.OTHER -> {
                        tabLayout.getTabAt(3)?.select()
                    }
                }
            } else {
                //viewModel.switchSpecialMatchType(it!!.couponCode!!)
                navGameFragment(it!!.matchType)
            }
        }


        viewModel.curMatchType.observe(this) {
            it?.let {
                navGameFragment(it)
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
                //[Martin]
                if(viewModel.getIsFastBetOpened()){
                    showFastBetFragment()
                }else{
                    showBetListPage()
                }
            }
        }
    }

    private fun showFastBetFragment(){
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit,
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit
            )

        val betListFragment = FastBetFragment()

        transaction
            .add(R.id.fl_bet_list, betListFragment)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()
    }

    private fun initServiceButton() {
        btn_floating_service.setView(this)
    }

    override fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {
            btn_login.visibility = View.GONE
            btn_register.visibility = View.GONE
            toolbar_divider.visibility = View.GONE
            iv_head.visibility = View.VISIBLE
            tv_odds_type.visibility = View.VISIBLE
        } else {
            btn_login.visibility = View.VISIBLE
            btn_register.visibility = View.VISIBLE
            toolbar_divider.visibility = View.VISIBLE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
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

        tabLayout.getTabAt(0)?.select()
    }

    private fun updateUiWithResult(messageListResult: MessageListResult?) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult?.let {
            it.rows?.forEach { data -> titleList.add(data.title + " - " + data.message) }

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

    private fun queryData() {
        loading()
        getSportList()
    }

    private fun getSportList() {
        viewModel.getSportList()
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
    }

    private fun updateSelectTabState(matchType: MatchType?) {
        when (matchType) {
            MatchType.IN_PLAY -> updateSelectTabState(1)
            MatchType.AT_START -> updateSelectTabState(2)
            MatchType.TODAY -> updateSelectTabState(3)
            MatchType.EARLY -> updateSelectTabState(4)
            MatchType.OUTRIGHT -> updateSelectTabState(5)
            MatchType.PARLAY -> updateSelectTabState(6)
            MatchType.EPS -> updateSelectTabState(7)
        }
    }

    private fun updateSelectTabState(position: Int) {
        val tab = tabLayout.getTabAt(position)?.customView

        tab?.let {
            clearSelectTabState()

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

    private fun removeBetListFragment(){
        supportFragmentManager.beginTransaction().remove(betListFragment).commit()
    }
}