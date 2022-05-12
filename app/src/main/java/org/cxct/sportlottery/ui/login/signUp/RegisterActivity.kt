package org.cxct.sportlottery.ui.login.signUp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import cn.jpush.android.api.JPushInterface
import com.bumptech.glide.Glide
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityRegisterBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.login.afterTextChanged
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.profile.AvatarSelectorDialog
import org.cxct.sportlottery.util.*
import java.util.*

class RegisterActivity : BaseActivity<RegisterViewModel>(RegisterViewModel::class),
    View.OnClickListener {

    private var mSmsTimer: Timer? = null
    private lateinit var binding: ActivityRegisterBinding

    override fun onClick(v: View?) {
        when (v) {
            binding.ivReturn -> {
                updateValidCode()
            }
            binding.tvDuty -> {
                JumpUtil.toInternalWeb(
                    this,
                    Constants.getDutyRuleUrl(this),
                    resources.getString(R.string.responsible)
                )

            }
            binding.tvPrivacy -> {
                JumpUtil.toInternalWeb(
                    this,
                    Constants.getPrivacyRuleUrl(this),
                    resources.getString(R.string.privacy_policy)
                )

            }
            binding.tvAgreement -> {
                JumpUtil.toInternalWeb(
                    this,
                    Constants.getAgreementRuleUrl(this),
                    resources.getString(R.string.terms_conditions)
                )
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        setupFullName()
        setupWithdrawalPassword()
        setupQQ()
        setupPhone()
        setupMail()
        setupAddress()
        setupWeChat()
        setupZalo()
        setupFacebook()
        setupWhatsApp()
        setupTelegram()
        setupSecurityPb()
        setupValidCode()
        setupSmsValidCode()
        setupAgreement()
        setupRegisterButton()
        setupGoToLoginButton()
        initObserve()

        binding.apply {
            etLoginPassword.endIconImageButton.setOnClickListener {
                if (etLoginPassword.endIconResourceId == R.drawable.ic_eye_open) {
                    eetLoginPassword.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    etLoginPassword.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etLoginPassword.setEndIcon(R.drawable.ic_eye_open)
                    eetLoginPassword.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                }
                eetLoginPassword.setSelection(eetLoginPassword.text.toString().length)
            }
            etConfirmPassword.endIconImageButton.setOnClickListener {
                if (etConfirmPassword.endIconResourceId == R.drawable.ic_eye_open) {
                    eetConfirmPassword.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    etConfirmPassword.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etConfirmPassword.setEndIcon(R.drawable.ic_eye_open)
                    eetConfirmPassword.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                }
                eetConfirmPassword.setSelection(eetConfirmPassword.text.toString().length)
            }
            etWithdrawalPwd.endIconImageButton.setOnClickListener {
                if (etWithdrawalPwd.endIconResourceId == R.drawable.ic_eye_open) {
                    eetWithdrawalPwd.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    etWithdrawalPwd.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etWithdrawalPwd.setEndIcon(R.drawable.ic_eye_open)
                    eetWithdrawalPwd.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                }
                eetWithdrawalPwd.setSelection(eetWithdrawalPwd.text.toString().length)
            }
        }

        binding.ivReturn.setOnClickListener(this)
        binding.tvDuty.setOnClickListener(this)
        binding.tvPrivacy.text =
            getString(R.string.register_privacy) + getString(R.string.register_privacy_policy) + getString(
                R.string.register_privacy_policy_promotions
            )
        binding.tvPrivacy.makeLinks(
            Pair(
                applicationContext.getString(R.string.register_privacy_policy),
                View.OnClickListener {
                    JumpUtil.toInternalWeb(
                        this,
                        Constants.getPrivacyRuleUrl(this),
                        resources.getString(R.string.privacy_policy)
                    )
                })
        )
        binding.tvAgreement.text =
            getString(R.string.register_over_21) + getString(R.string.app_name) + getString(R.string.register_rules)
        binding.tvAgreement.makeLinks(
            Pair(applicationContext.getString(R.string.register_rules), View.OnClickListener {
                JumpUtil.toInternalWeb(
                    this,
                    Constants.getAgreementRuleUrl(this),
                    resources.getString(R.string.terms_conditions)
                )
            })
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSmeTimer()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupFullName() {
        binding.etFullName.visibility =
            if (sConfigData?.enableFullName == FLAG_OPEN) View.VISIBLE else View.GONE
    }

    private fun setupWithdrawalPassword() {
        binding.etWithdrawalPwd.visibility =
            if (sConfigData?.enableFundPwd == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupQQ() {
        binding.etQq.visibility =
            if (sConfigData?.enableQQ == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupPhone() {
        binding.etPhone.visibility =
            if (sConfigData?.enablePhone == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupMail() {
        binding.etMail.visibility =
            if (sConfigData?.enableEmail == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupAddress() {
        (if (sConfigData?.enableAddress == FLAG_OPEN) View.VISIBLE else View.GONE).let { visible ->
            with(binding) {
                etPostal.visibility = visible
                etProvince.visibility = visible
                etCity.visibility = visible
                etAddress.visibility = visible
            }
        }
    }

    private fun setupWeChat() {
        binding.etWeChat.visibility =
            if (sConfigData?.enableWechat == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupZalo() {
        binding.etZalo.visibility =
            if (sConfigData?.enableZalo == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupFacebook() {
        binding.etFacebook.visibility =
            if (sConfigData?.enableFacebook == FLAG_OPEN) View.VISIBLE else View.GONE
    }

    private fun setupWhatsApp() {
        binding.etWhatsApp.visibility =
            if (sConfigData?.enableWhatsApp == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupTelegram() {
        binding.etTelegram.visibility =
            if (sConfigData?.enableTelegram == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupSecurityPb() {
        binding.etSecurityPb.visibility =
            if (sConfigData?.enableSafeQuestion == FLAG_OPEN) View.VISIBLE else View.GONE
    }

    private fun setupValidCode() {
        if (sConfigData?.enableRegValidCode == FLAG_OPEN) {
            binding.blockValidCode.visibility = View.VISIBLE
            updateValidCode()
        } else {
            binding.blockValidCode.visibility = View.GONE
        }
    }

    private fun setupSmsValidCode() {
        binding.blockSmsValidCode.visibility =
            if (sConfigData?.enableSmsValidCode == FLAG_OPEN) View.VISIBLE else View.GONE
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN) {
            //手機驗證碼開啟，必定需要手機號欄位輸入
            binding.etPhone.visibility = View.VISIBLE
            binding.blockSmsValidCode.visibility = View.VISIBLE
        } else {
            binding.blockSmsValidCode.visibility = View.GONE
        }

        binding.btnSendSms.setOnClickListener {
            sendSms()
        }
    }

    private fun setupAgreement() {
        binding.cbAgreement.setOnClickListener {
            viewModel.checkAgreement(binding.cbAgreement.isChecked)
        }
    }

    private fun btnRegisterEnable() {
        binding.apply {
            btnRegister.isEnabled = viewModel.checkAllInput(
                eetRecommendCode.text.toString(),
                eetMemberAccount.text.toString(),
                eetLoginPassword.text.toString(),
                eetConfirmPassword.text.toString(),
                eetFullName.text.toString(),
                eetWithdrawalPwd.text.toString(),
                eetQq.text.toString(),
                eetPhone.text.toString(),
                eetMail.text.toString(),
                eetPostal.text.toString(),
                eetProvince.text.toString(),
                eetCity.text.toString(),
                eetAddress.text.toString(),
                eetWeChat.text.toString(),
                eetZalo.text.toString(),
                eetFacebook.text.toString(),
                eetWhatsApp.text.toString(),
                eetTelegram.text.toString(),
                eetSecurityPb.text.toString(),
                eetSmsValidCode.text.toString(),
                eetVerificationCode.text.toString(),
                cbAgreement.isChecked
            )
            btnRegister.setTitleLetterSpacing()
        }
    }

    private fun EditText.setActionListener(isRegisterEnable: Boolean) {
        this.setOnEditorActionListener { _, actionId, _ ->
            if (actionId and EditorInfo.IME_MASK_ACTION != 0 && isRegisterEnable) {
                binding.btnRegister.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun setupRegisterButton() {
        binding.apply {
            eetRecommendCode.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetMemberAccount.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetConfirmPassword.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetFullName.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetWithdrawalPwd.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetQq.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetPhone.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetMail.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetPostal.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetProvince.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetCity.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetAddress.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetWeChat.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetZalo.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetFacebook.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetWhatsApp.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetTelegram.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetSecurityPb.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetSmsValidCode.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            eetVerificationCode.apply {
                this.afterTextChanged {
                    btnRegisterEnable()
                }
            }
            cbAgreement.setOnCheckedChangeListener { _, _ ->
                btnRegisterEnable()
            }
        }

        binding.btnRegister.setOnClickListener {
            Log.i(">>>", "btnRegister onclicked")
            val deviceId = Settings.Secure.getString(
                applicationContext.contentResolver, Settings.Secure.ANDROID_ID
            )
            val deviceSn = JPushInterface.getRegistrationID(applicationContext)
            binding.apply {
                var phone = eetPhone.text.toString()
                if (phone.isNotEmpty() && phone.substring(0, 1) == "0") {
                    phone = phone.substring(1, phone.length)
                }
                viewModel.registerSubmit(
                    eetRecommendCode.text.toString(),
                    eetMemberAccount.text.toString(),
                    eetLoginPassword.text.toString(),
                    eetConfirmPassword.text.toString(),
                    eetFullName.text.toString(),
                    eetWithdrawalPwd.text.toString(),
                    eetQq.text.toString(),
                    phone,
                    eetMail.text.toString(),
                    eetPostal.text.toString(),
                    eetProvince.text.toString(),
                    eetCity.text.toString(),
                    eetAddress.text.toString(),
                    eetWeChat.text.toString(),
                    eetZalo.text.toString(),
                    eetFacebook.text.toString(),
                    eetWhatsApp.text.toString(),
                    eetTelegram.text.toString(),
                    eetSecurityPb.text.toString(),
                    eetSmsValidCode.text.toString(),
                    eetVerificationCode.text.toString(),
                    cbAgreement.isChecked,
                    deviceSn,
                    deviceId
                )
            }
        }
    }

    private fun sendSms() {
        var phone = binding.eetPhone.text.toString()
        if (phone.isBlank())
            showErrorPromptDialog(
                getString(R.string.prompt),
                getString(R.string.hint_phone_number)
            ) {}
        else {
            binding.btnSendSms.isEnabled = false
            if(phone.substring(0,1) == "0"){
                phone = phone.substring(1,phone.length)
            }
            viewModel.sendSms(phone)
        }
    }

    private fun updateValidCode() {
        viewModel.getValidCode()
        binding.eetVerificationCode.setText("");
    }

    private fun setupGoToLoginButton() {
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnVisitFirst.setOnClickListener {
            viewModel.loginAsGuest()
        }
    }

    private fun initObserve() {

        setEditTextIme(binding.btnRegister.isEnabled)
        viewModel.registerEnable.observe(this) {
            Log.e(">>>", "registerEnable = $it")
            setEditTextIme(it)
        }

        viewModel.registerResult.observe(this) {
            updateUiWithResult(it)
        }

        viewModel.validCodeResult.observe(this) {
            updateUiWithResult(it)
        }

        viewModel.smsResult.observe(this) {
            updateUiWithResult(it)
        }

        viewModel.loginForGuestResult.observe(this) {
            updateUiWithResult(it)
        }

        /**
         * 輸入欄位判斷後錯誤提示
         */
        viewModel.apply {
            inviteCodeMsg.observe(this@RegisterActivity) {
                binding.etRecommendCode.setError(
                    it,
                    false
                )
            }
            memberAccountMsg.observe(this@RegisterActivity) {
                binding.etMemberAccount.setError(
                    it,
                    false
                )
            }
            loginPasswordMsg.observe(this@RegisterActivity) {
                binding.etLoginPassword.setError(
                    it,
                    false
                )
            }
            confirmPasswordMsg.observe(this@RegisterActivity) {
                binding.etConfirmPassword.setError(
                    it,
                    false
                )
            }
            fullNameMsg.observe(this@RegisterActivity) { binding.etFullName.setError(it, false) }
            fundPwdMsg.observe(this@RegisterActivity) {
                binding.etWithdrawalPwd.setError(
                    it,
                    false
                )
            }
            qqMsg.observe(this@RegisterActivity) { binding.etQq.setError(it, false) }
            phoneMsg.observe(this@RegisterActivity) { binding.etPhone.setError(it, false) }
            emailMsg.observe(this@RegisterActivity) { binding.etMail.setError(it, false) }
            postalMsg.observe(this@RegisterActivity) { binding.etPostal.setError(it, false) }
            provinceMsg.observe(this@RegisterActivity) { binding.etProvince.setError(it, false) }
            cityMsg.observe(this@RegisterActivity) { binding.etCity.setError(it, false) }
            addressMsg.observe(this@RegisterActivity) { binding.etAddress.setError(it, false) }
            weChatMsg.observe(this@RegisterActivity) { binding.etWeChat.setError(it, false) }
            zaloMsg.observe(this@RegisterActivity) { binding.etZalo.setError(it, false) }
            facebookMsg.observe(this@RegisterActivity) { binding.etFacebook.setError(it, false) }
            whatsAppMsg.observe(this@RegisterActivity) { binding.etWhatsApp.setError(it, false) }
            telegramMsg.observe(this@RegisterActivity) { binding.etTelegram.setError(it, false) }
            securityPbMsg.observe(this@RegisterActivity) { binding.etSecurityPb.setError(it, false) }
            securityCodeMsg.observe(this@RegisterActivity) {
                binding.etSmsValidCode.setError(
                    it,
                    false
                )
            }
            validCodeMsg.observe(this@RegisterActivity) {
                binding.etSmsValidCode.setError(
                    it,
                    false
                )
            }
        }

    }

    //當所有值都有填，按下enter時，自動點擊註冊鈕
    private fun setEditTextIme(registerEnable: Boolean) {
        binding.apply {
            eetRecommendCode.setActionListener(registerEnable)
            eetMemberAccount.setActionListener(registerEnable)
            eetLoginPassword.setActionListener(registerEnable)
            eetConfirmPassword.setActionListener(registerEnable)
            eetFullName.setActionListener(registerEnable)
            eetWithdrawalPwd.setActionListener(registerEnable)
            eetQq.setActionListener(registerEnable)
            eetPhone.setActionListener(registerEnable)
            eetMail.setActionListener(registerEnable)
            eetWeChat.setActionListener(registerEnable)
            eetZalo.setActionListener(registerEnable)
            eetFacebook.setActionListener(registerEnable)
            eetWhatsApp.setActionListener(registerEnable)
            eetTelegram.setActionListener(registerEnable)
            eetSecurityPb.setActionListener(registerEnable)
            eetSmsValidCode.setActionListener(registerEnable)
            eetVerificationCode.setActionListener(registerEnable)
        }
    }

    private fun updateUiWithResult(loginResult: LoginResult) {
        hideLoading()
        if (loginResult.success) {
            //finish()
            RegisterSuccessDialog(this).apply {
                setNegativeClickListener {
                    dismiss()
                    startActivity(Intent(this@RegisterActivity, MoneyRechargeActivity::class.java))
                    finish()
                }
            }.show(supportFragmentManager, null)

        } else {
            updateValidCode()
            showErrorDialog(loginResult.msg)
        }
    }

    private fun updateUiWithResult(validCodeResult: ValidCodeResult?) {
        if (validCodeResult?.success == true) {
            val bitmap = BitmapUtil.stringToBitmap(validCodeResult.validCodeData?.img)
            Glide.with(this)
                .load(bitmap)
                .into(binding.ivVerification)
        } else {
            updateValidCode()
            //et_verification_code.setVerificationCode(null)
            ToastUtil.showToastInCenter(
                this@RegisterActivity,
                getString(R.string.get_valid_code_fail_point)
            )
        }
    }

    private fun updateUiWithResult(smsResult: SmsResult?) {
        binding.btnSendSms.isEnabled = true
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
                            binding.btnSendSms.isEnabled = false
                            //btn_send_sms.text = getString(R.string.send_timer, sec)
                            binding.btnSendSms.text = "${sec}s"
                            binding.btnSendSms.setTextColor(
                                ContextCompat.getColor(
                                    this@RegisterActivity,
                                    R.color.color_E0E0E0_404040
                                )
                            )
                        } else {
                            stopSmeTimer()
                            binding.btnSendSms.isEnabled = true
                            binding.btnSendSms.text = getString(R.string.get_verification_code)
                            binding.btnSendSms.setTextColor(Color.WHITE)
                        }
                    }
                }
            }, 0, 1000) //在 0 秒後，每隔 1000L 毫秒執行一次
        } catch (e: Exception) {
            e.printStackTrace()

            stopSmeTimer()
            binding.btnSendSms.isEnabled = true
            binding.btnSendSms.text = getString(R.string.get_verification_code)
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
        dialog.show(supportFragmentManager, null)
    }


    fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
        val spannableString = SpannableString(this.text)
        var startIndexOfLink = -1
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.color = textPaint.linkColor
                    textPaint.isUnderlineText = false
                }

                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
            if (startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
            spannableString.setSpan(
                clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        this.movementMethod =
            LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }


}
