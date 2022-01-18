package org.cxct.sportlottery.ui.login.signUp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import cn.jpush.android.api.JPushInterface
import com.bumptech.glide.Glide
import org.cxct.sportlottery.MultiLanguagesApplication.Companion.UUID
import org.cxct.sportlottery.MultiLanguagesApplication.Companion.UUID_DEVICE_CODE
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
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.util.BitmapUtil
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.widget.boundsEditText.ExtendedEditText
import org.cxct.sportlottery.widget.boundsEditText.TextFieldBoxes
import timber.log.Timber
import java.util.*

class RegisterActivity : BaseActivity<RegisterViewModel>(RegisterViewModel::class) ,View.OnClickListener{

    private var mSmsTimer: Timer? = null
    private lateinit var binding: ActivityRegisterBinding

    override fun onClick(v: View?) {
        when (v) {
            binding.ivReturn -> {
               updateValidCode()
            }
            binding.tvDuty -> {
                JumpUtil.toExternalWeb(this, Constants.getDutyRuleUrl(this))
            }
            binding.tvPrivacy -> {
                JumpUtil.toExternalWeb(this, Constants.getPrivacyRuleUrl(this))
            }
            binding.tvAgreement -> {
                JumpUtil.toExternalWeb(this, Constants.getAgreementRuleUrl(this))
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        setupAddress()
        setupWeChat()
        setupZalo()
        setupFacebook()
        setupWhatsApp()
        setupTelegram()
        setupValidCode()
        setupSmsValidCode()
        setupAgreement()
        setupRegisterAgreementButton()
        setupRegisterButton()
        setupGoToLoginButton()
        initObserve()

        binding.apply {
            etLoginPassword.endIconImageButton.setOnClickListener {
                if (etLoginPassword.endIconResourceId == R.drawable.ic_eye_open) {
                    eetLoginPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    etLoginPassword.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etLoginPassword.setEndIcon(R.drawable.ic_eye_open)
                    eetLoginPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }
                eetLoginPassword.setSelection(eetLoginPassword.text.toString().length)
            }
            etConfirmPassword.endIconImageButton.setOnClickListener {
                if (etConfirmPassword.endIconResourceId == R.drawable.ic_eye_open) {
                    eetConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    etConfirmPassword.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etConfirmPassword.setEndIcon(R.drawable.ic_eye_open)
                    eetConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }
                eetConfirmPassword.setSelection(eetConfirmPassword.text.toString().length)
            }
            etWithdrawalPwd.endIconImageButton.setOnClickListener {
                if (etWithdrawalPwd.endIconResourceId == R.drawable.ic_eye_open) {
                    eetWithdrawalPwd.transformationMethod = PasswordTransformationMethod.getInstance()
                    etWithdrawalPwd.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etWithdrawalPwd.setEndIcon(R.drawable.ic_eye_open)
                    eetWithdrawalPwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }
                eetWithdrawalPwd.setSelection(eetWithdrawalPwd.text.toString().length)
            }
        }

        binding.ivReturn.setOnClickListener(this)
        binding.tvDuty.setOnClickListener(this)
        //binding.tvPrivacy.setOnClickListener(this)
        //binding.tvAgreement.setOnClickListener(this)
        binding.tvPrivacy.text = getString(R.string.register_privacy)+getString(R.string.register_privacy_policy)+getString(R.string.register_privacy_policy_promotions)
        binding.tvPrivacy.makeLinks(
            Pair(applicationContext.getString(R.string.register_privacy_policy), View.OnClickListener {
                JumpUtil.toExternalWeb(this, Constants.getPrivacyRuleUrl(this))
            }))
        binding.tvAgreement.text = getString(R.string.register_over_21)+getString(R.string.app_name)+getString(R.string.register_rules)
        binding.tvAgreement.makeLinks(
            Pair(applicationContext.getString(R.string.register_rules), View.OnClickListener {
                JumpUtil.toExternalWeb(this, Constants.getAgreementRuleUrl(this))
            }))
    }


    override fun onDestroy() {
        super.onDestroy()
        stopSmeTimer()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener { finish() }
    }

//    private fun setupEditTextFocusListener(
//        editText: ExtendedEditText,
//        doFun: (inputText: String) -> Unit
//    ) {
//        editText.setEditTextOnFocusChangeListener { _, hasFocus ->
//            if (!hasFocus)
//                doFun.invoke(editText.getText())
//        }
//    }


    private fun setupRecommendCode() {
//        et_recommend_code.apply {
//            setNecessarySymbolVisibility(if (sConfigData?.enableInviteCode == FLAG_OPEN) View.VISIBLE else View.INVISIBLE)
//            setupEditTextFocusListener(this) { viewModel.checkInviteCode(it) }
//        }

    }

    private fun setupMemberAccount() {
//        setupEditTextFocusListener(et_member_account) { viewModel.checkAccountExist(it) }
    }

    private fun setupLoginPassword() {

        //setupEditTextFocusListener(et_login_password) { viewModel.checkLoginPassword(it) }
    }

    private fun setupConfirmPassword() {
//        setupEditTextFocusListener(et_confirm_password) {
//            viewModel.checkConfirmPassword(
//                et_login_password.getText(),
//                it
//            )
//        }

        //20210427 紀錄：當確認密碼下方的 label 都被關時，要動態調整 MarginBottom 高度，為了符合排版要求
//        if (sConfigData?.enableFullName != FLAG_OPEN &&
//            sConfigData?.enableFundPwd != FLAG_OPEN &&
//            sConfigData?.enableQQ != FLAG_OPEN &&
//            sConfigData?.enablePhone != FLAG_OPEN &&
//            sConfigData?.enableEmail != FLAG_OPEN &&
//            sConfigData?.enableWechat != FLAG_OPEN &&
//            sConfigData?.enableZalo != FLAG_OPEN &&
//            sConfigData?.enableFacebook != FLAG_OPEN &&
//            sConfigData?.enableWhatsApp != FLAG_OPEN &&
//            sConfigData?.enableTelegram != FLAG_OPEN &&
//            sConfigData?.enableTelegram != FLAG_OPEN &&
//            sConfigData?.enableRegValidCode != FLAG_OPEN &&
//            sConfigData?.enableSmsValidCode != FLAG_OPEN
//        )
//            et_confirm_password.setMarginBottom(10.dp)
    }

    private fun setupFullName() {
//        et_full_name.visibility = if (sConfigData?.enableFullName == FLAG_OPEN) {
//            setupEditTextFocusListener(et_full_name) { viewModel.checkFullName(it) }
//            View.VISIBLE
//        } else View.GONE
        binding.etFullName.visibility = if (sConfigData?.enableFullName == FLAG_OPEN) View.VISIBLE else View.GONE
    }

    private fun setupWithdrawalPassword() {
//        et_withdrawal_pwd.visibility = if (sConfigData?.enableFundPwd == FLAG_OPEN) {
//            setupEditTextFocusListener(et_withdrawal_pwd) { viewModel.checkFundPwd(it) }
//            View.VISIBLE
//        } else View.GONE
        binding.etWithdrawalPwd.visibility = if (sConfigData?.enableFundPwd == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupQQ() {
//        et_qq.visibility = if (sConfigData?.enableQQ == FLAG_OPEN) {
//            setupEditTextFocusListener(et_qq) { viewModel.checkQQ(it) }
//            View.VISIBLE
//        } else View.GONE
        binding.etQq.visibility = if (sConfigData?.enableQQ == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupPhone() {
//        et_phone.visibility = if (sConfigData?.enablePhone == FLAG_OPEN) {
//            setupEditTextFocusListener(et_phone) { viewModel.checkPhone(it) }
//            View.VISIBLE
//        } else View.GONE
        binding.etPhone.visibility = if (sConfigData?.enablePhone == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupMail() {
//        et_mail.visibility = if (sConfigData?.enableEmail == FLAG_OPEN) {
//            setupEditTextFocusListener(et_mail) { viewModel.checkEmail(it) }
//            View.VISIBLE
//        } else View.GONE
        binding.etMail.visibility = if (sConfigData?.enableEmail == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupAddress() {
        binding.etAddress.visibility = if (sConfigData?.enableAddress == FLAG_OPEN) View.VISIBLE else View.GONE
    }

    private fun setupWeChat() {
//        et_we_chat.visibility = if (sConfigData?.enableWechat == FLAG_OPEN) {
//            setupEditTextFocusListener(et_we_chat) { viewModel.checkWeChat(it) }
//            View.VISIBLE
//        } else View.GONE
        binding.etWeChat.visibility = if (sConfigData?.enableWechat == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupZalo() {
//        et_zalo.visibility = if (sConfigData?.enableZalo == FLAG_OPEN) {
//            setupEditTextFocusListener(et_zalo) { viewModel.checkZalo(it) }
//            View.VISIBLE
//        } else View.GONE
        binding.etZalo.visibility = if (sConfigData?.enableZalo == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupFacebook() {
//        et_facebook.visibility = if (sConfigData?.enableFacebook == FLAG_OPEN) {
//            setupEditTextFocusListener(et_facebook) { viewModel.checkFacebook(it) }
//            View.VISIBLE
//        } else View.GONE
        binding.etFacebook.visibility = if (sConfigData?.enableFacebook == FLAG_OPEN) View.VISIBLE else View.GONE
    }

    private fun setupWhatsApp() {
//        et_whats_app.visibility = if (sConfigData?.enableWhatsApp == FLAG_OPEN) {
//            setupEditTextFocusListener(et_whats_app) { viewModel.checkWhatsApp(it) }
//            View.VISIBLE
//        } else View.GONE
        binding.etWhatsApp.visibility = if (sConfigData?.enableWhatsApp == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupTelegram() {
//        et_telegram.visibility = if (sConfigData?.enableTelegram == FLAG_OPEN) {
//            setupEditTextFocusListener(et_telegram) { viewModel.checkTelegram(it) }
//            View.VISIBLE
//        } else View.GONE
        binding.etTelegram.visibility = if (sConfigData?.enableTelegram == FLAG_OPEN) View.VISIBLE else View.GONE

    }

    private fun setupValidCode() {
        if (sConfigData?.enableRegValidCode == FLAG_OPEN) {
            binding.blockValidCode.visibility = View.VISIBLE
            updateValidCode()
            //setupEditTextFocusListener(et_verification_code) { viewModel.checkValidCode(it) }
        } else {
            binding.blockValidCode.visibility = View.GONE
        }

//        binding.etVerificationCode.setVerificationCodeBtnOnClickListener {
//            updateValidCode()
//        }
    }

    private fun setupSmsValidCode() {
        binding.blockSmsValidCode.visibility =
            if (sConfigData?.enableSmsValidCode == FLAG_OPEN) View.VISIBLE else View.GONE
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN) {
            //手機驗證碼開啟，必定需要手機號欄位輸入
            binding.etPhone.visibility = View.VISIBLE
            binding.blockSmsValidCode.visibility = View.VISIBLE
            //setupEditTextFocusListener(et_sms_valid_code) { viewModel.checkSecurityCode(it) }
        } else {
            binding.blockSmsValidCode.visibility = View.GONE
        }

        binding.btnSendSms.setOnClickListener {
            sendSms()
        }
    }

    private fun setupAgreement() {
//        when (LanguageManager.getSelectLanguage(this@RegisterActivity)) {
//            LanguageManager.Language.ZH -> binding.llAgreement.orientation = LinearLayout.HORIZONTAL
//            else -> binding.llAgreement.orientation = LinearLayout.VERTICAL
//        }
//
        binding.cbAgreement.setOnClickListener {
            binding.cbAgreement.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
            binding.cbAgreement.buttonTintList = null
            viewModel.checkAgreement(binding.cbAgreement.isChecked)
        }
    }

    private fun setupRegisterAgreementButton() {
//        binding.btnAgreement.setOnClickListener {
//            AgreementDialog().show(supportFragmentManager, null)
//        }

    }

    private fun setupRegisterButton() {
        binding.btnRegister.setOnClickListener {
            val inviteCode = binding.eetRecommendCode.text.toString()
            val userName = binding.eetMemberAccount.text.toString()
            val loginPassword = binding.eetLoginPassword.text.toString()
            val confirmPassword = binding.eetConfirmPassword.text.toString()
            val fullName = binding.eetFullName.text.toString()
            val fundPwd = binding.eetWithdrawalPwd.text.toString()
            val qq = binding.eetQq.text.toString()
            val phone = binding.eetPhone.text.toString()
            val email = binding.eetMail.text.toString()
            val address = binding.eetAddress.text.toString()
            val weChat = binding.eetWeChat.text.toString()
            val zalo = binding.eetZalo.text.toString()
            val facebook = binding.eetFacebook.text.toString()
            val whatsApp = binding.eetWhatsApp.text.toString()
            val telegram = binding.eetTelegram.text.toString()
            val smsCode = binding.eetSmsValidCode.text.toString()
            val validCode = binding.eetVerificationCode.text.toString()
            val agreementChecked = binding.cbAgreement.isChecked
            val deviceSn = JPushInterface.getRegistrationID(applicationContext)
//            val deviceSn = getSharedPreferences(UUID_DEVICE_CODE, Context.MODE_PRIVATE).getString(UUID, "") ?: ""
//            Timber.d("UUID = $deviceSn")

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
                address,
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
        val phone = binding.eetPhone.text.toString()
        if (phone.isBlank())
            showErrorPromptDialog(
                getString(R.string.prompt),
                getString(R.string.hint_phone_number)
            ) {}
        else {
            binding.btnSendSms.isEnabled = false
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

        viewModel.registerResult.observe(this, {
            updateUiWithResult(it)
        })

        viewModel.validCodeResult.observe(this, {
            updateUiWithResult(it)
        })

        viewModel.smsResult.observe(this, {
            updateUiWithResult(it)
        })

        viewModel.loginForGuestResult.observe(this, {
            updateUiWithResult(it)
        })

        viewModel.agreementChecked.observe(this, {
            if (it?.not() == true) {
                binding.cbAgreement.setTextColor(ContextCompat.getColor(this, R.color.colorRedDark))
                binding.cbAgreement.buttonTintList =
                    ContextCompat.getColorStateList(this, R.color.colorRedDark)
            } else {
                binding.cbAgreement.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
                binding.cbAgreement.buttonTintList = null
            }
        })

        /**
         * 輸入欄位判斷後錯誤提示
         */
        viewModel.apply {
            inviteCodeMsg.observe(this@RegisterActivity, { binding.etRecommendCode.setError(it,false) })
            memberAccountMsg.observe(this@RegisterActivity, { binding.etMemberAccount.setError(it,false) })
            loginPasswordMsg.observe(this@RegisterActivity, { binding.etLoginPassword.setError(it,false) })
            confirmPasswordMsg.observe(this@RegisterActivity, { binding.etConfirmPassword.setError(it,false) })
            fullNameMsg.observe(this@RegisterActivity, { binding.etFullName.setError(it,false) })
            fundPwdMsg.observe(this@RegisterActivity, { binding.etWithdrawalPwd.setError(it,false) })
            qqMsg.observe(this@RegisterActivity, { binding.etQq.setError(it,false) })
            phoneMsg.observe(this@RegisterActivity, { binding.etPhone.setError(it,false) })
            emailMsg.observe(this@RegisterActivity, { binding.etMail.setError(it,false) })
            addressMsg.observe(this@RegisterActivity, { binding.etAddress.setError(it,false) })
            weChatMsg.observe(this@RegisterActivity, { binding.etWeChat.setError(it,false) })
            zaloMsg.observe(this@RegisterActivity, {binding.etZalo.setError(it,false) })
            facebookMsg.observe(this@RegisterActivity, { binding.etFacebook.setError(it,false) })
            whatsAppMsg.observe(this@RegisterActivity, { binding.etWhatsApp.setError(it,false) })
            telegramMsg.observe(this@RegisterActivity, { binding.etTelegram.setError(it,false) })
            securityCodeMsg.observe(this@RegisterActivity, { binding.etSmsValidCode.setError(it,false) })
            validCodeMsg.observe(this@RegisterActivity, { binding.etSmsValidCode.setError(it,false) })
//            registerEnable.observe(
//                this@RegisterActivity,
//                { it?.let { binding.btnRegister.isEnabled = it } })
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
                                    R.color.colorGrayDark
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
        dialog.show()
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
            if(startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
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
