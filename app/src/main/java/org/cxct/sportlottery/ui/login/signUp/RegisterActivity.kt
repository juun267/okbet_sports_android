package org.cxct.sportlottery.ui.login.signUp

import android.annotation.SuppressLint
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
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import cn.jpush.android.api.JPushInterface
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
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
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.login.checkRegisterListener
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.util.*
import java.util.*

/**
 * @app_destination 註冊
 */
class RegisterActivity : BaseActivity<RegisterViewModel>(RegisterViewModel::class),
    View.OnClickListener {

    private var mSmsTimer: Timer? = null
    private lateinit var binding: ActivityRegisterBinding

    private var birthdayTimePickerView: TimePickerView? = null

    private var salarySourceSelectedData: StatusSheetData? = null
    private var bettingShopSelectedData: StatusSheetData? = null

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

    @SuppressLint("SetTextI18n")
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
        setupBirthday()
        setupRegisterIdentity()
        setupSalarySource()
        setupBettingShop()
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
                etLoginPassword.hasFocus = true
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
                etConfirmPassword.hasFocus = true
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
                etWithdrawalPwd.hasFocus = true
                eetWithdrawalPwd.setSelection(eetWithdrawalPwd.text.toString().length)
            }
            btnRegister.setTitleLetterSpacing()
        }

        binding.ivReturn.setOnClickListener(this)
        binding.tvDuty.setOnClickListener(this)
        binding.tvPrivacy.text =
            "1."+getString(R.string.register_privacy) + getString(R.string.register_privacy_policy) + getString(
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
        val appName = getString(R.string.app_name)
        //中英appName在前半 越南文appName會在後半
        binding.tvAgreement.text = when (LanguageManager.getSelectLanguage(this@RegisterActivity)) {
            LanguageManager.Language.VI -> "2."+String.format(getString(R.string.register_over_21), appName) + getString(R.string.register_rules) + String.format(getString(R.string.register_rules_2nd_half), appName)
            else -> "2."+String.format(getString(R.string.register_over_21), appName) + getString(R.string.register_rules)
        }

        binding.tvAgreement.makeLinks(
            Pair(getString(R.string.register_rules), View.OnClickListener {
                JumpUtil.toInternalWeb(
                    this,
                    Constants.getAgreementRuleUrl(this),
                    resources.getString(R.string.terms_conditions)
                )
            })
        )

        binding.tvNotPHOfficial.text = "3."+getString(R.string.register_not_ph_official)
        binding.tvNotPHSchool.text = "4."+getString(R.string.register_not_ph_school)
        binding.tvRuleOkbet.text = "5."+getString(R.string.register_rule_okbet)
        binding.tvAgreeAll.text = getString(R.string.register_rule_agree_all)
        setLetterSpace()
    }
    private fun setLetterSpace(){
        if (LanguageManager.getSelectLanguage(this)==LanguageManager.Language.ZH) {
            binding.btnRegister.letterSpacing = 0.6f
        }
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

    private fun setupBirthday() {
        binding.etBirth.visibility =
            if (sConfigData?.enableBirthday == FLAG_OPEN) View.VISIBLE else View.GONE

        with(binding) {
            birthPicker.setOnClickListener {
                //設置TextFieldBoxes為選中狀態
                etBirth.hasFocus = true
                //隱藏光標
                eetBirth.isCursorVisible = false
                //隱藏鍵盤
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

                birthdayTimePickerView?.show()
            }

            birthdayTimePickerView = createTimePicker { date ->
                eetBirth.setText(TimeUtil.stampToRegisterBirthdayFormat(date))
            }
        }
    }

    private fun setupRegisterIdentity() {
        with(binding) {
            etIdentity.visibility =
                if (sConfigData?.enableIdentityNumber == FLAG_OPEN) View.VISIBLE else View.GONE

            etIdentity.endIconImageButton.setOnClickListener {
                if (etIdentity.endIconResourceId == R.drawable.ic_eye_open) {
                    eetIdentity.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    etIdentity.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etIdentity.setEndIcon(R.drawable.ic_eye_open)
                    eetIdentity.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                }
                etIdentity.hasFocus = true
                eetIdentity.setSelection(eetIdentity.text.toString().length)
            }
        }
    }

    private fun setupSalarySource() {
        with(binding) {
            //顯示隱藏該選項
            etSalary.visibility =
                if (sConfigData?.enableSalarySource == FLAG_OPEN) View.VISIBLE else View.GONE

            //根據config配置薪資來源選項
            val salarySourceList = mutableListOf<StatusSheetData>()
            sConfigData?.salarySource?.map { salarySource ->
                salarySourceList.add(StatusSheetData(salarySource.id.toString(), salarySource.name))
            }

            //預設顯示第一項
            salarySourceSelectedData = salarySourceList.firstOrNull()
            eetSalary.setText(salarySourceList.firstOrNull()?.showName)
            //設置預設文字後會變成選中狀態, 需清除focus
            etSalary.hasFocus = false
            viewModel.checkSalary(eetSalary.text.toString())

            //配置點擊展開選項選單
            etSalary.post {
                salarySpinner.setSpinnerView(
                    eetSalary,
                    etSalary,
                    salarySourceList,
                    touchListener = {
                        //旋轉箭頭
                        etSalary.endIconImageButton.rotation = 180F
                    },
                    itemSelectedListener = {
                        salarySourceSelectedData = it
                        eetSalary.setText(it?.showName)
                    },
                    popupWindowDismissListener = {
                        //旋轉箭頭
                        etSalary.endIconImageButton.rotation = 0F
                    })
            }
        }
    }

    private fun setupBettingShop() {
        with(binding) {
            val bettingStationVisibility = sConfigData?.enableBettingStation == FLAG_OPEN

            if (bettingStationVisibility) {
                etBettingShop.visibility = View.VISIBLE
                //查詢投注站列表
                viewModel.bettingStationQuery()
            } else {
                etBettingShop.visibility = View.GONE
            }
        }
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
        binding.apply {
            cbAgreeAll.setOnClickListener {
                viewModel.apply {
                    checkCbAgreeAll(cbAgreeAll.isChecked)
                }
            }
            btnRegister.setTitleLetterSpacing()
        }
    }


    private fun setupRegisterButton() {
        binding.apply {
            eetRecommendCode.apply {
                checkRegisterListener { viewModel.checkInviteCode(it) }
            }
            eetMemberAccount.apply {
                checkRegisterListener { viewModel.checkAccountExist(it) }
            }
            eetLoginPassword.apply {
                checkRegisterListener { viewModel.checkLoginPassword(it) }
            }
            eetConfirmPassword.apply {
                checkRegisterListener { viewModel.checkConfirmPassword(eetLoginPassword.text.toString(), it) }
            }
            eetFullName.apply {
                checkRegisterListener { viewModel.checkFullName(it) }
            }
            eetBirth.apply {
                checkRegisterListener { viewModel.checkBirth(it) }
            }
            eetIdentity.apply {
                checkRegisterListener { viewModel.checkIdentity(it) }
            }
            eetSalary.apply {
                checkRegisterListener { viewModel.checkSalary(it) }
            }
            eetBettingShop.apply {
                checkRegisterListener { viewModel.checkBettingShop(it) }
            }
            eetWithdrawalPwd.apply {
                checkRegisterListener { viewModel.checkFundPwd(it) }
            }
            eetQq.apply {
                checkRegisterListener { viewModel.checkQQ(it) }
            }
            eetPhone.apply {
                checkRegisterListener { viewModel.checkPhone(it) }
            }
            eetMail.apply {
                checkRegisterListener { viewModel.checkEmail(it) }
            }
            eetPostal.apply {
                checkRegisterListener { viewModel.checkPostal(it) }
            }
            eetProvince.apply {
                checkRegisterListener { viewModel.checkProvince(it) }
            }
            eetCity.apply {
                checkRegisterListener { viewModel.checkCity(it) }
            }
            eetAddress.apply {
                checkRegisterListener { viewModel.checkAddress(it) }
            }
            eetWeChat.apply {
                checkRegisterListener { viewModel.checkWeChat(it) }
            }
            eetZalo.apply {
                checkRegisterListener { viewModel.checkZalo(it) }
            }
            eetFacebook.apply {
                checkRegisterListener { viewModel.checkFacebook(it) }
            }
            eetWhatsApp.apply {
                checkRegisterListener { viewModel.checkWhatsApp(it) }
            }
            eetTelegram.apply {
                checkRegisterListener { viewModel.checkTelegram(it) }
            }
            eetSecurityPb.apply {
                checkRegisterListener { viewModel.checkSecurityPb(it) }
            }
            eetSmsValidCode.apply {
                checkRegisterListener { viewModel.checkSecurityCode(it) }
            }
            eetVerificationCode.apply {
                checkRegisterListener { viewModel.checkValidCode(it) }
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
                    cbAgreeAll.isChecked,
                    deviceSn,
                    deviceId,
                    birth = eetBirth.text.toString().replace(" ",""), //傳給後端的不需要有空白間隔
                    identity = eetIdentity.text.toString(),
                    salarySource = salarySourceSelectedData?.code,
                    bettingShop = bettingShopSelectedData?.code
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
            binding.btnRegister.isEnabled = it
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
                    it.first,
                    false
                )
            }
            loginPasswordMsg.observe(this@RegisterActivity) {
                binding.etLoginPassword.setError(
                    it.first,
                    false
                )
            }
            confirmPasswordMsg.observe(this@RegisterActivity) {
                binding.etConfirmPassword.setError(
                    it.first,
                    false
                )
            }
            fullNameMsg.observe(this@RegisterActivity) { binding.etFullName.setError(it.first, false) }
            fundPwdMsg.observe(this@RegisterActivity) {
                binding.etWithdrawalPwd.setError(
                    it.first,
                    false
                )
            }
            qqMsg.observe(this@RegisterActivity) { binding.etQq.setError(it.first, false) }
            phoneMsg.observe(this@RegisterActivity) { binding.etPhone.setError(it.first, false) }
            emailMsg.observe(this@RegisterActivity) { binding.etMail.setError(it.first, false) }
            postalMsg.observe(this@RegisterActivity) { binding.etPostal.setError(it.first, false) }
            provinceMsg.observe(this@RegisterActivity) { binding.etProvince.setError(it.first, false) }
            cityMsg.observe(this@RegisterActivity) { binding.etCity.setError(it.first, false) }
            addressMsg.observe(this@RegisterActivity) { binding.etAddress.setError(it.first, false) }
            salaryMsg.observe(this@RegisterActivity) { binding.etSalary.setError(it.first, false) }
            birthMsg.observe(this@RegisterActivity) { binding.etBirth.setError(it.first, false) }
            identityMsg.observe(this@RegisterActivity) { binding.etIdentity.setError(it.first, false) }
            bettingShopMsg.observe(this@RegisterActivity) { binding.etBettingShop.setError(it.first, false) }
            weChatMsg.observe(this@RegisterActivity) { binding.etWeChat.setError(it.first, false) }
            zaloMsg.observe(this@RegisterActivity) { binding.etZalo.setError(it.first, false) }
            facebookMsg.observe(this@RegisterActivity) { binding.etFacebook.setError(it.first, false) }
            whatsAppMsg.observe(this@RegisterActivity) { binding.etWhatsApp.setError(it.first, false) }
            telegramMsg.observe(this@RegisterActivity) { binding.etTelegram.setError(it.first, false) }
            securityPbMsg.observe(this@RegisterActivity) { binding.etSecurityPb.setError(it.first, false) }
            securityCodeMsg.observe(this@RegisterActivity) {
                binding.etSmsValidCode.setError(
                    it.first,
                    false
                )
            }
            validCodeMsg.observe(this@RegisterActivity) {
                binding.etVerificationCode.setError(
                    it.first,
                    false
                )
            }
        }

        viewModel.bettingStationList.observe(this) { bettingStationList ->
            with(binding) {
                //設置投注站清單選項
                bettingShopSpinner.setSpinnerView(
                    eetBettingShop,
                    etBettingShop,
                    bettingStationList,
                    touchListener = {
                        //旋轉箭頭
                        etBettingShop.endIconImageButton.rotation = 180F
                    },
                    itemSelectedListener = {
                        bettingShopSelectedData = it
                        eetBettingShop.setText(it?.showName)
                    },
                    popupWindowDismissListener = {
                        //旋轉箭頭
                        etBettingShop.endIconImageButton.rotation = 0F
                    })

                //預設第一項
                bettingShopSelectedData = bettingStationList.firstOrNull()
                eetBettingShop.setText(bettingStationList.firstOrNull()?.showName)
                //預設後會變為選中狀態, 需清除focus
                etBettingShop.hasFocus = false
                viewModel.checkBettingShop(eetBettingShop.text.toString())
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
                                    R.color.color_AEAEAE_404040
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

    /**
     * 創建生日用日期選擇器
     * 日期範圍: ~今天
     * 日期格式: 年月日
     */
    private fun createTimePicker(timeSelectedListener: (time: Date) -> Unit): TimePickerView {
        val today = Calendar.getInstance() //用來限制生日的結束日期 (今天)

        val dateTimePicker: TimePickerView = TimePickerBuilder(this
        ) { date, _ ->
            try {
                timeSelectedListener(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            .setLabel("", "", "", "", "", "")
            .setRangDate(null, today)
            .setDate(Calendar.getInstance())
            .setTimeSelectChangeListener { }
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setCancelText(" ")
            .setSubmitText(getString(R.string.picker_submit))
            .setTitleColor(ContextCompat.getColor(this, R.color.color_CCCCCC_000000))
            .setTitleBgColor(ContextCompat.getColor(this, R.color.color_2B2B2B_e2e2e2))
            .setBgColor(ContextCompat.getColor(this, R.color.color_191919_FCFCFC))
            .setSubmitColor(ContextCompat.getColor(this, R.color.color_7F7F7F_999999))
            .setCancelColor(ContextCompat.getColor(this, R.color.color_7F7F7F_999999))
            .isDialog(true)
            .build() as TimePickerView

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        )

        params.leftMargin = 0
        params.rightMargin = 0
        dateTimePicker.dialogContainerLayout.layoutParams = params
        val dialogWindow = dateTimePicker.dialog.window
        if (dialogWindow != null) {
            dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim)
            dialogWindow.setGravity(Gravity.BOTTOM)
            dialogWindow.setDimAmount(0.1f)
        }

        return dateTimePicker
    }
}
