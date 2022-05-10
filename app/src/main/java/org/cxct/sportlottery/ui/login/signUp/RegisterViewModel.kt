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
    infoCenterRepository: InfoCenterRepository,
    protected val userInfoRepository: UserInfoRepository
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
    val postalMsg: LiveData<String?>
        get() = _postalMsg
    val provinceMsg: LiveData<String?>
        get() = _provinceMsg
    val cityMsg: LiveData<String?>
        get() = _cityMsg
    val addressMsg: LiveData<String?>
        get() = _addressMsg
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
    val securityPbMsg: LiveData<String?>
        get() = _securityPbMsg
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


    private val cbPrivacyChecked: LiveData<Boolean?>
        get() = _cbPrivacyChecked

    private val agreementChecked: LiveData<Boolean?>
        get() = _agreementChecked

    private val cbNotPHOfficialChecked: LiveData<Boolean?>
        get() = _cbNotPHOfficialChecked

    private val cbNotPHSchoolChecked: LiveData<Boolean?>
        get() = _cbNotPHSchoolChecked

    private val cbRuleOkbetChecked: LiveData<Boolean?>
        get() = _cbRuleOkbetChecked

    private val cbAgreeAllChecked: LiveData<Boolean?>
        get() = _cbAgreeAllChecked

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
    private val _postalMsg = MutableLiveData<String?>()
    private val _provinceMsg = MutableLiveData<String?>()
    private val _cityMsg = MutableLiveData<String?>()
    private val _addressMsg = MutableLiveData<String?>()
    private val _weChatMsg = MutableLiveData<String?>()
    private val _zaloMsg = MutableLiveData<String?>()
    private val _facebookMsg = MutableLiveData<String?>()
    private val _whatsAppMsg = MutableLiveData<String?>()
    private val _telegramMsg = MutableLiveData<String?>()
    private val _securityPbMsg = MutableLiveData<String?>()
    private val _securityCodeMsg = MutableLiveData<String?>()
    private val _validCodeMsg = MutableLiveData<String?>()
    private val _registerEnable = MutableLiveData<Boolean>()

    private val _validCodeResult = MutableLiveData<ValidCodeResult?>()
    private val _smsResult = MutableLiveData<SmsResult?>()
    private val _loginForGuestResult = MutableLiveData<LoginResult>()

    private val _cbPrivacyChecked = MutableLiveData<Boolean?>()
    private val _agreementChecked = MutableLiveData<Boolean?>()
    private val _cbNotPHOfficialChecked = MutableLiveData<Boolean?>()
    private val _cbNotPHSchoolChecked = MutableLiveData<Boolean?>()
    private val _cbRuleOkbetChecked = MutableLiveData<Boolean?>()
    private val _cbAgreeAllChecked = MutableLiveData<Boolean?>()

    @Deprecated("沒在用")
    fun getAgreementContent(context: Context): Spanned {
        //TODO 添加多國語系 開戶協議 檔案路徑 mapping
        val path = when (LanguageManager.getSelectLanguage(context)) {
            LanguageManager.Language.ZH -> "agreement/register_agreement_zh.html"
            LanguageManager.Language.ZHT -> "agreement/register_agreement_zh.html"
            LanguageManager.Language.EN -> "agreement/register_agreement_en.html"
            LanguageManager.Language.VI -> "agreement/register_agreement_vi.html"
        }

        val assetManager = context.assets
        val htmlString = FileUtil.readStringFromAssetManager(assetManager, path) ?: ""
        return HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    /**
     * 檢查輸入欄位
     */

    private fun checkInviteCode(inviteCode: String?) {
        _inviteCodeMsg.value = when {
            inviteCode.isNullOrEmpty() -> {
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
            account.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            isExistAccount -> androidContext.getString(R.string.error_register_id_exist)
            !VerifyConstUtil.verifyCombinationAccount(account) -> {
                androidContext.getString(R.string.error_member_account)
            }
            !VerifyConstUtil.verifyAccount(account) -> androidContext.getString(R.string.error_member_account)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkLoginPassword(password: String?) {
        _loginPasswordMsg.value = when {
            password.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPwdFormat(password) -> androidContext.getString(R.string.error_register_password)
            password.length !in 6..20 -> androidContext.getString(R.string.error_register_password)
            !VerifyConstUtil.verifyPwd(password) -> androidContext.getString(R.string.error_register_password)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkConfirmPassword(password: String?, confirmPassword: String?) {
        _confirmPasswordMsg.value = when {
            password.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            password != confirmPassword -> androidContext.getString(R.string.error_confirm_password)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkFullName(fullName: String?) {
        _fullNameMsg.value = when {
            fullName.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkFundPwd(fundPwd: String?) {
        _fundPwdMsg.value = when {
            fundPwd.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPayPwd(fundPwd) -> androidContext.getString(R.string.error_withdrawal_pwd)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkQQ(qq: String?) {
        _qqMsg.value = when {
            qq.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyQQ(qq) -> androidContext.getString(R.string.error_qq_number)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkPhone(phone: String?) {
        _phoneMsg.value = when {
            phone.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkEmail(email: String?) {
        _emailMsg.value = when {
            email.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyMail(email) -> androidContext.getString(R.string.error_e_mail)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkPostal(postalCode: String?) {
        _postalMsg.value = when {
            postalCode.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkProvince(province: String?) {
        _provinceMsg.value = when {
            province.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkCity(city: String?) {
        _cityMsg.value = when {
            city.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkAddress(address: String?) {
        _addressMsg.value = when {
            address.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkWeChat(weChat: String?) {
        _weChatMsg.value = when {
            weChat.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkZalo(zalo: String?) {
        _zaloMsg.value = when {
            zalo.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyZalo(zalo) -> androidContext.getString(R.string.error_zalo)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkFacebook(facebook: String?) {
        _facebookMsg.value = when {
            facebook.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyFacebook(facebook) -> androidContext.getString(R.string.error_facebook)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkWhatsApp(whatsApp: String?) {
        _whatsAppMsg.value = when {
            whatsApp.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyWhatsApp(whatsApp) -> androidContext.getString(R.string.error_whats_app)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkTelegram(telegram: String?) {
        _telegramMsg.value = when {
            telegram.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyTelegram(telegram) -> androidContext.getString(R.string.error_telegram)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkSecurityPb(securityPb: String?) {
        _securityPbMsg.value = when {
            securityPb.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkSecurityCode(securityCode: String?) {
        _securityCodeMsg.value = when {
            securityCode.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifySecurityCode(securityCode) -> androidContext.getString(R.string.error_verification_code_by_sms)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    private fun checkValidCode(validCode: String?) {
        _validCodeMsg.value = when {
            validCode.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
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

    fun checkcbPrivacy(checked: Boolean?) {
        _cbPrivacyChecked.value = if(checked == false) false else null
    }

    fun checkAgreement(checked: Boolean?) {
        _agreementChecked.value = if(checked == false) false else null
        focusChangeCheckAllInputComplete()
    }

    fun checkcbNotPHOfficial(checked: Boolean?) {
        _cbNotPHOfficialChecked.value = if(checked == false) false else null
    }

    fun checkcbNotPHSchool(checked: Boolean?) {
        _cbNotPHSchoolChecked.value = if(checked == false) false else null
    }

    fun checkcbRuleOkbet(checked: Boolean?) {
        _cbRuleOkbetChecked.value = if(checked == false) false else null
    }

    fun checkcbAgreeAll(checked: Boolean?) {
        _cbAgreeAllChecked.value = if(checked == false) false else null
    }

    //檢查是否所有欄位都填寫完畢
    fun checkAllInput(
        inviteCode: String,
        userName: String,
        loginPassword: String,
        confirmPassword: String,
        fullName: String,
        fundPwd: String,
        qq: String,
        phone: String,
        email: String,
        postalCode: String,
        province: String,
        city: String,
        address: String,
        weChat: String,
        zalo: String,
        facebook: String,
        whatsApp: String,
        telegram: String,
        securityPb: String,
        smsCode: String,
        validCode: String,
        cbPrivacyChecked: Boolean,
        agreementChecked: Boolean,
        cbNotPHOfficialChecked: Boolean,
        cbNotPHSchoolChecked: Boolean,
        cbRuleOkbetChecked: Boolean,
        cbAgreeAllChecked: Boolean,
    ): Boolean {
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
        if (sConfigData?.enableAddress == FLAG_OPEN){
            checkPostal(postalCode)
            checkProvince(province)
            checkCity(city)
            checkAddress(address)
        }
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
        if(sConfigData?.enableSafeQuestion == FLAG_OPEN)
            checkSecurityPb(securityPb)
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN)
            checkSecurityCode(smsCode)
        if (sConfigData?.enableRegValidCode == FLAG_OPEN)
            checkValidCode(validCode)

        checkcbPrivacy(cbPrivacyChecked)
        checkAgreement(agreementChecked)
        checkcbNotPHOfficial(cbNotPHOfficialChecked)
        checkcbNotPHSchool(cbNotPHSchoolChecked)
        checkcbRuleOkbet(cbRuleOkbetChecked)
        checkcbAgreeAll(cbAgreeAllChecked)

        return checkAllInputComplete()
    }

    private fun focusChangeCheckAllInputComplete() {
        _registerEnable.value = checkAllInputComplete()
    }


    fun checkAllInputComplete(): Boolean {
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
        if (sConfigData?.enableAddress == FLAG_OPEN && (postalMsg.value != null || provinceMsg.value != null || cityMsg.value != null || addressMsg.value != null))
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
        if (sConfigData?.enableSafeQuestion == FLAG_OPEN && securityPbMsg.value != null)
            return false
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN && securityCodeMsg.value != null)
            return false
        if (sConfigData?.enableRegValidCode == FLAG_OPEN && validCodeMsg.value != null)
            return false

        if (cbPrivacyChecked.value == false)
            return false
        if (agreementChecked.value == false)
            return false
        if (cbNotPHOfficialChecked.value == false)
            return false
        if (cbNotPHSchoolChecked.value == false)
            return false
        if (cbRuleOkbetChecked.value == false)
            return false
        if (cbAgreeAllChecked.value == false)
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
        postalCode: String,
        province: String,
        city: String,
        address: String,
        weChat: String,
        zalo: String,
        facebook: String,
        whatsApp: String,
        telegram: String,
        securityPb: String,
        smsCode: String,
        validCode: String,
        cbPrivacyChecked: Boolean,
        agreementChecked: Boolean,
        cbNotPHOfficialChecked: Boolean,
        cbNotPHSchoolChecked: Boolean,
        cbRuleOkbetChecked: Boolean,
        cbAgreeAllChecked: Boolean,
        deviceSn: String,
        deviceId: String
    ) {
        if (checkAllInput(
                inviteCode,
                userName,
                loginPassword,
                confirmPassword,
                fullName,
                fundPwd,
                qq,
                phone,
                email,
                postalCode,
                province,
                city,
                address,
                weChat,
                zalo,
                facebook,
                whatsApp,
                telegram,
                securityPb,
                smsCode,
                validCode,
                cbPrivacyChecked,
                agreementChecked,
                cbNotPHOfficialChecked,
                cbNotPHSchoolChecked,
                cbRuleOkbetChecked,
                cbAgreeAllChecked,
            )) {
            register(
                createRegisterRequest(
                    inviteCode,
                    userName,
                    loginPassword,
                    fullName,
                    fundPwd,
                    qq,
                    phone,
                    email,
                    postalCode,
                    province,
                    city,
                    address,
                    weChat,
                    zalo,
                    facebook,
                    whatsApp,
                    telegram,
                    securityPb,
                    smsCode,
                    validCode,
                    deviceSn,
                    deviceId
                )
            )
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
        postalCode: String,
        province: String,
        city: String,
        address: String,
        weChat: String,
        zalo: String,
        facebook: String,
        whatsApp: String,
        telegram: String,
        securityPb: String,
        smsCode: String,
        validCode: String,
        deviceSn: String,
        deviceId: String
    ): RegisterRequest {
        return RegisterRequest(
            userName = userName,
            password = MD5Util.MD5Encode(loginPassword),
            loginSrc = LOGIN_SRC,
            deviceSn = deviceSn,
            inviteCode = inviteCode,
            loginEnvInfo = deviceId
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
            if (sConfigData?.enableAddress == FLAG_OPEN) {
                this.zipCode = postalCode
                this.province = province
                this.city = city
                this.address = address
            }
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
            if (sConfigData?.enableSafeQuestion == FLAG_OPEN)
                this.safeQuestion = securityPb
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
                // TODO 20220108 更新UserInfo by Hewie
                userInfoRepository.getUserInfo()
                _registerResult.postValue(result)
            }
        }
    }
}