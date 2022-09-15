package org.cxct.sportlottery.ui.profileCenter.cancelaccount

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_cancel_account.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.feedback.FeedbackMainActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity

/**
 * 注销账号页面
 */

class CancelAccountActivity :BaseActivity<CancelAccountViewModel>(CancelAccountViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_cancel_account)
        initObserve()
        onClick()
    }
    private fun initObserve() {
        viewModel.cancelResult.observe(this, Observer {
            if (it.success){
                viewModel.doLogoutAPI() //退出登录
                viewModel.doLogoutCleanUser {//清除用户设置跳转到首页
                    run {
                        MainTabActivity.reStart(this)
                    }
                }
            }
        })
    }
    private fun onClick(){
        btn_cancel_account.setOnClickListener{
            et_password.text?.let {
                viewModel.cancelAccount(it.toString())
            }
        }
        btn_feedback.setOnClickListener{
            startActivity(Intent(this, FeedbackMainActivity::class.java))
        }
    }
}