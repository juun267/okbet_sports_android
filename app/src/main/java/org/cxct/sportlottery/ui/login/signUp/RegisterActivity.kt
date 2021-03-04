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
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.checkAccount.CheckAccountResult
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.register.RegisterRequest
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.LOGIN_SRC
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.util.BitmapUtil
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.ToastUtil
import timber.log.Timber
import java.util.*

class RegisterActivity : BaseActivity<RegisterViewModel>(RegisterViewModel::class) {

    private var mSmsTimer: Timer? = null
    private var mIsExistAccount = false //判斷是帳號是否註冊過

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

    private fun checkInputData(): Boolean {
        return viewModel.checkInputData(
            context = this,
            inviteCode = et_recommend_code.getText(),
            memberAccount = et_member_account.getText(),
            isExistAccount = mIsExistAccount,
            loginPassword = et_login_password.getText(),
            confirmPassword = et_confirm_password.getText(),
            fullName = et_full_name.getText(),
            fundPwd = et_withdrawal_pwd.getText(),
            qq = et_qq.getText(),
            phone = et_phone.getText(),
            email = et_mail.getText(),
            weChat = et_we_chat.getText(),
            zalo = et_zalo.getText(),
            facebook = et_facebook.getText(),
            whatsApp = et_whats_app.getText(),
            telegram = et_telegram.getText(),
            validCode = et_verification_code.getText(),
            securityCode = et_sms_valid_code.getText(),
            checkAgreement = cb_agreement.isChecked
        )
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener { finish() }
    }

    private fun setupRecommendCode() {
        et_recommend_code.setNecessarySymbolVisibility(if (sConfigData?.enableInviteCode == FLAG_OPEN) View.VISIBLE else View.INVISIBLE)
        et_recommend_code.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupMemberAccount() {
        et_member_account.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkAccountExist(et_member_account.getText())
        }
    }

