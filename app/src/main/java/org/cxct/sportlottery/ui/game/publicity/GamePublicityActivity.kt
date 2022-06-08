package org.cxct.sportlottery.ui.game.publicity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityGamePublicityBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.dialog.AgeVerifyDialog
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.Page
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.betList.FastBetFragment
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity.Companion.FROM_ACTIVITY
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.AppManager
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MetricsUtil
import org.cxct.sportlottery.util.setVisibilityByCreditSystem
import org.parceler.Parcels

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
        setLetterSpace()

        //進入宣傳頁，優先跳出這個視窗(不論有沒有登入，每次都要跳)
        showAgeVerifyDialog()
    }

    //確認年齡彈窗
    private fun showAgeVerifyDialog() {
        if (MultiLanguagesApplication.getInstance()?.isAgeVerifyNeedShow() == false) return
        AgeVerifyDialog(
            this,
            object : AgeVerifyDialog.OnAgeVerifyCallBack {
                override fun onConfirm() {
                    //當玩家點擊"I AM OVER 21 YEARS OLD"後，關閉此視窗
                    MultiLanguagesApplication.getInstance()?.setIsAgeVerifyShow(false)
                }

                override fun onExit() {
                    //當玩家點擊"EXIT"後，徹底關閉APP
                    AppManager.AppExit()
                }

            }).show()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        initDestination()
    }
    private fun setLetterSpace(){
        if (LanguageManager.getSelectLanguage(this)==LanguageManager.Language.ZH) {
            binding.tvRegister.letterSpacing = 0.6f
            binding.tvLogin.letterSpacing = 0.6f
        }
    }
    private fun initDestination() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.publicityFragment -> {
                    binding.gameToolbar.toolBar.visibility = View.GONE
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
        //region publicityToolbar
        with(binding) {
            publicityToolbar.ivLanguage.setImageResource(LanguageManager.getLanguageFlag(this@GamePublicityActivity))
            publicityToolbar.tvLanguage.text = LanguageManager.getLanguageStringResource(this@GamePublicityActivity)
        }
        //endregion
    }

    override fun updateNoticeButton(noticeCount: Int) {
        val updatedNoticeResource = if (noticeCount > 0) R.drawable.icon_bell_with_red_dot else R.drawable.icon_bell
        with(binding) {
            publicityToolbar.ivNotice.setImageResource(updatedNoticeResource)
            gameToolbar.ivNotice.setImageResource(updatedNoticeResource)
        }
    }

    private fun initServiceButton() {
        binding.btnFloatingService.setView(this)
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
        //endregion
    }

    private fun initObservers() {
        viewModel.showBetUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true)
                snackBarBetUpperLimitNotify.apply {
                    setAnchorView(R.id.viewBottom)
                    show()
                }
        }

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
            if (sConfigData?.thirdOpen == FLAG_OPEN) {
                MainActivity.reStart(this)
            } else {
                AppManager.AppExit()
            }
        }
    }

    private fun getSportMenuFilter() {
        viewModel.getSportMenuFilter()
    }

    //region 開啟(快捷)投注單
    //跟進GameActivity開啟投注單方式
    fun showFastBetFragment(fastBetDataBean: FastBetDataBean) {
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
                        if (sConfigData?.thirdOpen == FLAG_OPEN)
                            MainActivity.reStart(this@GamePublicityActivity)
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
        viewModel.getTransNum()
        binding.gameBottomNavigation.sportBottomNavigation.clearSelectedStatus()
        binding.gameBottomNavigation.sportBottomNavigation.setNavigationItemClickListener {
            when (it) {
                R.id.navigation_sport -> {
                    viewModel.navGame()
                    false
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

                viewBottom.layoutParams.height = 55.dp
            } else {
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