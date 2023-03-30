package org.cxct.sportlottery.ui.profileCenter.cancelaccount

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_cancel_account.*
import kotlinx.android.synthetic.main.activity_cancel_account.custom_tool_bar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.feedback.FeedbackMainActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod

/**
 * 注销账号页面  ios需要安卓暂时不需要 此页面隐藏
 */

class CancelAccountActivity :BaseActivity<CancelAccountViewModel>(CancelAccountViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_cancel_account)
        init()
        onClick()
        initObserve()
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

        et_qq_number.endIconImageButton.setOnClickListener{
            if (et_qq_number.endIconResourceId == R.drawable.ic_eye_open) {
                et_password.transformationMethod =
                    AsteriskPasswordTransformationMethod()
                et_qq_number.setEndIcon(R.drawable.ic_eye_close)
            } else {
                et_qq_number.setEndIcon(R.drawable.ic_eye_open)
                et_password.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
            et_qq_number.hasFocus = true
            et_password.setSelection(et_password.text.toString().length)
        }
        btn_cancel_account.setOnClickListener{
            et_password.text?.let {
                viewModel.cancelAccount(it.toString())
            }
        }
        btn_feedback.setOnClickListener{
            startActivity(Intent(this@CancelAccountActivity, FeedbackMainActivity::class.java))
        }

        custom_tool_bar.setOnBackPressListener {
            finish()
        }
    }

    private fun init(){
        custom_tool_bar.titleText = getString(R.string.cancel_account)
        et_password.transformationMethod =
            AsteriskPasswordTransformationMethod()
    }
}