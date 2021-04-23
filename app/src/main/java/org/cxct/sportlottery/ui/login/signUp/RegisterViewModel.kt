package org.cxct.sportlottery.ui.login.signUp

import android.content.Context
import android.text.Spanned
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.register.RegisterRequest
import org.cxct.sportlottery.network.index.sendSms.SmsRequest
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.FileUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.VerifyConstUtil

class RegisterViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {
    val registerResult: LiveData<LoginResult>
        get() = _registerResult
    val inviteCodeMsg: LiveData<String?>
        get() = _inviteCodeMsg
    val memberAccountMsg: LiveData<String?>
        get() = _memberAccountMsg
    val loginPasswordMsg: LiveData<String?>
        get() = _loginPasswordMsg
    val confirmPasswordMsg: LiveData<String?>
        get() = _confirmPasswordMsg
    val fullNameMsg: LiveData<String?>
        get() = _fullNameMsg
    val fundPwdMsg: LiveData<String?>
        get() = _fundPwdMsg
    val qqMsg: LiveData<String?>
        get() = _qqMsg
    val phoneMsg: LiveData<String?>
        get() = _phoneMsg
    val emailMsg: LiveData<String?>
        get() = _emailMsg
    val weChatMsg: LiveData<String?>
        get() = _weChatMsg
    val zaloMsg: LiveData<String?>
        get() = _zaloMsg
    val facebookMsg: LiveData<String?>
        get() = _facebookMsg
    val whatsAppMsg: LiveData<String?>
        get() = _whatsAppMsg
    val telegramMsg: LiveData<String?>
        get() = _telegramMsg
    val securityCodeMsg: LiveData<String?>
        get() = _securityCodeMsg
    val validCodeMsg: LiveData<String?>
        get() = _validCodeMsg
    val registerEnable: LiveData<Boolean>
        get() = _registerEnable
    val validCodeResult: LiveData<ValidCodeResult?>
        get() = _validCodeResult
    val smsResult: LiveData<SmsResult?>
        get() = _smsResult
    val loginForGuestResult: LiveData<LoginResult>
        get() = _loginForGuestResult

    val agreementChecked: LiveData<Boolean?>
        get() = _agreementChecked

    private val _registerResult = MutableLiveData<LoginResult>()
    private val _inviteCodeMsg = MutableLiveData<String?>()
    private val _memberAccountMsg = MutableLiveData<String?>()
    private val _loginPasswordMsg = MutableLiveData<String?>()
    private val _confirmPasswordMsg = MutableLiveData<String?>()
    private val _fullNameMsg = MutableLiveData<String?>()
    private val _fundPwdMsg = MutableLiveData<String?>()
    private val _qqMsg = MutableLiveData<String?>()
    private val _phoneMsg = MutableLiveData<String?>()
    private val _emailMsg = MutableLiveData<String?>()
    private val _weChatMsg = MutableLiveData<String?>()
    private val _zaloMsg = MutableLiveData<String?>()
    private val _facebookMsg = MutableLiveData<String?>()
    private val _whatsAppMsg = MutableLiveData<String?>()
    private val _telegramMsg = MutableLiveData<String?>()
    private val _securityCodeMsg = MutableLiveData<String?>()
    private val _validCodeMsg = MutableLiveData<String?>()
    private val _registerEnable = MutableLiveData<Boolean>()

    private val _validCodeResult = MutableLiveData<ValidCodeResult?>()
    private val _smsResult = MutableLiveData<SmsResult?>()
    private val _loginForGuestResult = MutableLiveData<LoginResult>()
    private val _agreementChecked = MutableLiveData<Boolean?>()