    private fun setupLoginPassword() {
        et_login_password.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupConfirmPassword() {
        et_confirm_password.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupFullName() {
        et_full_name.visibility = if (sConfigData?.enableFullName == FLAG_OPEN) View.VISIBLE else View.GONE
        et_full_name.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupWithdrawalPassword() {
        et_withdrawal_pwd.visibility = if (sConfigData?.enableFundPwd == FLAG_OPEN) View.VISIBLE else View.GONE
        et_withdrawal_pwd.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupQQ() {
        et_qq.visibility = if (sConfigData?.enableQQ == FLAG_OPEN) View.VISIBLE else View.GONE
        et_qq.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupPhone() {
        et_phone.visibility = if (sConfigData?.enablePhone == FLAG_OPEN) View.VISIBLE else View.GONE
        et_phone.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupMail() {
        et_mail.visibility = if (sConfigData?.enableEmail == FLAG_OPEN) View.VISIBLE else View.GONE
        et_mail.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupWeChat() {
        et_we_chat.visibility = if (sConfigData?.enableWechat == FLAG_OPEN) View.VISIBLE else View.GONE
        et_we_chat.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupZalo() {
        et_zalo.visibility = if (sConfigData?.enableZalo == FLAG_OPEN) View.VISIBLE else View.GONE
        et_zalo.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupFacebook() {
        et_facebook.visibility = if (sConfigData?.enableFacebook == FLAG_OPEN) View.VISIBLE else View.GONE
        et_facebook.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupWhatsApp() {
        et_whats_app.visibility = if (sConfigData?.enableWhatsApp == FLAG_OPEN) View.VISIBLE else View.GONE
        et_whats_app.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }


    private fun setupTelegram() {
        et_telegram.visibility = if (sConfigData?.enableTelegram == FLAG_OPEN) View.VISIBLE else View.GONE
        et_telegram.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupValidCode() {
        if (sConfigData?.enableRegValidCode == FLAG_OPEN) {
            et_verification_code.visibility = View.VISIBLE
            updateValidCode()
        } else {
            et_verification_code.visibility = View.GONE
        }

        et_verification_code.setVerificationCodeBtnOnClickListener(View.OnClickListener {
            updateValidCode()
        })

        et_verification_code.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupSmsValidCode() {
        block_sms_valid_code.visibility = if (sConfigData?.enableSmsValidCode == FLAG_OPEN) View.VISIBLE else View.GONE
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN) {
            //手機驗證碼開啟，必定需要手機號欄位輸入
            et_phone.visibility = View.VISIBLE
            block_sms_valid_code.visibility = View.VISIBLE
        } else {
            block_sms_valid_code.visibility = View.GONE
        }

        btn_send_sms.setOnClickListener {
            sendSms()
        }

        et_sms_valid_code.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupAgreementButton() {
        cb_agreement.setOnClickListener {
            checkInputData()
        }
    }

    private fun setupRegisterAgreementButton() {
        btn_agreement.setOnClickListener {
            AgreementDialog().show(supportFragmentManager, null)
        }
    }

    private fun setupRegisterButton() {
        btn_register.setOnClickListener {
            if (checkInputData()) {
                register()
            }
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
        val data = viewModel.validCodeResult.value?.validCodeData
        viewModel.getValidCode(data?.identity)
        et_verification_code.setText(null)
    }

    private fun checkAccountExist(account: String) {
        mIsExistAccount = false
        et_member_account.setError(getString(R.string.desc_register_checking_account))
        viewModel.checkAccountExist(account)
    }

    private fun register() {
        loading()

        val inviteCode = et_recommend_code.getText()
        val userName = et_member_account.getText()
        val loginPassword = et_login_password.getText()
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
        val validCodeIdentity = viewModel.validCodeResult.value?.validCodeData?.identity
        val validCode = et_verification_code.getText()
        val deviceSn = JPushInterface.getRegistrationID(applicationContext) //極光推播
        Timber.d("極光推播: RegistrationID = $deviceSn")

        val registerRequest = RegisterRequest(
            userName = userName,
            password = MD5Util.MD5Encode(loginPassword),
            loginSrc = LOGIN_SRC,
            deviceSn = deviceSn,
            inviteCode = inviteCode
        ).apply {
            if (sConfigData?.enableFullName == FLAG_OPEN)
                this.fullName = fullName
            if (sConfigData?.enableFundPwd == FLAG_OPEN)
                this.fundPwd = MD5Util.MD5Encode(fundPwd)
            if (sConfigData?.enableQQ == FLAG_OPEN)
                this.qq = qq
            if (sConfigData?.enablePhone == FLAG_OPEN)
                this.phone = phone
            if (sConfigData?.enableEmail == FLAG_OPEN)
                this.email = email
            if (sConfigData?.enableWechat == FLAG_OPEN)
                this.wechat = weChat
            if (sConfigData?.enableZalo == FLAG_OPEN)
                this.zalo = zalo
            if (sConfigData?.enableFacebook == FLAG_OPEN)
                this.facebook = facebook
            if (sConfigData?.whatsApp == FLAG_OPEN)
                this.whatsapp = whatsApp
            if (sConfigData?.enableTelegram == FLAG_OPEN)
                this.telegram = telegram
            if (sConfigData?.enableSmsValidCode == FLAG_OPEN)
                this.securityCode = smsCode
            if (sConfigData?.enableRegValidCode == FLAG_OPEN) {
                this.validCodeIdentity = validCodeIdentity
                this.validCode = validCode
            }
        }
        viewModel.register(registerRequest)
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
        viewModel.registerFormState.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.registerResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.validCodeResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.smsResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.checkAccountResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.loginForGuestResult.observe(this) {
            updateUiWithResult(it)
        }
    }

    private fun updateUiWithResult(state: RegisterFormState) {
        et_recommend_code.setError(state.inviteCodeError)
        et_member_account.setError(state.memberAccountError)
        et_login_password.setError(state.loginPasswordError)
        et_confirm_password.setError(state.confirmPasswordError)
        et_full_name.setError(state.fullNameError)
        et_withdrawal_pwd.setError(state.fundPwdError)
        et_qq.setError(state.qqError)
        et_phone.setError(state.phoneError)
        et_mail.setError(state.emailError)
        et_we_chat.setError(state.weChatError)
        et_zalo.setError(state.zaloError)
        et_facebook.setError(state.facebookError)
        et_whats_app.setError(state.whatsAppError)
        et_telegram.setError(state.telegramError)
        et_sms_valid_code.setError(state.securityCodeError)
        et_verification_code.setError(state.validCodeError)
        btn_register.isEnabled = state.isDataValid

        if (state.checkAgreement) {
            cb_agreement.setTextColor(ContextCompat.getColor(this, R.color.gray4))
            cb_agreement.buttonTintList = null
        } else {
            cb_agreement.setTextColor(ContextCompat.getColor(this, R.color.orangeRed))
            cb_agreement.buttonTintList = ContextCompat.getColorStateList(this, R.color.orangeRed)
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

    private fun updateUiWithResult(checkAccountResult: CheckAccountResult?) {
        mIsExistAccount = checkAccountResult?.isExist ?: false //若回傳資料為 null 也當作帳號未註冊
        checkInputData()
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
