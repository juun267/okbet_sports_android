package org.cxct.sportlottery.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_game.drawer_layout
import kotlinx.android.synthetic.main.toast_top_bet_result.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_nav_right.nav_right
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity

class GameActivity : BaseOddButtonActivity<GameViewModel>(GameViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setupToolbar()

        initObserver()
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

    private fun initObserver() {
        viewModel.isLogin.observe(this, Observer {
            updateLoginWidget(it)
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
}