package org.cxct.sportlottery.ui.main.accountHistory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.drawer_layout
import kotlinx.android.synthetic.main.activity_main.nav_right
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseNoticeActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.util.MetricsUtil

class AccountHistoryActivity : BaseNoticeActivity<AccountHistoryViewModel>(AccountHistoryViewModel::class)  {

    private val navController by lazy { findNavController(R.id.account_history_container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_history)
        initToolBar()
        initMenu()
        setupNoticeButton(btn_notice)
        initObserve()
        initNavigationListener()
    }

    private fun initNavigationListener() {
        bottom_navigation_sport.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home_page -> {
                    startActivity(Intent(this, GameActivity::class.java))
                    finish()
                    true
                }
                R.id.game_page -> {
                    //TODO navigate sport game
                    false
                }
                R.id.bet_list -> {
                    //TODO open bet list page
                    false
                }
                R.id.account_history -> {
                    false
                }
                R.id.transaction_status -> {
                    //TODO navigate transaction_status
                    true
                }
                else -> false
            }
        }
    }

    private fun initObserve() {

        viewModel.oddsType.observe(this, {
            tv_odds_type.text = getString(it.res)
        })

        receiver.orderSettlement.observe(this, {
            viewModel.getSettlementNotification(it)
        })

        viewModel.settlementNotificationMsg.observe(this, {
            val message = it.getContentIfNotHandled()
            message?.let { messageNotnull -> view_notification.addNotification(messageNotnull) }
        })

        viewModel.isLogin.observe(this, {
            updateUiWithLogin(it)
            getAnnouncement()
        })
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
    }

    private fun initMenu() {
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

    private fun updateUiWithLogin(isLogin: Boolean) {
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

    private fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)

        //點擊 logo 回到首頁
        iv_logo.setOnClickListener {
            navController.popBackStack(R.id.mainFragment, false)
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
    }

}