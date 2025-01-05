package org.cxct.sportlottery.ui.profileCenter.authbind

import android.content.Intent
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.showErrorPromptDialog
import org.cxct.sportlottery.databinding.ActivityAuthBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.AuthManager
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.setServiceClick
import org.cxct.sportlottery.util.setStartDrawable

/**
 * @app_destination 修改暱稱
 */
class AuthActivity : BaseActivity<AuthViewModel, ActivityAuthBinding>() {

    override fun pageName() = "三方账号管理页面"

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        initButton()
        setupServiceButton()
        initObserve()
        viewModel.getUserInfo()
    }

    private fun initButton()=binding.run {
        toolBar.setOnBackPressListener {
            finish()
        }
        tvCheckGoogle.clickDelay() {
            AuthManager.authGoogle(this@AuthActivity)
        }
        tvCheckFacebook.clickDelay {
            AuthManager.authFacebook(this@AuthActivity, { token ->
                viewModel.bindFacebook(token)
            }, { errorMsg ->
                errorMsg?.let {
                    showErrorPromptDialog(it) { }
                }
            })
        }
        clLiveChat.setServiceClick(supportFragmentManager)

    }


    private fun initObserve() {
        viewModel.userInfo.observe(this) {
            it?.let {
                binding.tvCheckGoogle.setChecked(it.googleBind)
                binding.tvCheckFacebook.setChecked(it.facebookBind)
            }
        }
        viewModel.bindGoogleResult.observe(this) {
            if (it.success) {
                viewModel.getUserInfo()
            } else {
                showErrorPromptDialog(it.msg) {}
            }
        }
        viewModel.bindFacebookResult.observe(this) {
            if (it.success) {
                viewModel.getUserInfo()
            } else {
                showErrorPromptDialog(it.msg) {}
            }
        }
    }


    fun TextView.setChecked(checked: Boolean) {
        if (checked) {
            text = getString(R.string.linked)
            setBackgroundResource(R.drawable.button_radius_8_bet_button)
            isEnabled = false
            setTextColor(getColor(R.color.color_FFFFFF))
        } else {
            text = getString(R.string.link)
            setBackgroundResource(R.drawable.bg_blue_radius_8_stroke_1)
            isEnabled = true
            setTextColor(getColor(R.color.color_025BE8))
        }
    }

    private fun setupServiceButton() {
        binding.tvCustomerService.setServiceClick(supportFragmentManager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        AuthManager.facebookCallback(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        AuthManager.googleCallback(requestCode, resultCode, data) { success, msg ->
            if (success) {
                if (msg.isEmptyStr()) {
                    hideLoading()
                } else {
                    viewModel.bindGoogle(msg!!)
                }
            } else {
                hideLoading()
            }
        }
    }
}