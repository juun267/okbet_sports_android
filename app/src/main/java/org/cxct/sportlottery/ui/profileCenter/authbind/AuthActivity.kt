package org.cxct.sportlottery.ui.profileCenter.authbind

import android.content.Intent
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.showErrorPromptDialog
import org.cxct.sportlottery.databinding.ActivityAuthBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.util.AuthManager
import org.cxct.sportlottery.util.setServiceClick
import org.cxct.sportlottery.util.setStartDrawable

/**
 * @app_destination 修改暱稱
 */
class AuthActivity : BindingActivity<AuthViewModel,ActivityAuthBinding>() {

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        binding.toolBar.tvToolbarTitle.text = getString(R.string.auth_login)
        initButton()
        setupServiceButton()
        initObserve()
        viewModel.getUserInfo()
    }

    private fun initButton()=binding.run {
        toolBar.btnToolbarBack.setOnClickListener {
            finish()
        }

        tvCheckGoogle.setOnClickListener {
            AuthManager.authGoogle(this@AuthActivity)
        }
        tvCheckFacebook.setOnClickListener {
            AuthManager.authFacebook(this@AuthActivity, { token ->
                viewModel.bindFacebook(token)
            }, { errorMsg ->
                errorMsg?.let {
                    showErrorPromptDialog(it) { }
                }
            })
        }
    }


    private fun initObserve() {
        viewModel.userInfo.observe(this) {
            it?.let {
                binding.tvCheckGoogle.setChecked(it.googleBind)
                binding.tvCheckFacebook.setChecked(it.facebookBind)
            }
        }
        viewModel.bindGoogleResult.observe(this) {
            it.peekContent().let {
                if (it.success) {
                    viewModel.getUserInfo()
                } else {
                    showErrorPromptDialog(it.msg) {}
                }
            }
        }
        viewModel.bindFacebookResult.observe(this) {
            it.peekContent().let {
                if (it.success) {
                    viewModel.getUserInfo()
                } else {
                    showErrorPromptDialog(it.msg) {}
                }
            }
        }
    }


    fun TextView.setChecked(checked: Boolean) {
        if (checked) {
            text = getString(R.string.linked)
            setBackgroundResource(R.drawable.button_radius_8_bet_button)
            setStartDrawable(R.drawable.ic_checked_white)
            isEnabled = false
            setTextColor(getColor(R.color.color_FFFFFF))
        } else {
            text = getString(R.string.link)
            setBackgroundResource(R.drawable.bg_stroke_radius_8_gray)
            setStartDrawable(0)
            isEnabled = true
            setTextColor(getColor(R.color.color_535D76))
        }
    }

    private fun setupServiceButton() {
        binding.tvCustomerService.setServiceClick(supportFragmentManager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        AuthManager.facebookCallback(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        AuthManager.googleCallback(requestCode, resultCode, data) { success, msg ->
            msg?.let {
                if (success) {
                    viewModel.bindGoogle(it)
                } else {
                    showErrorPromptDialog(it) {}
                }
            }
        }
    }
}