package org.cxct.sportlottery.ui.game.publicity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_game_publicity.*
import kotlinx.android.synthetic.main.content_bet_info_item_v3.view.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityGamePublicityBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.Page
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity.Companion.FROM_ACTIVITY
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.news.NewsActivity
import org.cxct.sportlottery.util.*

class GamePublicityActivity : BaseBottomNavActivity<GameViewModel>(GameViewModel::class),
    View.OnClickListener {
    private lateinit var binding: ActivityGamePublicityBinding

    private val navController by lazy { findNavController(binding.publicityContainer.id) }

    companion object {
        const val IS_FROM_PUBLICITY = "isFromPublicity"
        const val PUBLICITY_GAME_TYPE = "publicityGameType"
        const val PUBLICITY_MATCH_ID = "publicityMatchId"
        const val PUBLICITY_MATCH_TYPE = "publicityMatchType"
        const val PUBLICITY_MATCH_LIST = "publicityMatchList"

        fun reStart(context: Context) {
            if (MultiLanguagesApplication.mInstance.doNotReStartPublicity) {
                MultiLanguagesApplication.mInstance.doNotReStartPublicity = false
                AppManager.currentActivity().finish()
                return
            }
            val intent = Intent(context, GamePublicityActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.push_right_to_left_enter, R.anim.push_right_to_left_exit)
            }
        }
    }

    private var betListFragment = BetListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamePublicityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initBaseFun()
        initObservers()
        setupDataSourceChange()

        //進入宣傳頁，優先跳出這個視窗(不論有沒有登入，每次都要跳)
