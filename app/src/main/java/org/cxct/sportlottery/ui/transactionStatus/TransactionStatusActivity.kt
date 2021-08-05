package org.cxct.sportlottery.ui.transactionStatus

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.fragment_transaction_status.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import kotlinx.android.synthetic.main.view_message.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.Row
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseNoticeActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.betList.BetReceiptFragment
import org.cxct.sportlottery.ui.game.bottomNavigation.BottomNavigationItem
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeDialog
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.util.MetricsUtil

class TransactionStatusActivity : BaseNoticeActivity<TransactionStatusViewModel>(TransactionStatusViewModel::class) {

    private val mMarqueeAdapter by lazy { MarqueeAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_status)

        setupNoticeButton(btn_notice)
        initToolBar()
        initMenu()
        initButtonToTop()
        initBottomNavigation()
        initRvMarquee()
        initObserver()
        getAnnouncement()
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()
    }

    private fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)
        iv_logo.setOnClickListener {
            goToMainActivity()
        }

        //頭像 當 側邊欄 開/關
        iv_head.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tv_odds_type.setOnClickListener {
            ChangeOddsTypeDialog().show(supportFragmentManager, null)
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, ThirdGameCategory.MAIN)
        startActivity(intent)
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

    //TODO fix bug: 滾動時跳至頂部滾動仍持續，造成偏移
    private fun initButtonToTop() {
        btn_return_to_top.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                val transactionStatusFragment =
                    (supportFragmentManager.findFragmentById(R.id.fragment_transaction_status) as TransactionStatusFragment)
                transactionStatusFragment.scroll_view.apply {
                    stopNestedScroll()
                    fullScroll(View.FOCUS_UP)
//                    scrollTo(0,top)
//                    smoothScrollTo(0,0,1000)
                }
            }
        }
    }

    private fun initBottomNavigation() {
        initNavigationView()
        initNavigationListener()

        val transactionStatusFragment =
            (supportFragmentManager.findFragmentById(R.id.fragment_transaction_status) as TransactionStatusFragment)
        transactionStatusFragment.setBottomNavigationListener(object :
            TransactionStatusFragment.BottomNavigationListener {
            override fun onSportHomeNav() {
                findViewById<BottomNavigationItem>(R.id.navigation_sport).performClick()
            }
        })
    }

    private fun initNavigationView() {
        sport_bottom_navigation.setSelected(R.id.navigation_transaction_status)
    }

    private fun initNavigationListener() {
        sport_bottom_navigation.setNavigationItemClickListener {
            when (it) {
                R.id.navigation_sport -> {
                    finish()
                    startActivity(Intent(this, GameActivity::class.java))
                    false
                }
                R.id.navigation_game -> {
                    //TODO navigate sport game
                    false
                }
                R.id.item_bet_list -> {
                    showBetListPage()
                    false
                }
                R.id.navigation_account_history -> {
                    //TODO navigate account_history
                    false
                }
                R.id.navigation_transaction_status -> {
                    startActivity(Intent(this, TransactionStatusActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun showBetListPage() {
        val betListFragment = BetListFragment.newInstance(object : BetListFragment.BetResultListener {
            override fun onBetResult(betResultData: List<Row>?) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.push_right_to_left_enter, R.anim.pop_bottom_to_top_exit, R.anim.push_right_to_left_enter, R.anim.pop_bottom_to_top_exit)
                    .replace(R.id.fl_bet_list, BetReceiptFragment.newInstance(betResultData))
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

    private fun initRvMarquee() {
        rv_marquee.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_marquee.adapter = mMarqueeAdapter
    }

    private fun initObserver() {
        viewModel.messageListResult.observe(this, {
            updateUiWithResult(it)
        })

        viewModel.isLogin.observe(this, {
            updateUiWithLogin(it)
            getAnnouncement()
        })

        viewModel.oddsType.observe(this, {
            tv_odds_type.text = getString(it.res)
        })
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
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
}