package org.cxct.sportlottery.ui.thirdGame

import android.content.Intent
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_third_game.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.ToastUtil

open class ThirdGameActivity : WebActivity() {

    private var mUserInfo: UserInfo? = null

    override fun init() {
        setContentView(R.layout.activity_third_game)
        setCookie()
        setupWebView(web_view)
        loadUrl(web_view)
        setupMenu()
        initObserve()
    }

    private fun setupMenu() {
        motion_menu.setOnMenuListener(object : MotionFloatingMenu.OnMenuListener {
            override fun onHome() {
                finish()
            }

            override fun onCashSave() {
                if (checkLogin()) {
                    startActivity(Intent(this@ThirdGameActivity, MoneyRechargeActivity::class.java))
                    finish()
                }
            }

            override fun onCashGet() {
                if (checkLogin()) {
                    startActivity(Intent(this@ThirdGameActivity, WithdrawActivity::class.java))
                    finish()
                }
            }
        })
    }


    private fun checkLogin(): Boolean {
        return when (mUserInfo?.testFlag) {
            TestFlag.NORMAL.index -> true
            TestFlag.GUEST.index -> {
                ToastUtil.showToastInCenter(this, resources.getString(R.string.message_guest_no_permission))
                false
            }
            else -> {
                startActivity(Intent(this, LoginActivity::class.java))
                false
            }
        }
    }

    private fun initObserve() {
        viewModel.userInfo.observe(this, Observer {
            mUserInfo = it
        })
    }
}
