package org.cxct.sportlottery.ui.login.signUp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
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
        et_recommend_code.visibility = if (sConfigData?.enableInviteCode == FLAG_OPEN) View.VISIBLE else View.GONE
        et_recommend_code.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupMemberAccount() {
        et_member_account.afterTextChanged {
            checkAccountExist(it)
        }
    }

    private fun setupLoginPassword() {
        et_login_password.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupConfirmPassword() {
        et_confirm_password.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupFullName() {
        et_full_name.visibility = if (sConfigData?.enableFullName == FLAG_OPEN) View.VISIBLE else View.GONE
        et_full_name.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupWithdrawalPassword() {
        et_withdrawal_pwd.visibility = if (sConfigData?.enableFundPwd == FLAG_OPEN) View.VISIBLE else View.GONE
        et_withdrawal_pwd.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupQQ() {
        et_qq.visibility = if (sConfigData?.enableQQ == FLAG_OPEN) View.VISIBLE else View.GONE
        et_qq.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupPhone() {
        et_phone.visibility = if (sConfigData?.enablePhone == FLAG_OPEN) View.VISIBLE else View.GONE
        et_phone.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupMail() {
        et_mail.visibility = if (sConfigData?.enableEmail == FLAG_OPEN) View.VISIBLE else View.GONE
        et_mail.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupWeChat() {
        et_we_chat.visibility = if (sConfigData?.enableWechat == FLAG_OPEN) View.VISIBLE else View.GONE
        et_we_chat.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupZalo() {
        et_zalo.visibility = if (sConfigData?.enableZalo == FLAG_OPEN) View.VISIBLE else View.GONE
        et_zalo.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupFacebook() {
        et_facebook.visibility = if (sConfigData?.enableFacebook == FLAG_OPEN) View.VISIBLE else View.GONE
        et_facebook.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupWhatsApp() {
        et_whats_app.visibility = if (sConfigData?.enableWhatsApp == FLAG_OPEN) View.VISIBLE else View.GONE
        et_whats_app.afterTextChanged {
            checkInputData()
        }
    }


    private fun setupTelegram() {
        et_telegram.visibility = if (sConfigData?.enableTelegram == FLAG_OPEN) View.VISIBLE else View.GONE
        et_telegram.afterTextChanged {
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

        et_verification_code.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupSmsValidCode() {
        block_sms_valid_code.visibility = if (sConfigData?.enableSmsValidCode == FLAG_OPEN) View.VISIBLE else View.GONE
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN) {
            //手機驗證碼開啟，必定需要手機號欄位輸入
            et_phone.visibility = View.VISIBLE
            block_sms_valid_code.visibility = View.VISIBLE
        } else{
            block_sms_valid_code.visibility = View.GONE
        }

        btn_send_sms.setOnClickListener {
            sendSms()
        }

        et_sms_valid_code.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupAgreementButton() {
        btn_agreement.setOnClickListener {
            val dialog = CustomAlertDialog(this)
            dialog.setTitle(getString(R.string.btn_agreement))
            val message = viewModel.getAgreementContent(this)
            dialog.setMessage(message)
            dialog.setNegativeButtonText(null)
            dialog.setPositiveButtonText(getString(R.string.btn_confirm))
            dialog.setPositiveClickListener(View.OnClickListener {
                dialog.dismiss()
            })
            dialog.show()
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
            showErrorDialog(getString(R.string.hint_phone_number))
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
            deviceSn = deviceSn
        ).apply {
            if (sConfigData?.enableInviteCode == FLAG_OPEN)
                this.inviteCode = inviteCode
            if (sConfigData?.enableFullName == FLAG_OPEN)
                this.fullName = fullName
            if (sConfigData?.enableFundPwd == FLAG_OPEN)
                this.fundPwd = fundPwd
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
    }

    private fun initObserve() {
        viewModel.registerFormState.observe(this, Observer {
            val registerState = it ?: return@Observer
            et_recommend_code.setError(registerState.inviteCodeError)
            et_member_account.setError(registerState.memberAccountError)
            et_login_password.setError(registerState.loginPasswordError)
            et_confirm_password.setError(registerState.confirmPasswordError)
            et_full_name.setError(registerState.fullNameError)
            et_withdrawal_pwd.setError(registerState.fundPwdError)
            et_qq.setError(registerState.qqError)
            et_phone.setError(registerState.phoneError)
            et_mail.setError(registerState.emailError)
            et_we_chat.setError(registerState.weChatError)
            et_zalo.setError(registerState.zaloError)
            et_facebook.setError(registerState.facebookError)
            et_whats_app.setError(registerState.whatsAppError)
            et_telegram.setError(registerState.telegramError)
            et_sms_valid_code.setError(registerState.securityCodeError)
            et_verification_code.setError(registerState.validCodeError)
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
            ToastUtil.showToastInCenter(this@RegisterActivity, smsResult?.msg)
        }
    }

    private fun updateUiWithResult(checkAccountResult: CheckAccountResult?) {
        mIsExistAccount = checkAccountResult?.isExist?: false //若回傳資料為 null 也當作帳號未註冊
        checkInputData()
    }

    //發送簡訊後，倒數五分鐘
    private fun showSmeTimer300() {
        try {
            stopSmeTimer()
            btn_send_sms.visibility = View.GONE

            var sec = 300
            mSmsTimer = Timer()
            mSmsTimer?.schedule(object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        if (sec-- > 0) {
                            tv_timer.text = getString(R.string.send_timer, sec)
                        } else {
                            stopSmeTimer()
                            btn_send_sms.visibility = View.VISIBLE
                            tv_timer.text = null
                        }
                    }
                }
            }, 0, 1000) //在 0 秒後，每隔 1000L 毫秒執行一次
        } catch (e: Exception) {
            e.printStackTrace()

            stopSmeTimer()
            btn_send_sms.visibility = View.VISIBLE
            tv_timer.text = null
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
