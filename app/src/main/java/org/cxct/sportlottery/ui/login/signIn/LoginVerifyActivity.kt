package org.cxct.sportlottery.ui.login.signIn

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.RegisterInfoEvent
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.common.extentions.showErrorPromptDialog
import org.cxct.sportlottery.databinding.ActivityLoginVerifyBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.login.LoginCodeRequest
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.signUp.info.RegisterInfoActivity
import org.cxct.sportlottery.util.CountDownUtil
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.util.showCaptchaDialog
import org.cxct.sportlottery.view.checkRegisterListener

class LoginVerifyActivity: BaseActivity<LoginViewModel, ActivityLoginVerifyBinding>() {

    companion object {

        fun startLoginVerify(activity: Activity, phone: String) {
            val intent = Intent(activity, LoginVerifyActivity::class.java)
            intent.putExtra("phone", phone)
            activity.startActivity(intent)
        }
    }

    private val phone by lazy { intent.getStringExtra("phone")!! }
    private var countDownGoing = false

    override fun onInitView() = binding.run {
        ImmersionBar.with(this@LoginVerifyActivity)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(vTop.vStatusbar)
            .fitsSystemWindows(false)
            .init()
        llInput.background = DrawableCreatorUtils.getCommonBackgroundStyle(15, R.color.white, R.color.color_DDE8FF)
        binding.bottomLiences.tvLicense.text = Constants.copyRightString

        initEvent()
        initObserver()
        startCountDown()
    }

    private fun initEvent() = binding.run {
        btnLogin.setBtnEnable(false)
        edtCode.checkRegisterListener(::onInputCode)
        btnBack.setOnClickListener { finish() }
        btnLogin.setOnClickListener { viewModel.loginOrReg(phone, edtCode.text.toString(), "") }
        btnSend.setOnClickListener {
            showCaptchaDialog(supportFragmentManager)
                { identity, validCode ->
                    loading()
                    viewModel.loginOrRegSendValidCode(LoginCodeRequest(phone!!).apply { buildParams(identity, validCode) })
                }
        }

    }

    private fun initObserver() = viewModel.run {
        isLoading.observe(this@LoginVerifyActivity) {
            if (it) {
                loading()
            } else {
                hideLoading()
            }
        }

        //跳转至完善注册信息
        registerInfoEvent.observe(this@LoginVerifyActivity) {
            val intent = Intent(this@LoginVerifyActivity, RegisterInfoActivity::class.java)
            intent.putExtra("data", it)
            startActivity(intent)
            finish()
        }

        loginResult.observe(this@LoginVerifyActivity) {
            EventBusUtil.post(RegisterInfoEvent(it))
            finish()
        }

        msgCodeResult.observe(this@LoginVerifyActivity) {
            hideLoading()
            if (it?.success == true) {
                startCountDown()
            } else {
                it?.msg?.let { msg -> showErrorPromptDialog(msg) {} }
            }
        }
    }

    private fun onInputCode(code: String) {
        binding.btnLogin.setBtnEnable(code.length == 4)
    }

    private fun startCountDown() {
        if (countDownGoing) {
            return
        }
        countDownGoing = true
        CountDownUtil.smsCountDown(lifecycleScope, {
            countDownGoing = true
            binding.btnSend.setBtnEnable(false)
        }, {
            binding.btnSend.text = "${it}s"
        }, {
            countDownGoing = false
            binding.btnSend.setBtnEnable(true)
            binding.btnSend.setText(R.string.send)
        })
    }
}