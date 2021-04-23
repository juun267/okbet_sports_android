package org.cxct.sportlottery.ui.login.signUp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import cn.jpush.android.api.JPushInterface
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.toast_top_bet_result.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.util.BitmapUtil
import org.cxct.sportlottery.util.ToastUtil
import timber.log.Timber
import java.util.*

class RegisterActivity : BaseActivity<RegisterViewModel>(RegisterViewModel::class) {

    private var mSmsTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setupBackButton()
        setupRecommendCode()
        setupMemberAccount()
        setupLoginPassword()
        setupConfirmPassword()
        setupFullName()
        setupWithdrawalPassword()
        setupQQ()
        setupPhone()
        setupMail()
        setupWeChat()
        setupZalo()
        setupFacebook()
        setupWhatsApp()
        setupTelegram()
        setupValidCode()
        setupSmsValidCode()
        setupAgreementButton()
        setupRegisterAgreementButton()
        setupRegisterButton()
        setupGoToLoginButton()
        initObserve()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSmeTimer()
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener { finish() }
    }

    private fun setupEditTextFocusListener(editText: LoginEditText, doFun: (inputText: String) -> Unit) {
        editText.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                doFun.invoke(editText.getText())
        }
    }

    private fun setupRecommendCode() {
        et_recommend_code.apply {
            setNecessarySymbolVisibility(if (sConfigData?.enableInviteCode == FLAG_OPEN) View.VISIBLE else View.INVISIBLE)
            setupEditTextFocusListener(this) { viewModel.checkInviteCode(it) }
        }
    }

    private fun setupMemberAccount() {
        setupEditTextFocusListener(et_member_account) { viewModel.checkAccountExist(it) }
    }

    private fun setupLoginPassword() {
        setupEditTextFocusListener(et_login_password) { viewModel.checkLoginPassword(it) }
    }

    private fun setupConfirmPassword() {
        setupEditTextFocusListener(et_confirm_password) { viewModel.checkConfirmPassword(et_login_password.getText(), it) }
    }

    private fun setupFullName() {
        et_full_name.visibility = if (sConfigData?.enableFullName == FLAG_OPEN) {
            setupEditTextFocusListener(et_full_name) { viewModel.checkFullName(it) }
            View.VISIBLE
        } else View.GONE
    }

    private fun setupWithdrawalPassword() {
        et_withdrawal_pwd.visibility = if (sConfigData?.enableFundPwd == FLAG_OPEN) {
            setupEditTextFocusListener(et_withdrawal_pwd) { viewModel.checkFundPwd(it) }
            View.VISIBLE
        } else View.GONE
    }

    private fun setupQQ() {
        et_qq.visibility = if (sConfigData?.enableQQ == FLAG_OPEN) {
            setupEditTextFocusListener(et_qq) { viewModel.checkQQ(it) }
            View.VISIBLE
        } else View.GONE
    }

    private fun setupPhone() {
        et_phone.visibility = if (sConfigData?.enablePhone == FLAG_OPEN) {
            setupEditTextFocusListener(et_phone) { viewModel.checkPhone(it) }
            View.VISIBLE
        } else View.GONE
    }

    private fun setupMail() {
        et_mail.visibility = if (sConfigData?.enableEmail == FLAG_OPEN) {
            setupEditTextFocusListener(et_mail) { viewModel.checkEmail(it) }
            View.VISIBLE
        } else View.GONE
    }

    private fun setupWeChat() {
        et_we_chat.visibility = if (sConfigData?.enableWechat == FLAG_OPEN) {
            setupEditTextFocusListener(et_we_chat) { viewModel.checkWeChat(it) }
            View.VISIBLE
        } else View.GONE
    }

    private fun setupZalo() {
        et_zalo.visibility = if (sConfigData?.enableZalo == FLAG_OPEN) {
            setupEditTextFocusListener(et_zalo) { viewModel.checkZalo(it) }
            View.VISIBLE
        } else View.GONE
    }

    private fun setupFacebook() {
        et_facebook.visibility = if (sConfigData?.enableFacebook == FLAG_OPEN) {
            setupEditTextFocusListener(et_facebook) { viewModel.checkFacebook(it) }
            View.VISIBLE
        } else View.GONE
    }

    private fun setupWhatsApp() {
        et_whats_app.visibility = if (sConfigData?.enableWhatsApp == FLAG_OPEN) {
            setupEditTextFocusListener(et_whats_app) { viewModel.checkWhatsApp(it) }
            View.VISIBLE
        } else View.GONE
    }


    private fun setupTelegram() {
        et_telegram.visibility = if (sConfigData?.enableTelegram == FLAG_OPEN) {
            setupEditTextFocusListener(et_telegram) { viewModel.checkTelegram(it) }
            View.VISIBLE
        } else View.GONE
    }

    private fun setupValidCode() {
        if (sConfigData?.enableRegValidCode == FLAG_OPEN) {
            et_verification_code.visibility = View.VISIBLE
            updateValidCode()
            setupEditTextFocusListener(et_verification_code) { viewModel.checkValidCode(it) }
        } else {
            et_verification_code.visibility = View.GONE
        }

        et_verification_code.setVerificationCodeBtnOnClickListener(View.OnClickListener {
            updateValidCode()
        })
    }

    private fun setupSmsValidCode() {
        block_sms_valid_code.visibility = if (sConfigData?.enableSmsValidCode == FLAG_OPEN) View.VISIBLE else View.GONE
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN) {
            //手機驗證碼開啟，必定需要手機號欄位輸入
            et_phone.visibility = View.VISIBLE
            block_sms_valid_code.visibility = View.VISIBLE
            setupEditTextFocusListener(et_sms_valid_code) { viewModel.checkSecurityCode(it) }
        } else {
            block_sms_valid_code.visibility = View.GONE
        }

        btn_send_sms.setOnClickListener {
            sendSms()
        }
    }

    private fun setupAgreementButton() {
        cb_agreement.setOnClickListener {
            cb_agreement.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
            cb_agreement.buttonTintList = null
            viewModel.checkAgreement(cb_agreement.isChecked)
        }
    }

    private fun setupRegisterAgreementButton() {
        btn_agreement.setOnClickListener {
            AgreementDialog().show(supportFragmentManager, null)
        }
    }

    private fun setupRegisterButton() {
        btn_register.setOnClickListener {
            val inviteCode = et_recommend_code.getText()
            val userName = et_member_account.getText()
            val loginPassword = et_login_password.getText()
            val confirmPassword = et_confirm_password.getText()
            val fullName = et_full_name.getText()
            val fundPwd = et_withdrawal_pwd.getText()
            val qq = et_qq.getText()
            val phone = et_phone.getText()
            val email = et_mail.getText()
            val weChat = et_we_chat.getText()
            val zalo = et_zalo.getText()
            val facebook = et_facebook.getText()
            val whatsApp = et_whats_app.getText()
            val telegram = et_telegram.getText()
            val smsCode = et_sms_valid_code.getText()
            val validCode = et_verification_code.getText()
            val agreementChecked = cb_agreement.isChecked
            val deviceSn = JPushInterface.getRegistrationID(applicationContext) //極光推播
            Timber.d("極光推播: RegistrationID = $deviceSn")

            viewModel.registerSubmit(
                inviteCode,
                userName,
                loginPassword,
                confirmPassword,
                fullName,
                fundPwd,
                qq,
                phone,
                email,
                weChat,
                zalo,
                facebook,
                whatsApp,
                telegram,
                smsCode,
                validCode,
                agreementChecked,
                deviceSn
            )
        }
    }

    private fun sendSms() {
        val phone = et_phone.getText()
        if (phone.isBlank())
            showErrorPromptDialog(getString(R.string.prompt), getString(R.string.hint_phone_number)) {}
        else {
            btn_send_sms.isEnabled = false
            viewModel.sendSms(phone)
        }
    }

    private fun updateValidCode() {
        //TODO review structure
        viewModel.getValidCode()
        et_verification_code.setText(null)
    }

    private fun setupGoToLoginButton() {
        btn_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btn_visit_first.setOnClickListener {
            viewModel.loginAsGuest()
        }
    }

    private fun initObserve() {

        viewModel.registerResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.validCodeResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.smsResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.loginForGuestResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.agreementChecked.observe(this, {
            if (it?.not() == true) {
                cb_agreement.setTextColor(ContextCompat.getColor(this, R.color.colorRedDark))
                cb_agreement.buttonTintList = ContextCompat.getColorStateList(this, R.color.colorRedDark)
            } else {
                cb_agreement.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
                cb_agreement.buttonTintList = null
            }
        })

        /**
         * 輸入欄位判斷後錯誤提示
         */
        viewModel.apply {
            inviteCodeMsg.observe(this@RegisterActivity, { et_recommend_code.setError(it) })
            memberAccountMsg.observe(this@RegisterActivity, { et_member_account.setError(it) })
            loginPasswordMsg.observe(this@RegisterActivity, { et_login_password.setError(it) })
            confirmPasswordMsg.observe(this@RegisterActivity, { et_confirm_password.setError(it) })
            fullNameMsg.observe(this@RegisterActivity, { et_full_name.setError(it) })
            fundPwdMsg.observe(this@RegisterActivity, { et_withdrawal_pwd.setError(it) })
            qqMsg.observe(this@RegisterActivity, { et_qq.setError(it) })
            phoneMsg.observe(this@RegisterActivity, { et_phone.setError(it) })
            emailMsg.observe(this@RegisterActivity, { et_mail.setError(it) })
            weChatMsg.observe(this@RegisterActivity, { et_we_chat.setError(it) })
            zaloMsg.observe(this@RegisterActivity, { et_zalo.setError(it) })
            facebookMsg.observe(this@RegisterActivity, { et_facebook.setError(it) })
            whatsAppMsg.observe(this@RegisterActivity, { et_whats_app.setError(it) })
            telegramMsg.observe(this@RegisterActivity, { et_telegram.setError(it) })
            securityCodeMsg.observe(this@RegisterActivity, { et_sms_valid_code.setError(it) })
            validCodeMsg.observe(this@RegisterActivity, { et_verification_code.setError(it) })
            registerEnable.observe(this@RegisterActivity, { it?.let { btn_register.isEnabled = it } })
        }

    }

    private fun updateUiWithResult(loginResult: LoginResult) {
        hideLoading()
        if (loginResult.success) {
            finish()
        } else {
            updateValidCode()
            showErrorDialog(loginResult.msg)
        }
    }

    private fun updateUiWithResult(validCodeResult: ValidCodeResult?) {
        if (validCodeResult?.success == true) {
            val bitmap = BitmapUtil.stringToBitmap(validCodeResult.validCodeData?.img)
            et_verification_code.setVerificationCode(bitmap)
        } else {
            updateValidCode()
            et_verification_code.setVerificationCode(null)
            ToastUtil.showToastInCenter(this@RegisterActivity, getString(R.string.get_valid_code_fail_point))
        }
    }

    private fun updateUiWithResult(smsResult: SmsResult?) {
        btn_send_sms.isEnabled = true
        if (smsResult?.success == true) {
            showSmeTimer300()
        } else {
            smsResult?.msg?.let { showErrorPromptDialog(getString(R.string.prompt), it) {} }
            showSmeTimer300()
        }
    }

    //發送簡訊後，倒數五分鐘
    private fun showSmeTimer300() {
        try {
            stopSmeTimer()

            var sec = 60
            mSmsTimer = Timer()
            mSmsTimer?.schedule(object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        if (sec-- > 0) {
                            btn_send_sms.isEnabled = false
                            btn_send_sms.text = getString(R.string.send_timer, sec)
                            btn_send_sms.setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.colorPrimaryDark))
                        } else {
                            stopSmeTimer()
                            btn_send_sms.isEnabled = true
                            btn_send_sms.text = getString(R.string.get_verification_code)
                            btn_send_sms.setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.white))
                        }
                    }
                }
            }, 0, 1000) //在 0 秒後，每隔 1000L 毫秒執行一次
        } catch (e: Exception) {
            e.printStackTrace()

            stopSmeTimer()
            btn_send_sms.isEnabled = true
            btn_send_sms.text = getString(R.string.get_verification_code)
        }
    }

    private fun stopSmeTimer() {
        if (mSmsTimer != null) {
            mSmsTimer?.cancel()
            mSmsTimer = null
        }
    }

    private fun showErrorDialog(errorMsg: String?) {
        val dialog = CustomAlertDialog(this)
        dialog.setMessage(errorMsg)
        dialog.setNegativeButtonText(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }
}
