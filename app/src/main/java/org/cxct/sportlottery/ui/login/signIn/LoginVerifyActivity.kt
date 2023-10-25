package org.cxct.sportlottery.ui.login.signIn

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.RegisterInfoEvent
import org.cxct.sportlottery.databinding.ActivityLoginVerifyBinding
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.login.signUp.info.RegisterInfoActivity
import org.cxct.sportlottery.util.CountDownUtil
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.view.checkRegisterListener

class LoginVerifyActivity: BindingActivity<LoginViewModel, ActivityLoginVerifyBinding>() {

    companion object {

        fun startLoginVerify(activity: Activity, loginRequest: LoginRequest) {
            val intent = Intent(activity, LoginVerifyActivity::class.java)
            intent.putExtra("data", loginRequest)
            activity.startActivity(intent)
        }
    }

    private val loginRequest by lazy { intent.getParcelableExtra<LoginRequest>("data")!! }
    private var countDownGoing = false

    override fun onInitView() = binding.run {
        ImmersionBar.with(this@LoginVerifyActivity)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(vTop.vStatusbar)
            .fitsSystemWindows(false)
            .init()
        llInput.background = DrawableCreatorUtils.getCommonBackgroundStyle(15, R.color.white, R.color.color_DDE8FF)


        initEvent()
        initObserver()
        startCountDown()
    }

    private fun initEvent() = binding.run {
        btnLogin.setBtnEnable(false)
        edtCode.checkRegisterListener(::onInputCode)
        btnBack.setOnClickListener { finish() }
        btnSend.setOnClickListener {
            loginRequest.securityCode = null
            viewModel.checkUserNeedCode(loginRequest) {
                startCountDown()
            }
        }
        btnLogin.setOnClickListener {
            loginRequest.securityCode = edtCode.text.toString()
            loading()
            lifecycleScope.launch { viewModel.loginV3(loginRequest) }
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
            binding.btnSend.setBtnEnable(false)
            binding.btnSend.text = "${it}s"
        }, {
            countDownGoing = false
            binding.btnSend.setBtnEnable(true)
            binding.btnSend.setText(R.string.send)
        })
    }
}