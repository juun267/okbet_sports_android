package org.cxct.sportlottery.ui.profileCenter.changePassword

import android.graphics.Typeface
import android.text.style.StyleSpan
import androidx.lifecycle.lifecycleScope
import com.drake.spannable.addSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.ColorSpan
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.common.extentions.finishWithOK
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityVerifyPhonenoBinding
import org.cxct.sportlottery.network.index.login.LoginCodeRequest
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.login.VerifyCodeDialog
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.checkSMSCode
import org.cxct.sportlottery.view.checkWithdrawPassword

class ResetWithdrawActivity: BindingActivity<LoginViewModel, ActivityVerifyPhonenoBinding>() {

    private val phoneNo by lazy { intent.getStringExtra("phone")!! }

    private var smsCode: String? = null // 短信或者邮箱验证码
    private var countDownGoing = false

    override fun onInitView() {
        setTitleStyle()
        setVerifyInfo()
        initObserver()
    }

    private fun setTitleStyle() = binding.run {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        bindFinish(toolBar.btnToolbarBack)
        toolBar.tvToolbarTitle.setTitleLetterSpacing()
        toolBar.tvToolbarTitle.text = getString(R.string.withdraw_password)
    }

    private fun setVerifyInfo() = binding.run {
        tvTipsInfo.text = getString(R.string.P231)
            .setSpan(ColorSpan(getColor(R.color.color_0D2245)))
            .addSpan(" ******${phoneNo.takeLast(4)}", listOf(StyleSpan(Typeface.BOLD), ColorSpan(getColor(R.color.color_F23C3B))))
        etSmsValidCode.setBottomLineLeftMargin(0)
        etSmsValidCode.bottomPart.setPadding(0, 0, 0, 0)
        btnConfirm.setBtnEnable(false)
        eetSmsCode.checkSMSCode(etSmsValidCode) {
            smsCode = it
            btnConfirm.setBtnEnable(it != null)
        }
        btnSendSms.setOnClickListener {
            hideSoftKeyboard(this@ResetWithdrawActivity)
            VerifyCodeDialog().run {
                callBack = { identity, validCode ->
                    loading()
                    eetSmsCode.requestFocus()
                    viewModel.loginOrRegSendValidCode(LoginCodeRequest(phoneNo, identity, validCode))
                }
                show(supportFragmentManager, null)
            }
        }

        btnConfirm.setOnClickListener {
            loading()
            viewModel.verifySMSCode(phoneNo, "$smsCode")
        }
    }

    private fun setPwdInput() = binding.run {
        btnSendSms.setBtnEnable(false)
        tvTipsInfo.gone()
        blockSmsValidCode.gone()
        etNewPassword.visible()
        etConfirmPassword.visible()
        btnConfirm.setBtnEnable(false)
        btnConfirm.setText(R.string.chat_confirm)
        eetNewPassword.checkWithdrawPassword(etNewPassword) {
            btnConfirm.setBtnEnable(it != null && it == eetConfirmPassword.text.toString())
        }
        eetConfirmPassword.checkWithdrawPassword(etConfirmPassword, eetNewPassword) {
            btnConfirm.setBtnEnable(it != null && it == eetNewPassword.text.toString())
        }
        btnConfirm.setOnClickListener {
            loading()
            viewModel.resetWithdraw(eetConfirmPassword.text.toString())
        }
        etNewPassword.setTransformationMethodEvent(eetNewPassword)
        etConfirmPassword.setTransformationMethodEvent(eetConfirmPassword)
    }

    private fun startCountDown() {
        if (countDownGoing) {
            return
        }
        countDownGoing = true
        CountDownUtil.smsCountDown(lifecycleScope, {
            binding.btnSendSms.setBtnEnable(false)
            countDownGoing = true
        }, {
            binding.btnSendSms.text = "${it}s"
        }, {
            binding.btnSendSms.setBtnEnable(true)
            binding.btnSendSms.text = getString(R.string.send)
            countDownGoing = false
        })
    }

    private fun initObserver(){
        viewModel.msgCodeResult.observe(this) { onResult(it?.success, it?.msg) { startCountDown() } }
        viewModel.smsCodeVerify.observe(this) { onResult(it.succeeded(), it.msg) { setPwdInput() } }
        viewModel.resetWithdraw.observe(this) {
            onResult(it.succeeded(), it.msg) {
                showPromptDialog(message = getString(R.string.update_withdrawal_pwd)) { finishWithOK() }
            }
        }
    }

    private inline fun onResult(success: Boolean?, msg: String?, onSucceed: Runnable) {
        hideLoading()
        if (success == true) {
            onSucceed.run()
        } else {
            showErrorPromptDialog(msg ?: getString(R.string.unknown_error)) {}
        }
    }
}