//        if (sConfigData?.thirdOpen != FLAG_OPEN)
        MultiLanguagesApplication.showAgeVerifyDialog(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        initDestination()
    }

    private fun initDestination() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                /*R.id.publicityFragment -> {
                    binding.gameToolbar.toolBar.visibility = View.GONE
                    setupNoticeButton(binding.publicityToolbar.ivNotice)
                }*/
                R.id.publicityFragment -> {
                    binding.gameToolbar.toolBar.visibility = View.GONE
                    binding.publicityToolbar.toolBar.visibility = View.VISIBLE
                    setupNoticeButton(binding.publicityToolbar.ivNotice)
                }
                else -> {
                    binding.gameToolbar.toolBar.visibility = View.VISIBLE
                    setupNoticeButton(binding.gameToolbar.ivNotice)
                }
            }
        }
    }

    private fun initViews() {
        initToolBar()
        initServiceButton()
        initRegionViewBtn()
        initOnClickListener()
    }

    private fun initBaseFun() {
        initMenu()
        initBottomNavigation()
    }

    override fun initToolBar() {
        //region Language block
        with(binding) {
            publicityToolbar.ivLanguage.setImageResource(LanguageManager.getLanguageFlag(this@GamePublicityActivity))
            publicityToolbar.tvLanguage.text = LanguageManager.getLanguageStringResource(this@GamePublicityActivity)
        }
        //endregion
    }

    override fun updateNoticeButton(noticeCount: Int) {
        val updatedNoticeResource = if (noticeCount > 0) R.drawable.icon_bell_with_red_dot else R.drawable.icon_bell
        with(binding) {
            gameToolbar.ivNotice.setImageResource(updatedNoticeResource)
        }
    }

    private fun initServiceButton() {
        binding.gameBottomNavigation.btnFloatingService.setView(this)
    }

    private fun initRegionViewBtn() {
        //region view bottom
        binding.tvRegister.letterSpacing = 0.0892957143f
        binding.tvLogin.letterSpacing = 0.0892957143f

        binding.tvRegister.setVisibilityByCreditSystem()
    }


    private fun initOnClickListener() {
        //region view bottom
        binding.tvRegister.setOnClickListener(this)
        binding.tvLogin.setOnClickListener(this)
        //endregion
        //region publicity tool bar
        binding.publicityToolbar.ivLogo.setOnClickListener(this)
        binding.publicityToolbar.blockLanguage.setOnClickListener(this)
        binding.publicityToolbar.ivMenu.setOnClickListener(this)
        //endregion
        //region game tool bar
        binding.gameToolbar.ivLogo.setOnClickListener(this)
        binding.gameToolbar.btnLogin.setOnClickListener(this)
        binding.gameToolbar.btnRegister.setOnClickListener(this)
        binding.gameToolbar.ivMenu.setOnClickListener(this)

        binding.gameBottomNavigation.clBetListBar.setOnClickListener {
            showBetListPage()
        }
        //endregion

    }

    private fun initObservers() {
        viewModel.userMoney.observe(this) {
            it?.let { money ->
                tv_balance.text = TextUtil.formatMoney(money)
            }
        }
        viewModel.showBetUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true)
                snackBarBetUpperLimitNotify.apply {
                    anchorView = game_Bottom_Navigation
                    show()
                }
        }

        viewModel.showBetInfoSingle.observe(this) { event ->
            event?.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
            return
        }
        //關閉drawer
        binding.apply {
            if (drawerLayout.isDrawerOpen(viewNavRight.navRight)) {
                drawerLayout.closeDrawers()
                return
            }
        }
        if (navController.currentDestination?.id != R.id.publicityFragment) {
            navController.navigateUp()
        } else {
//            if (sConfigData?.thirdOpen == FLAG_OPEN) {
//                MainActivity.reStart(this)
//            } else {
                AppManager.AppExit()
//            }
        }
    }

    //region 開啟(快捷)投注單
    //跟進GameActivity開啟投注單方式
    fun setupBetData(fastBetDataBean: FastBetDataBean) {
        viewModel.updateMatchBetListData(fastBetDataBean)
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
                override fun onBetResult(
                    betResultData: Receipt?,
                    betParlayList: List<ParlayOdd>,
                    isMultiBet: Boolean
                ) {
                    showBetReceiptDialog(betResultData, betParlayList, isMultiBet, binding.flBetList.id)
                }

            }, showToolbar = navController.currentDestination?.id == R.id.publicityFragment)

        transaction
            .add(binding.flBetList.id, betListFragment, BetListFragment::class.java.simpleName)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()

    }

    override fun getBetListPageVisible(): Boolean {
        return betListFragment.isVisible
    }

    fun removeBetListFragment() {
        supportFragmentManager.beginTransaction().remove(betListFragment).commit()
    }
    //endregion

    override fun onClick(v: View?) {
        avoidFastDoubleClick()
        with(binding) {
            when (v) {
                tvRegister, gameToolbar.btnRegister -> {
                    goRegisterPage()
                }
                tvLogin, gameToolbar.btnLogin -> {
                    goLoginPage()
                }

                publicityToolbar.ivLogo, gameToolbar.ivLogo -> {
                    if (navController.currentDestination?.id != R.id.publicityFragment) {
                        navController.navigateUp()
                    } else {
//                        if (sConfigData?.thirdOpen == FLAG_OPEN)
//                            MainActivity.reStart(this@GamePublicityActivity)
                    }
                    removeBetListFragment()
                }
                publicityToolbar.blockLanguage -> {
                    goSwitchLanguagePage()
                }
                publicityToolbar.ivMenu, gameToolbar.ivMenu -> {
                    if (drawerLayout.isDrawerOpen(viewNavRight.navRight)) drawerLayout.closeDrawers()
                    else {
                        drawerLayout.openDrawer(viewNavRight.navRight)
                        viewModel.getMoney()
                    }
                }
            }
        }
    }

    override fun clickMenuEvent() {
        with(binding) {
            if (drawerLayout.isDrawerOpen(viewNavRight.navRight)) drawerLayout.closeDrawers()
            else {
                drawerLayout.openDrawer(viewNavRight.navRight)
                viewModel.getMoney()
            }
        }
    }

    fun fragmentClickNotice() {
        onCloseMenu()
        startActivity(
            Intent(this, InfoCenterActivity::class.java)
                .putExtra(InfoCenterActivity.KEY_READ_PAGE, InfoCenterActivity.YET_READ)
        )
    }

    fun fragmentClickNews() {
        onCloseMenu()
        startActivity(
            Intent(this, NewsActivity::class.java)
        )
    }

    private fun goRegisterPage() {
        startActivity(Intent(this@GamePublicityActivity, RegisterOkActivity::class.java))
    }

    private fun goLoginPage() {
        startActivity(Intent(this@GamePublicityActivity, LoginActivity::class.java))
    }

    private fun goGamePage() {
        GameActivity.reStart(this)
        finish()
    }

    fun goSwitchLanguagePage() {
        startActivity(Intent(this@GamePublicityActivity, SwitchLanguageActivity::class.java).apply {
            putExtra(FROM_ACTIVITY, Page.PUBLICITY)
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
        tv_balance_currency.text = sConfigData?.systemCurrencySign
        tv_balance.text = TextUtil.formatMoney(0.0)
        binding.gameBottomNavigation.sportBottomNavigation.apply {
            setNavigationItemClickListener {
                when (it) {
                    R.id.navigation_home -> {
                        true
                    }
                    R.id.navigation_sport -> {
                        viewModel.navGame()
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
                    R.id.navigation_my -> {
                        viewModel.navMy()
                        false
                    }
                    else -> false
                }
            }
            setSelected(R.id.navigation_home)
        }
    }

    override fun updateUiWithLogin(isLogin: Boolean) {
        with(binding) {
            if (isLogin) {
                viewBottom.visibility = View.GONE
                gameBottomNavigation.sportBottomNavigation.visibility = View.VISIBLE

                //region publicity tool bar
                publicityToolbar.ivNotice.visibility = View.VISIBLE
                publicityToolbar.ivMenu.visibility = View.VISIBLE

                publicityToolbar.blockLanguage.visibility = View.GONE
                //endregion

                //region game tool bar
                with(gameToolbar) {
                    ivNotice.visibility = View.VISIBLE
                    ivMenu.visibility = View.VISIBLE

                    btnLogin.visibility = View.GONE
                    btnRegister.visibility = View.GONE
                    toolbarDivider.visibility = View.GONE
                }
                //endregion
            } else {
                viewBottom.visibility = View.VISIBLE
                gameBottomNavigation.sportBottomNavigation.visibility = View.GONE

                //region publicity tool bar
                publicityToolbar.ivNotice.visibility = View.GONE
                publicityToolbar.ivMenu.visibility = View.GONE

                publicityToolbar.blockLanguage.visibility = View.VISIBLE
                //endregion

                //region game tool bar
                with(gameToolbar) {
                    ivNotice.visibility = View.GONE
                    ivMenu.visibility = View.GONE

                    btnLogin.visibility = View.VISIBLE
                    btnRegister.visibility = View.VISIBLE
                    toolbarDivider.visibility = View.VISIBLE
                }
                //endregion
            }
        }
    }

    override fun updateOddsType(oddsType: OddsType) {
        //盤口顯示方式已調整至右側邊欄
    }

    override fun updateBetListCount(num: Int) {
        binding.gameBottomNavigation.sportBottomNavigation.setBetCount(num)
        if (viewModel.isLogin.value == true) {
            cl_bet_list_bar.isVisible = num > 0
            binding.gameBottomNavigation.lineShadow.isVisible = !cl_bet_list_bar.isVisible
        }
        tv_bet_list_count.text = num.toString()
        if (num > 0) viewModel.getMoney()
    }
    override fun updateBetListOdds(list: MutableList<BetInfoListData>) {
        val multipleOdds = getMultipleOdds(list)
        cl_bet_list_bar.tvOdds.text = "@ $multipleOdds"
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            anchorView = if (viewBottom.isVisible) viewBottom else game_Bottom_Navigation
            show()
        }
    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
        snackBarMyFavoriteNotify?.apply {
            anchorView = game_Bottom_Navigation
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

    private fun setupDataSourceChange() {
        setDataSourceChangeEvent {
            if (navController.currentDestination?.id != R.id.publicityFragment) {
                navController.navigateUp()
            } else {
//                navController.navigate(PublicityFragmentDirections.actionPublicityFragmentSelf())
                //新版宣傳頁
                navController.navigate(PublicityNewFragmentDirections.actionPublicityFragmentSelf())
            }
        }
    }

}