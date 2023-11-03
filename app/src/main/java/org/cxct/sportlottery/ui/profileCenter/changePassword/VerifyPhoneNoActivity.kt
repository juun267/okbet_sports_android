package org.cxct.sportlottery.ui.profileCenter.changePassword

import android.graphics.Typeface
import android.text.style.StyleSpan
import androidx.lifecycle.lifecycleScope
import com.drake.spannable.addSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.ColorSpan
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.databinding.ActivityVerifyPhonenoBinding
import org.cxct.sportlottery.network.index.login.LoginCodeRequest
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.login.VerifyCodeDialog
import org.cxct.sportlottery.util.CountDownUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.util.setTitleLetterSpacing
import org.cxct.sportlottery.view.checkSMSCode

class VerifyPhoneNoActivity: BindingActivity<SettingPasswordViewModel, ActivityVerifyPhonenoBinding>() {

    private val phoneNo by lazy { intent.getStringExtra("phone")!! }

    private var smsCode: String? = null // 短信或者邮箱验证码
    private var countDownGoing = false

    override fun onInitView() {
        setTitleStyle()
        setVerifyInfo()
    }

    private fun setTitleStyle() = binding.run {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        bindFinish(toolBar.btnToolbarBack)
        toolBar.tvToolbarTitle.setTitleLetterSpacing()
        toolBar.tvToolbarTitle.text = getString(R.string.withdraw_password)
    }

    private fun setVerifyInfo() = binding.run {
//        " ******${phoneNo.takeLast(4)}"
        tvTipsInfo.text = getString(R.string.P231)
            .setSpan(ColorSpan(getColor(R.color.color_0D2245)))
            .addSpan(" $phoneNo", listOf(StyleSpan(Typeface.BOLD), ColorSpan(getColor(R.color.color_F23C3B))))
        etSmsValidCode.setBottomLineLeftMargin(0)
        eetSmsCode.checkSMSCode(etSmsValidCode) {
            smsCode = it
            btnSendSms.setBtnEnable(smsCode != null && !countDownGoing)
        }
        btnSendSms.setOnClickListener {
            hideSoftKeyboard(this@VerifyPhoneNoActivity)
            VerifyCodeDialog().run {
                callBack = { identity, validCode ->
                    loading()
                    btnSendSms.setBtnEnable(false)
                    eetSmsCode.requestFocus()
                    viewModel.loginOrRegSendValidCode(LoginCodeRequest(phoneNo, identity, validCode))
                }
                show(supportFragmentManager, null)
            }
        }
    }

    private fun startCountDown() {
        countDownGoing = true
        CountDownUtil.smsCountDown(lifecycleScope, {
            binding.btnSendSms.setBtnEnable(false)
            countDownGoing = true
        }, {
            binding.btnSendSms.setBtnEnable(false)
            binding.btnSendSms.text = "${it}s"
        }, {
            binding.btnSendSms.setBtnEnable(smsCode != null)
            binding.btnSendSms.text = getString(R.string.send)
            countDownGoing = false
        })
    }

    private val initObserver = viewModel.run {
        msgCodeResult.observe(this@VerifyPhoneNoActivity) {
            hideLoading()
            if (it?.success == true) {
                startCountDown()
            } else {
                it?.msg?.let { msg -> showErrorPromptDialog(msg) {} }
            }
        }
    }
}