package org.cxct.sportlottery.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_game.drawer_layout
import kotlinx.android.synthetic.main.toast_top_bet_result.*
import kotlinx.android.synthetic.main.view_message.*
import kotlinx.android.synthetic.main.view_message.rv_marquee
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_nav_right.nav_right
import kotlinx.android.synthetic.main.view_toolbar_main.*
import kotlinx.android.synthetic.main.view_toolbar_main.btn_login
import kotlinx.android.synthetic.main.view_toolbar_main.btn_register
import kotlinx.android.synthetic.main.view_toolbar_main.iv_head
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.util.MetricsUtil

class GameActivity : BaseOddButtonActivity<GameViewModel>(GameViewModel::class) {

    private val mMarqueeAdapter by lazy {
        MarqueeAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setupToolbar()
        setupDrawer()
        setupMessage()

        initObserver()
    }

    override fun onResume() {
        super.onResume()

        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()

        rv_marquee.stopAuto()
    }

    private fun setupToolbar() {
        iv_head.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this@GameActivity, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this@GameActivity, RegisterActivity::class.java))
        }
    }

    private fun setupDrawer() {
        try {
            //關閉側邊欄滑動行為
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            //選單選擇結束要收起選單
            val menuFrag =
                supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener(View.OnClickListener { drawer_layout.closeDrawers() })

            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupMessage() {
        rv_marquee.apply {
            layoutManager =
                LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = mMarqueeAdapter
        }
    }

    private fun initObserver() {
        viewModel.isLogin.observe(this, Observer {
            updateLoginWidget(it)

            viewModel.getMessage()
            loading()
        })

        viewModel.messageListResult.observe(this, Observer {
            hideLoading()
            updateMessage(it)
        })

    }

    private fun updateLoginWidget(isLogin: Boolean) {
        when (isLogin) {
            true -> {
                btn_login.visibility = View.GONE
                btn_register.visibility = View.GONE
                toolbar_divider.visibility = View.GONE
                iv_head.visibility = View.VISIBLE
            }
            false -> {
                btn_login.visibility = View.VISIBLE
                btn_register.visibility = View.VISIBLE
                toolbar_divider.visibility = View.VISIBLE
                iv_head.visibility = View.GONE
            }
        }
    }

    private fun updateMessage(messageList: List<String>) {
        mMarqueeAdapter.setData(messageList.toMutableList())

        if (messageList.isEmpty()) {
            rv_marquee.stopAuto()
        } else {
            rv_marquee.startAuto()
        }
    }
}