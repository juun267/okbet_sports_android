package org.cxct.sportlottery.ui.profileCenter.cancelaccount

import android.content.Intent
import android.text.method.HideReturnsTransformationMethod
import androidx.lifecycle.Observer
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityCancelAccountBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.feedback.FeedbackMainActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod

/**
 * 注销账号页面  ios需要安卓暂时不需要 此页面隐藏
 */

class CancelAccountActivity :BindingActivity<CancelAccountViewModel,ActivityCancelAccountBinding>() {
    
    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
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
    private fun onClick()=binding.run{

        eetPassword.endIconImageButton.setOnClickListener{
            if (eetPassword.endIconResourceId == R.drawable.ic_eye_open) {
                etPassword.transformationMethod =
                    AsteriskPasswordTransformationMethod()
                eetPassword.setEndIcon(R.drawable.ic_eye_close)
            } else {
                eetPassword.setEndIcon(R.drawable.ic_eye_open)
                etPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
            eetPassword.hasFocus = true
            etPassword.setSelection(etPassword.text.toString().length)
        }
        btnCancelAccount.setOnClickListener{
            etPassword.text?.let {
                viewModel.cancelAccount(it.toString())
            }
        }
        btnFeedback.setOnClickListener{
            startActivity(Intent(this@CancelAccountActivity, FeedbackMainActivity::class.java))
        }

        customToolBar.setOnBackPressListener {
            finish()
        }
    }

    private fun init()=binding.run{
        customToolBar.titleText = getString(R.string.cancel_account)
        etPassword.transformationMethod =
            AsteriskPasswordTransformationMethod()
    }
}