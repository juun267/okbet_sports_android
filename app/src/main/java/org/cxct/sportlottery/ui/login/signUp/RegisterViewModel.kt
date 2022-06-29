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

    val memberAccountMsg: LiveData<Pair<String?, Boolean>>
        get() = _memberAccountMsg
    val loginPasswordMsg: LiveData<Pair<String?, Boolean>>
        get() = _loginPasswordMsg
    val confirmPasswordMsg: LiveData<Pair<String?, Boolean>>
        get() = _confirmPasswordMsg
    val fullNameMsg: LiveData<Pair<String?, Boolean>>
        get() = _fullNameMsg
    val fundPwdMsg: LiveData<Pair<String?, Boolean>>
        get() = _fundPwdMsg
    val qqMsg: LiveData<Pair<String?, Boolean>>
        get() = _qqMsg
    val phoneMsg: LiveData<Pair<String?, Boolean>>
        get() = _phoneMsg
    val emailMsg: LiveData<Pair<String?, Boolean>>
        get() = _emailMsg
    val postalMsg: LiveData<Pair<String?, Boolean>>
        get() = _postalMsg
    val provinceMsg: LiveData<Pair<String?, Boolean>>
        get() = _provinceMsg
    val cityMsg: LiveData<Pair<String?, Boolean>>
        get() = _cityMsg
    val addressMsg: LiveData<Pair<String?, Boolean>>
        get() = _addressMsg
    val weChatMsg: LiveData<Pair<String?, Boolean>>
        get() = _weChatMsg
    val zaloMsg: LiveData<Pair<String?, Boolean>>
        get() = _zaloMsg
    val facebookMsg: LiveData<Pair<String?, Boolean>>
        get() = _facebookMsg
    val whatsAppMsg: LiveData<Pair<String?, Boolean>>
        get() = _whatsAppMsg
    val telegramMsg: LiveData<Pair<String?, Boolean>>
        get() = _telegramMsg
    val securityPbMsg: LiveData<Pair<String?, Boolean>>
        get() = _securityPbMsg
    val securityCodeMsg: LiveData<Pair<String?, Boolean>>
        get() = _securityCodeMsg
    val validCodeMsg: LiveData<Pair<String?, Boolean>>
        get() = _validCodeMsg

    val registerEnable: LiveData<Boolean>
        get() = _registerEnable
    val validCodeResult: LiveData<ValidCodeResult?>
        get() = _validCodeResult
    val smsResult: LiveData<SmsResult?>
        get() = _smsResult
    val loginForGuestResult: LiveData<LoginResult>
        get() = _loginForGuestResult


    private val cbAgreeAllChecked: LiveData<Boolean?>
        get() = _cbAgreeAllChecked


    private val _registerResult = MutableLiveData<LoginResult>()
    private val _inviteCodeMsg = MutableLiveData<String?>()
    private val _memberAccountMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _loginPasswordMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _confirmPasswordMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _fullNameMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _fundPwdMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _qqMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _phoneMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _emailMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _postalMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _provinceMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _cityMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _addressMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _weChatMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _zaloMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _facebookMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _whatsAppMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _telegramMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _securityPbMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _securityCodeMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _validCodeMsg = MutableLiveData<Pair<String?, Boolean>>()

    private val _registerEnable = MutableLiveData<Boolean>()

    private val _validCodeResult = MutableLiveData<ValidCodeResult?>()
    private val _smsResult = MutableLiveData<SmsResult?>()
    private val _loginForGuestResult = MutableLiveData<LoginResult>()

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

    fun checkInviteCode(inviteCode: String?) {
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

    fun checkMemberAccount(account: String?, isExistAccount: Boolean) {
        val msg = when {
            account.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            isExistAccount -> androidContext.getString(R.string.error_register_id_exist)
            !VerifyConstUtil.verifyCombinationAccount(account) -> {
                androidContext.getString(R.string.error_member_account)
            }
            !VerifyConstUtil.verifyAccount(account) -> androidContext.getString(R.string.error_member_account)
            else -> null
        }
        _memberAccountMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkLoginPassword(password: String?) {
        val msg = when {
            password.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPwdFormat(password) -> androidContext.getString(R.string.error_register_password)
            password.length !in 6..20 -> androidContext.getString(R.string.error_register_password)
            !VerifyConstUtil.verifyPwd(password) -> androidContext.getString(R.string.error_register_password)
            else -> null
        }
        _loginPasswordMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkConfirmPassword(password: String?, confirmPassword: String?) {
        val msg = when {
            password.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            password != confirmPassword -> androidContext.getString(R.string.error_confirm_password)
            else -> null
        }
        _confirmPasswordMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkFullName(fullName: String?) {
        val msg = when {
            fullName.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyFullName(fullName) -> androidContext.getString(R.string.error_input_has_blank)
            else -> null
        }
        _fullNameMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkFundPwd(fundPwd: String?) {
        val msg = when {
            fundPwd.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPayPwd(fundPwd) -> androidContext.getString(R.string.error_withdrawal_pwd)
            else -> null
        }
        _fundPwdMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkQQ(qq: String?) {
        val msg = when {
            qq.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyQQ(qq) -> androidContext.getString(R.string.error_qq_number)
            else -> null
        }
        _qqMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkPhone(phone: String?) {
        val msg = when {
            phone.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        _phoneMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkEmail(email: String?) {
        val msg = when {
            email.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyMail(email) -> androidContext.getString(R.string.error_e_mail)
            else -> null
        }
        _emailMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkPostal(postalCode: String?) {
        val msg = when {
            postalCode.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        _postalMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkProvince(province: String?) {
        val msg = when {
            province.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        _provinceMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkCity(city: String?) {
        val msg = when {
            city.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        _cityMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkAddress(address: String?) {
        val msg = when {
            address.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        _addressMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkWeChat(weChat: String?) {
        val msg = when {
            weChat.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        _weChatMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkZalo(zalo: String?) {
        val msg = when {
            zalo.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyZalo(zalo) -> androidContext.getString(R.string.error_zalo)
            else -> null
        }
        _zaloMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkFacebook(facebook: String?) {
        val msg = when {
            facebook.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyFacebook(facebook) -> androidContext.getString(R.string.error_facebook)
            else -> null
        }
        _facebookMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkWhatsApp(whatsApp: String?) {
        val msg = when {
            whatsApp.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyWhatsApp(whatsApp) -> androidContext.getString(R.string.error_whats_app)
            else -> null
        }
        _whatsAppMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkTelegram(telegram: String?) {
        val msg = when {
            telegram.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyTelegram(telegram) -> androidContext.getString(R.string.error_telegram)
            else -> null
        }
        _telegramMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkSecurityPb(securityPb: String?) {
        val msg = when {
            securityPb.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        _securityPbMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkSecurityCode(securityCode: String?) {
        val msg = when {
            securityCode.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifySecurityCode(securityCode) -> androidContext.getString(R.string.error_verification_code_by_sms)
            else -> null
        }
        _securityCodeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
    }

    fun checkValidCode(validCode: String?) {
        val msg = when {
            validCode.isNullOrEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyValidCode(validCode) -> androidContext.getString(R.string.error_verification_code)
            else -> null
        }
        _validCodeMsg.value = Pair(msg, msg == null)
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


    fun checkCbAgreeAll(checked: Boolean?) {
        _cbAgreeAllChecked.value = checked
        focusChangeCheckAllInputComplete()
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

        checkCbAgreeAll(cbAgreeAllChecked)

        return checkAllInputComplete()
    }

    private fun focusChangeCheckAllInputComplete() {
        _registerEnable.value = checkAllInputComplete()
    }

    private fun checkAllInputComplete(): Boolean {
        if (sConfigData?.enableInviteCode == FLAG_OPEN && inviteCodeMsg.value != null) {
            return false
        }
        if (checkInputPair(memberAccountMsg)) {
            return false
        }
        if (checkInputPair(loginPasswordMsg)) {
            return false
        }
        if (checkInputPair(confirmPasswordMsg)) {
            return false
        }
        if (sConfigData?.enableFullName == FLAG_OPEN && checkInputPair(fullNameMsg))
            return false
        if (sConfigData?.enableFundPwd == FLAG_OPEN && checkInputPair(fundPwdMsg))
            return false
        if (sConfigData?.enableQQ == FLAG_OPEN && checkInputPair(qqMsg))
            return false
        if (sConfigData?.enablePhone == FLAG_OPEN && checkInputPair(phoneMsg))
            return false
        if (sConfigData?.enableEmail == FLAG_OPEN && checkInputPair(emailMsg))
            return false
        if (sConfigData?.enableAddress == FLAG_OPEN && (checkInputPair(postalMsg) || checkInputPair(provinceMsg) || checkInputPair(cityMsg) || checkInputPair(addressMsg)))
            return false
        if (sConfigData?.enableWechat == FLAG_OPEN && checkInputPair(weChatMsg))
            return false
        if (sConfigData?.enableZalo == FLAG_OPEN && checkInputPair(zaloMsg))
            return false
        if (sConfigData?.enableFacebook == FLAG_OPEN && checkInputPair(facebookMsg))
            return false
        if (sConfigData?.enableWhatsApp == FLAG_OPEN && checkInputPair(whatsAppMsg))
            return false
        if (sConfigData?.enableTelegram == FLAG_OPEN && checkInputPair(telegramMsg))
            return false
        if (sConfigData?.enableSafeQuestion == FLAG_OPEN && checkInputPair(securityPbMsg))
            return false
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN && checkInputPair(securityCodeMsg))
            return false
        if (sConfigData?.enableRegValidCode == FLAG_OPEN && checkInputPair(validCodeMsg))
            return false

        if (cbAgreeAllChecked.value != true)
            return false

        return true
    }

    private fun checkInputPair(data: LiveData<Pair<String?, Boolean>>): Boolean {
        return data.value?.first != null || data.value?.second != true
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
        //舊 檢查中字樣 (不過會有消不掉的疑慮 h5沒有此)
//        val msg = androidContext.getString(R.string.desc_register_checking_account)
//        _memberAccountMsg.value = Pair(msg, false)
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.indexService.checkAccountExist(account)
            }.let {
                if(it?.success == true) {
                    checkMemberAccount(account, it.isExist ?: false)
                }else {
                    checkMemberAccount(account, false)
                }
            }
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