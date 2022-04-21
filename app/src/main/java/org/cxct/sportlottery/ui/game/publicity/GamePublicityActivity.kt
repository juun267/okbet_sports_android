package org.cxct.sportlottery.ui.game.publicity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityGamePublicityBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
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
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MetricsUtil
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
    }

    override fun onResume() {
        super.onResume()

        getSportMenuFilter()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        initDestination()
    }

    private fun initDestination() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.publicityFragment -> {
                    binding.publicityToolbar.toolBar.visibility = View.GONE
                }
                else -> {
                    binding.publicityToolbar.toolBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun initViews() {
        initToolBar()
        initServiceButton()
        initOnClickListener()
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
    }

    private fun initObservers() {
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

    fun removeBetListFragment() {
        supportFragmentManager.beginTransaction().remove(betListFragment).commit()
    }
    //endregion

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
            }
        }
    }

    fun clickMenu() {
        with(binding) {
            if (drawerLayout.isDrawerOpen(viewNavRight.navRight)) drawerLayout.closeDrawers()
            else {
                drawerLayout.openDrawer(viewNavRight.navRight)
                viewModel.getMoney()
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