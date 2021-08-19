package org.cxct.sportlottery.ui.main.accountHistory

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_account_history.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.view.*
import kotlinx.android.synthetic.main.view_message.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.betList.receipt.BetReceiptFragment
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeDialog
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.util.MetricsUtil


class AccountHistoryActivity :
    BaseBottomNavActivity<AccountHistoryViewModel>(AccountHistoryViewModel::class) {

    private val navController by lazy { findNavController(R.id.account_history_container) }
    private val mMarqueeAdapter by lazy { MarqueeAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_history)

        initBottomNavigation()
        initToolBar()
        initRvMarquee()
        initMenu()
        setupNoticeButton(btn_notice)
        initObserve()
    }

    override fun initBottomNavigation() {
        sport_bottom_navigation.apply {
            setNavigationItemClickListener {
                when (it) {
                    R.id.navigation_sport -> {
                        viewModel.navGame()
                        finish()
                        false
                    }
                    R.id.navigation_game -> {
                        viewModel.navMyFavorite()
                        finish()
                        false
                    }
                    R.id.item_bet_list -> {
                        viewModel.navShoppingCart()
                        false
                    }
                    R.id.navigation_account_history -> {
                        true
                    }
                    R.id.navigation_transaction_status -> {
                        viewModel.navTranStatus()
                        finish()
                        false
                    }
                    else -> false
                }
            }

            setSelected(R.id.navigation_account_history)
        }
    }

    override fun showBetListPage() {
        val betListFragment =
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

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit,
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit
            )
            .add(R.id.fl_bet_list, betListFragment)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()
    }

    override fun updateBetListCount(num: Int) {
        sport_bottom_navigation.setBetCount(num)
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            setAnchorView(R.id.sport_bottom_navigation)
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()
    }

    override fun onBackPressed() {
        when (navController.currentDestination?.id) {
            R.id.accountHistoryNextFragment -> {
                navController.navigateUp()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun initObserve() {

        viewModel.oddsType.observe(this, {
            tv_odds_type.text = getString(it.res)
        })

        viewModel.messageListResult.observe(this, {
            updateUiWithResult(it)
        })

        viewModel.settlementNotificationMsg.observe(this, {
            val message = it.getContentIfNotHandled()
            message?.let { messageNotnull -> view_notification.addNotification(messageNotnull) }
        })

        viewModel.isLogin.observe(this, {
            getAnnouncement()
        })

        viewModel.thirdGameCategory.observe(this, {
            it.getContentIfNotHandled().let { thirdGameCategory ->
                if (thirdGameCategory != null) {
                    val intent = Intent(this, MainActivity::class.java)
                        .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
                    startActivity(intent)

                    return@let
                }

                startActivity(Intent(this, GameActivity::class.java))
            }
        })
    }

    private fun updateUiWithResult(messageListResult: MessageListResult?) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult?.let {
            it.rows?.forEach { data -> titleList.add(data.title + " - " + data.message) }

            mMarqueeAdapter.setData(titleList)

            if (messageListResult.success && titleList.size > 0) {
                rv_marquee.startAuto() //啟動跑馬燈
            } else {
                rv_marquee.stopAuto() //停止跑馬燈
            }
        }
    }

    private fun initRvMarquee() {
        rv_marquee.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_marquee.adapter = mMarqueeAdapter
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
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

        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    override fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)

        //點擊 logo 回到首頁
        iv_logo.setOnClickListener {
            viewModel.navMainPage(ThirdGameCategory.MAIN)
        }

        //頭像 當 側邊欄 開/關
        iv_head.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this@AccountHistoryActivity, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this@AccountHistoryActivity, RegisterActivity::class.java))
        }

        tv_odds_type.setOnClickListener {
            ChangeOddsTypeDialog().show(supportFragmentManager, null)
        }
    }
}