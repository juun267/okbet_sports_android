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
import org.cxct.sportlottery.network.index.checkAccount.CheckAccountResult
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.register.RegisterRequest
import org.cxct.sportlottery.network.index.sendSms.SmsRequest
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.FileUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.VerifyConstUtil

class RegisterViewModel(
    private val androidContext: Context,
    private val loginRepository: LoginRepository
) : BaseViewModel() {
    val registerFormState: LiveData<RegisterFormState>
        get() = _registerFormState
    val registerResult: LiveData<LoginResult>
        get() = _registerResult
    val validCodeResult: LiveData<ValidCodeResult?>
        get() = _validCodeResult
    val smsResult: LiveData<SmsResult?>
        get() = _smsResult
    val checkAccountResult: LiveData<CheckAccountResult?>
        get() = _checkAccountResult

    private val _registerFormState = MutableLiveData<RegisterFormState>()
    private val _registerResult = MutableLiveData<LoginResult>()
    private val _validCodeResult = MutableLiveData<ValidCodeResult?>()
    private val _smsResult = MutableLiveData<SmsResult?>()
    private val _checkAccountResult = MutableLiveData<CheckAccountResult?>()

    fun checkInputData(
        context: Context,
        inviteCode: String?,
        memberAccount: String?,
        isExistAccount: Boolean = false, //判斷是帳號是否註冊過
        loginPassword: String?,
        confirmPassword: String?,
        fullName: String?,
        fundPwd: String?,
        qq: String?,
        phone: String?,
        email: String?,
        weChat: String?,
        zalo: String?,
        facebook: String?,
        whatsApp: String?,
        telegram: String?,
        validCode: String?,
        securityCode: String?,
        checkAgreement: Boolean
    ): Boolean {
        val inviteCodeError: String? = checkInviteCode(context, inviteCode)
        val memberAccountError = checkMemberAccount(context, memberAccount, isExistAccount)
        val loginPasswordError = checkLoginPassword(context, loginPassword)
        val confirmPasswordError = checkConfirmPassword(context, loginPassword, confirmPassword)
        val fullNameError = checkFullName(context, fullName)
        val fundPwdError = checkFundPwd(context, fundPwd)
        val qqError = checkQQ(context, qq)
        val phoneError = checkPhone(context, phone)
        val emailError = checkEmail(context, email)
        val weChatError = checkWeChat(context, weChat)
        val zaloError = checkZalo(context, zalo)
        val facebookError = checkFacebook(context, facebook)
        val whatsAppError = checkWhatsApp(context, whatsApp)
        val telegramError = checkTelegram(context, telegram)
        val securityCodeError = checkSecurityCode(context, securityCode)
        val validCodeError = checkValidCode(context, validCode)
        val isDataValid = (sConfigData?.enableInviteCode != FLAG_OPEN || inviteCodeError == null) &&
                memberAccountError == null &&
                loginPasswordError == null &&
                confirmPasswordError == null &&
                (sConfigData?.enableFullName != FLAG_OPEN || fullNameError == null) &&
                (sConfigData?.enableFundPwd != FLAG_OPEN || fundPwdError == null) &&
                (sConfigData?.enableQQ != FLAG_OPEN || qqError == null) &&
                (sConfigData?.enablePhone != FLAG_OPEN || phoneError == null) &&
                (sConfigData?.enableEmail != FLAG_OPEN || emailError == null) &&
                (sConfigData?.enableWechat != FLAG_OPEN || weChatError == null) &&
                (sConfigData?.enableZalo != FLAG_OPEN || zaloError == null) &&
                (sConfigData?.enableFacebook != FLAG_OPEN || facebookError == null) &&
                (sConfigData?.enableWhatsApp != FLAG_OPEN || whatsAppError == null) &&
                (sConfigData?.enableTelegram != FLAG_OPEN || telegramError == null) &&
                (sConfigData?.enableSmsValidCode != FLAG_OPEN || securityCodeError == null) &&
                (sConfigData?.enableRegValidCode != FLAG_OPEN || validCodeError == null) &&
                checkAgreement

        _registerFormState.value = RegisterFormState(
            inviteCodeError = inviteCodeError,
            memberAccountError = memberAccountError,
            loginPasswordError = loginPasswordError,
            confirmPasswordError = confirmPasswordError,
            fullNameError = fullNameError,
            fundPwdError = fundPwdError,
            qqError = qqError,
            phoneError = phoneError,
            emailError = emailError,
            zaloError = zaloError,
            facebookError = facebookError,
            whatsAppError = whatsAppError,
            telegramError = telegramError,
            securityCodeError = securityCodeError,
            validCodeError = validCodeError,
            checkAgreement = checkAgreement,
            isDataValid = isDataValid
        )

        return isDataValid
    }

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

    private fun checkInviteCode(context: Context, inviteCode: String?): String? {
        return when {
            inviteCode.isNullOrBlank() -> context.getString(R.string.hint_recommend_code)
            else -> null
        }
    }

    private fun checkMemberAccount(context: Context, account: String?, isExistAccount: Boolean): String? {
        return when {
            account.isNullOrBlank() -> context.getString(R.string.error_account_empty)
            isExistAccount -> context.getString(R.string.error_register_id_exist)
            account.length !in 4..16 -> context.getString(R.string.error_member_account)
            !VerifyConstUtil.verifyAccount(account) -> context.getString(R.string.error_character_not_match)
            else -> null
        }
    }

    private fun checkLoginPassword(context: Context, password: String?): String? {
        return when {
            password.isNullOrBlank() -> context.getString(R.string.error_password_empty)
            password.length !in 6..20 -> context.getString(R.string.error_register_password)
            !VerifyConstUtil.verifyPwd(password) -> context.getString(R.string.error_character_not_match)
            else -> null
        }
    }

    private fun checkConfirmPassword(context: Context, password: String?, confirmPassword: String?): String? {
        return when {
            password != confirmPassword -> context.getString(R.string.error_confirm_password)
            else -> null
        }
    }

    private fun checkFullName(context: Context, fullName: String?): String? {
        return when {
            !VerifyConstUtil.verifyFullName(fullName ?: "") -> context.getString(R.string.error_character_not_match)
            else -> null
        }
    }

    private fun checkFundPwd(context: Context, fundPwd: String?): String? {
        return when {
            !VerifyConstUtil.verifyPayPwd(fundPwd ?: "") -> context.getString(R.string.hint_withdrawal_pwd)
            else -> null
        }
    }

    private fun checkQQ(context: Context, qq: String?): String? {
        return when {
            qq.isNullOrBlank() -> context.getString(R.string.hint_qq_number)
            !VerifyConstUtil.verifyQQ(qq) -> context.getString(R.string.error_input_format)
            else -> null
        }
    }

    private fun checkPhone(context: Context, phone: String?): String? {
        return when {
            phone.isNullOrBlank() -> context.getString(R.string.hint_phone_number)
            else -> null
        }
    }

    private fun checkEmail(context: Context, email: String?): String? {
        return when {
            email.isNullOrBlank() -> context.getString(R.string.hint_e_mail)
            !VerifyConstUtil.verifyMail(email) -> context.getString(R.string.error_input_format)
            else -> null
        }
    }

    private fun checkWeChat(context: Context, weChat: String?): String? {
        return when {
            weChat.isNullOrBlank() -> context.getString(R.string.hint_we_chat_number)
            !VerifyConstUtil.verifyWeChat(weChat) -> context.getString(R.string.error_input_format)
            else -> null
        }
    }

    private fun checkZalo(context: Context, zalo: String?): String? {
        return when {
            zalo.isNullOrBlank() -> context.getString(R.string.hint_zalo)
            else -> null
        }
    }

    private fun checkFacebook(context: Context, facebook: String?): String? {
        return when {
            facebook.isNullOrBlank() -> context.getString(R.string.hint_facebook)
            else -> null
        }
    }

    private fun checkWhatsApp(context: Context, whatsApp: String?): String? {
        return when {
            whatsApp.isNullOrBlank() -> context.getString(R.string.hint_whats_app)
            else -> null
        }
    }

    private fun checkTelegram(context: Context, telegram: String?): String? {
        return when {
            telegram.isNullOrBlank() -> context.getString(R.string.hint_telegram)
            !VerifyConstUtil.verifyTelegram(telegram) -> context.getString(R.string.error_input_format)
            else -> null
        }
    }

    private fun checkSecurityCode(context: Context, securityCode: String?): String? {
        return when {
            securityCode.isNullOrBlank() -> context.getString(R.string.hint_verification_code_by_sms)
            else -> null
        }
    }

    private fun checkValidCode(context: Context, validCode: String?): String? {
        return when {
            validCode.isNullOrBlank() -> context.getString(R.string.hint_verification_code)
            else -> null
        }
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

    fun getValidCode(identity: String?) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.getValidCode(ValidCodeRequest(identity))
            }
            _validCodeResult.postValue(result)
        }
    }

    fun checkAccountExist(account: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.checkAccountExist(account)
            }
            _checkAccountResult.postValue(result)
        }
    }

    fun register(registerRequest: RegisterRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.register(registerRequest)
            }?.let { result ->
                _registerResult.postValue(result)
            }
        }
    }
}