    fun getAgreementContent(context: Context): Spanned {
        //TODO 添加多國語系 開戶協議 檔案路徑 mapping
        val path = when (LanguageManager.getSelectLanguage(context)) {
            LanguageManager.Language.ZH -> "agreement/register_agreement_zh.html"
            LanguageManager.Language.ZHT -> "agreement/register_agreement_zh.html"
            LanguageManager.Language.EN -> "agreement/register_agreement_zh.html"
            LanguageManager.Language.VI -> "agreement/register_agreement_zh.html"
        }

        val assetManager = context.assets
        val htmlString = FileUtil.readStringFromAssetManager(assetManager, path) ?: ""
        return HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    /**
     * 檢查輸入欄位
     */

    fun checkInviteCode(inviteCode: String?) {
        _inviteCodeMsg.value = when {
            inviteCode.isNullOrBlank() -> {
                if (sConfigData?.enableInviteCode != FLAG_OPEN)
                    null
                else
                    androidContext.getString(R.string.error_input_empty)
            }
            !VerifyConstUtil.verifyInviteCode(inviteCode) -> androidContext.getString(R.string.error_recommend_code)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkMemberAccount(account: String?, isExistAccount: Boolean) {
        _memberAccountMsg.value = when {
            account.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            isExistAccount -> androidContext.getString(R.string.error_register_id_exist)
            !VerifyConstUtil.verifyCombinationAccount(account) -> {
                androidContext.getString(R.string.error_member_account)
            }
            !VerifyConstUtil.verifyAccount(account) -> androidContext.getString(R.string.error_member_account)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkLoginPassword(password: String?) {
        _loginPasswordMsg.value = when {
            password.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPwdFormat(password) -> androidContext.getString(R.string.error_register_password)
            password.length !in 6..20 -> androidContext.getString(R.string.error_register_password)
            !VerifyConstUtil.verifyPwd(password) -> androidContext.getString(R.string.error_register_password)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkConfirmPassword(password: String?, confirmPassword: String?) {
        _confirmPasswordMsg.value = when {
            password.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            password != confirmPassword -> androidContext.getString(R.string.error_confirm_password)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkFullName(fullName: String?) {
        _fullNameMsg.value = when {
            fullName.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyFullName(fullName) -> androidContext.getString(R.string.error_incompatible_format)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkFundPwd(fundPwd: String?) {
        _fundPwdMsg.value = when {
            fundPwd.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPayPwd(fundPwd) -> androidContext.getString(R.string.error_withdrawal_pwd)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkQQ(qq: String?) {
        _qqMsg.value = when {
            qq.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyQQ(qq) -> androidContext.getString(R.string.error_qq_number)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkPhone(phone: String?) {
        _phoneMsg.value = when {
            phone.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPhone(phone) -> androidContext.getString(R.string.error_phone_number)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkEmail(email: String?) {
        _emailMsg.value = when {
            email.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyMail(email) -> androidContext.getString(R.string.error_e_mail)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkWeChat(weChat: String?) {
        _weChatMsg.value = when {
            weChat.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyWeChat(weChat) -> androidContext.getString(R.string.error_we_chat_number)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkZalo(zalo: String?) {
        _zaloMsg.value = when {
            zalo.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyZalo(zalo) -> androidContext.getString(R.string.error_zalo)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkFacebook(facebook: String?) {
        _facebookMsg.value = when {
            facebook.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyFacebook(facebook) -> androidContext.getString(R.string.error_facebook)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkWhatsApp(whatsApp: String?) {
        _whatsAppMsg.value = when {
            whatsApp.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyWhatsApp(whatsApp) -> androidContext.getString(R.string.error_whats_app)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkTelegram(telegram: String?) {
        _telegramMsg.value = when {
            telegram.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyTelegram(telegram) -> androidContext.getString(R.string.error_telegram)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkSecurityCode(securityCode: String?) {
        _securityCodeMsg.value = when {
            securityCode.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifySecurityCode(securityCode) -> androidContext.getString(R.string.error_verification_code_by_sms)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun checkValidCode(validCode: String?) {
        _validCodeMsg.value = when {
            validCode.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyValidCode(validCode) -> androidContext.getString(R.string.error_verification_code)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    fun sendSms(phone: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.sendSms(
                    SmsRequest(phone)
                )
            }
            _smsResult.postValue(result)
        }
    }

    fun getValidCode() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.getValidCode(ValidCodeRequest(validCodeResult.value?.validCodeData?.identity))
            }
            _validCodeResult.postValue(result)
        }
    }

    fun checkAgreement(checked: Boolean?) {
        if (checked == false) {
            _agreementChecked.value = false
        } else {
            _agreementChecked.value = null
        }
        focusChangeCheckAllInputComplete()
    }

    //檢查是否所有欄位都填寫完畢
    private fun checkAllInput(
        inviteCode: String,
        userName: String,
        loginPassword: String,
        confirmPassword: String,
        fullName: String,
        fundPwd: String,
        qq: String,
        phone: String,
        email: String,
        weChat: String,
        zalo: String,
        facebook: String,
        whatsApp: String,
        telegram: String,
        smsCode: String,
        validCode: String,
        agreementChecked: Boolean,
    ) {
        if (sConfigData?.enableInviteCode == FLAG_OPEN)
            checkInviteCode(inviteCode)

        checkMemberAccount(userName, false)
        checkLoginPassword(loginPassword)
        checkConfirmPassword(loginPassword, confirmPassword)

        if (sConfigData?.enableFullName == FLAG_OPEN)
            checkFullName(fullName)
        if (sConfigData?.enableFundPwd == FLAG_OPEN)
            checkFundPwd(fundPwd)
        if (sConfigData?.enableQQ == FLAG_OPEN)
            checkQQ(qq)
        if (sConfigData?.enablePhone == FLAG_OPEN)
            checkPhone(phone)
        if (sConfigData?.enableEmail == FLAG_OPEN)
            checkEmail(email)
        if (sConfigData?.enableWechat == FLAG_OPEN)
            checkWeChat(weChat)
        if (sConfigData?.enableZalo == FLAG_OPEN)
            checkZalo(zalo)
        if (sConfigData?.enableFacebook == FLAG_OPEN)
            checkFacebook(facebook)
        if (sConfigData?.enableWhatsApp == FLAG_OPEN)
            checkWhatsApp(whatsApp)
        if (sConfigData?.enableTelegram == FLAG_OPEN)
            checkTelegram(telegram)
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN)
            checkSecurityCode(smsCode)
        if (sConfigData?.enableRegValidCode == FLAG_OPEN)
            checkValidCode(validCode)

        checkAgreement(agreementChecked)
    }

    private fun focusChangeCheckAllInputComplete() {
        _registerEnable.value = checkAllInputComplete()
    }


    private fun checkAllInputComplete(): Boolean {
        if (sConfigData?.enableInviteCode == FLAG_OPEN && inviteCodeMsg.value != null)
            return false
        if (memberAccountMsg.value != null)
            return false
        if (loginPasswordMsg.value != null)
            return false
        if (confirmPasswordMsg.value != null)
            return false
        if (sConfigData?.enableFullName == FLAG_OPEN && fullNameMsg.value != null)
            return false
        if (sConfigData?.enableFundPwd == FLAG_OPEN && fundPwdMsg.value != null)
            return false
        if (sConfigData?.enableQQ == FLAG_OPEN && qqMsg.value != null)
            return false
        if (sConfigData?.enablePhone == FLAG_OPEN && phoneMsg.value != null)
            return false
        if (sConfigData?.enableEmail == FLAG_OPEN && emailMsg.value != null)
            return false
        if (sConfigData?.enableWechat == FLAG_OPEN && weChatMsg.value != null)
            return false
        if (sConfigData?.enableZalo == FLAG_OPEN && zaloMsg.value != null)
            return false
        if (sConfigData?.enableFacebook == FLAG_OPEN && facebookMsg.value != null)
            return false
        if (sConfigData?.enableWhatsApp == FLAG_OPEN && whatsAppMsg.value != null)
            return false
        if (sConfigData?.enableTelegram == FLAG_OPEN && telegramMsg.value != null)
            return false
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN && securityCodeMsg.value != null)
            return false
        if (sConfigData?.enableRegValidCode == FLAG_OPEN && validCodeMsg.value != null)
            return false
        if (agreementChecked.value == false)
            return false

        return true
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.loginForGuest()
            }?.let {
                _loginForGuestResult.value = it
            }
        }
    }

    fun checkAccountExist(account: String) {
        _memberAccountMsg.value = androidContext.getString(R.string.desc_register_checking_account)
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.checkAccountExist(account)
            }
            checkMemberAccount(account, result?.isExist ?: false)
        }
    }

    fun registerSubmit(
        inviteCode: String,
        userName: String,
        loginPassword: String,
        confirmPassword: String,
        fullName: String,
        fundPwd: String,
        qq: String,
        phone: String,
        email: String,
        weChat: String,
        zalo: String,
        facebook: String,
        whatsApp: String,
        telegram: String,
        smsCode: String,
        validCode: String,
        agreementChecked: Boolean,
        deviceSn: String
    ) {
        checkAllInput(
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
            agreementChecked
        )
        if (checkAllInputComplete()) {
            register(createRegisterRequest(inviteCode, userName, loginPassword, fullName, fundPwd, qq, phone, email, weChat, zalo, facebook, whatsApp, telegram, smsCode, validCode, deviceSn))
        }
    }

    private fun createRegisterRequest(
        inviteCode: String,
        userName: String,
        loginPassword: String,
        fullName: String,
        fundPwd: String,
        qq: String,
        phone: String,
        email: String,
        weChat: String,
        zalo: String,
        facebook: String,
        whatsApp: String,
        telegram: String,
        smsCode: String,
        validCode: String,
        deviceSn: String
    ): RegisterRequest {
        return RegisterRequest(
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
            if (sConfigData?.enableWhatsApp == FLAG_OPEN)
                this.whatsapp = whatsApp
            if (sConfigData?.enableTelegram == FLAG_OPEN)
                this.telegram = telegram
            if (sConfigData?.enableSmsValidCode == FLAG_OPEN)
                this.securityCode = smsCode
            if (sConfigData?.enableRegValidCode == FLAG_OPEN) {
                this.validCodeIdentity = validCodeResult.value?.validCodeData?.identity
                this.validCode = validCode
            }
        }
    }

    private fun register(registerRequest: RegisterRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.register(registerRequest)
            }?.let { result ->
                _registerResult.postValue(result)
            }
        }
    }
}