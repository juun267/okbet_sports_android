package org.cxct.sportlottery.ui.login.signUp

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.OneBoSportApi.bettingStationService
import org.cxct.sportlottery.network.index.chechBetting.CheckBettingResult
import org.cxct.sportlottery.network.index.checkAccount.CheckAccountResult
import org.cxct.sportlottery.network.index.config.Currency
import org.cxct.sportlottery.network.index.config.NationCurrency
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.register.RegisterRequest
import org.cxct.sportlottery.network.index.sendSms.SmsRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.network.uploadImg.UploadImgResult
import org.cxct.sportlottery.network.uploadImg.UploadVerifyDocRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.*
import java.io.File

class RegisterViewModel(
    androidContext: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    userInfoRepository: UserInfoRepository,
    favoriteRepository: MyFavoriteRepository
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    val registerResult: LiveData<LoginResult>
        get() = _registerResult
    val inviteCodeMsg: LiveData<String?>
        get() = _inviteCodeMsg

    val memberAccountMsg: LiveData<Pair<String?, Boolean>>
        get() = _memberAccountMsg
    val checkAccountMsg: LiveData<CheckAccountResult>
        get() = _checkAccountMsg

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
    val salaryMsg: LiveData<Pair<String?, Boolean>>
        get() = _salaryMsg
    val birthMsg: LiveData<Pair<String?, Boolean>>
        get() = _birthMsg
    val identityMsg: LiveData<Pair<String?, Boolean>>
        get() = _identityMsg
    val identityBackupMsg: LiveData<Pair<String?, Boolean>>
        get() = _identityBackupMsg
    val identityTypeMsg: LiveData<Pair<String?, Boolean>>
        get() = _identityTypeMsg
    val identityBackupTypeMsg: LiveData<Pair<String?, Boolean>>
        get() = _identityBackupTypeMsg
    val eetIdentityNumber: LiveData<Pair<String?, Boolean>>
        get() = _eetIdentityNumber
    val eetIdentityBackupNumber: LiveData<Pair<String?, Boolean>>
        get() = _eetIdentityBackupNumber
    val bettingShopMsg: LiveData<Pair<String?, Boolean>>
        get() = _bettingShopMsg
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
    val nationMsg: LiveData<Pair<String?, Boolean>>
        get() = _nationMsg
    val currencyMsg: LiveData<Pair<String?, Boolean>>
        get() = _currencyMsg

    val registerEnable: LiveData<Boolean>
        get() = _registerEnable
    val validCodeResult: LiveData<ValidCodeResult?>
        get() = _validCodeResult

    val smsResult: LiveData<NetResult?>
        get() = _smsResult
    val loginForGuestResult: LiveData<LoginResult>
        get() = _loginForGuestResult

    val bettingStationList: LiveData<List<StatusSheetData>>
        get() = _bettingStationList


    private val cbAgreeAllChecked: LiveData<Boolean?>
        get() = _cbAgreeAllChecked

    val checkBettingResult: LiveData<CheckBettingResult?>
        get() = _checkBettingResult

    private val _registerResult = MutableLiveData<LoginResult>()
    private val _inviteCodeMsg = MutableLiveData<String?>()
    private val _memberAccountMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _checkAccountMsg = MutableLiveData<CheckAccountResult>()
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
    private val _salaryMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _birthMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _identityMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _identityBackupMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _identityTypeMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _identityBackupTypeMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _eetIdentityNumber = MutableLiveData<Pair<String?, Boolean>>()
    private val _eetIdentityBackupNumber = MutableLiveData<Pair<String?, Boolean>>()
    private val _bettingShopMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _weChatMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _zaloMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _facebookMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _whatsAppMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _telegramMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _securityPbMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _securityCodeMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _validCodeMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _nationMsg = MutableLiveData<Pair<String?, Boolean>>()
    private val _currencyMsg = MutableLiveData<Pair<String?, Boolean>>()

    private val _registerEnable = MutableLiveData<Boolean>()

    private val _validCodeResult = MutableLiveData<ValidCodeResult?>()
    private val _smsResult = MutableLiveData<NetResult?>()
    private val _loginForGuestResult = MutableLiveData<LoginResult>()

    private val _cbAgreeAllChecked = MutableLiveData<Boolean?>()

    private val _bettingStationList = MutableLiveData<List<StatusSheetData>>()

    private val _checkBettingResult = MutableLiveData<CheckBettingResult?>()

    //region 證件照片
    val docUrlResult: LiveData<UploadImgResult?>
        get() = _docUrlResult
    private val _docUrlResult = MutableLiveData<UploadImgResult?>()

    val photoUrlResult: LiveData<UploadImgResult?>
        get() = _photoUrlResult
    private val _photoUrlResult = MutableLiveData<UploadImgResult?>()

    //endregion
    val identityPhoto: LiveData<File?>
        get() = _identityPhoto
    private val _identityPhoto = MutableLiveData<File?>()

    val identityPhotoBackup: LiveData<File?>
        get() = _identityPhotoBackup
    private val _identityPhotoBackup = MutableLiveData<File?>()

    //region 國家幣種
    val nationCurrencyList: LiveData<List<NationCurrency>?>
        get() = _nationCurrencyList
    private var _nationCurrencyList = MutableLiveData<List<NationCurrency>?>()

    val currencyList: LiveData<List<Currency>?>
        get() = _currencyList
    private val _currencyList = MutableLiveData<List<Currency>?>()

    val nationPhoneCode: LiveData<String>
        get() = _nationPhoneCode
    private var _nationPhoneCode = MutableLiveData<String>()

    //endregion

    fun getNationCurrencyList() {
        //有開啟國家幣種選項時才去獲取相關資料
        if (sConfigData?.enableNationCurrency == FLAG_OPEN) {
            //不複製一份的話選中狀態會被記錄著
            _nationCurrencyList.value = sConfigData?.nationCurrencyList?.map { it.copy() }

            _currencyList.value = nationCurrencyList.value?.firstOrNull()?.currencyList
        }
    }

    fun updateNationCurrency(nationCode: String) {
        _currencyList.value = nationCurrencyList.value?.firstOrNull { it.nationCode == nationCode }?.currencyList
    }

    fun updateNationPhoneCode(nationCode: String) {
        _nationPhoneCode.value = nationCurrencyList.value?.firstOrNull { it.nationCode == nationCode }?.phoneCode ?: ""
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
                    LocalUtils.getString(R.string.error_input_empty)
            }
            !VerifyConstUtil.verifyInviteCode(inviteCode) -> LocalUtils.getString(if (sConfigData?.enableBettingStation == FLAG_OPEN) R.string.error_recommend_code else R.string.error_recommend_agent)
            else -> null
        }
        focusChangeCheckAllInputComplete(1)
    }

    fun checkMemberAccount(account: String?) {
        val msg = when {
            account.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyCombinationAccount(account) -> {
                LocalUtils.getString(R.string.error_member_account)
            }
            !VerifyConstUtil.verifyAccount(account) -> LocalUtils.getString(R.string.error_member_account)
            else -> null
        }
        _memberAccountMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(1)
    }

    fun checkLoginPassword(password: String?, confirmPassword: String? = null) {
        val msg = when {
            password.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPwdFormat(password) -> LocalUtils.getString(R.string.error_register_password)
            password.length !in 6..20 -> LocalUtils.getString(R.string.error_register_password)
            !VerifyConstUtil.verifyPwd(password) -> LocalUtils.getString(R.string.error_register_password)
            else -> null
        }

        if(confirmPassword?.isNotEmpty() == true)
            checkConfirmPassword(password, confirmPassword)

        _loginPasswordMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(1)
    }

    fun checkConfirmPassword(password: String?, confirmPassword: String?) {
        val msg = when {
            password.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            password != confirmPassword -> LocalUtils.getString(R.string.error_confirm_password)
            else -> null
        }
        _confirmPasswordMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(1)
    }

    fun checkFullName(fullName: String?) {
        val msg = when {
            fullName.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyFullName(fullName) -> LocalUtils.getString(R.string.error_full_name)
            else -> null
        }
        _fullNameMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkFundPwd(fundPwd: String?) {
        val msg = when {
            fundPwd.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPayPwd(fundPwd) -> LocalUtils.getString(R.string.error_withdraw_password)
            else -> null
        }
        _fundPwdMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkQQ(qq: String?) {
        val msg = when {
            qq.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyQQ(qq) -> LocalUtils.getString(R.string.error_qq_number)
            else -> null
        }
        _qqMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkPhone(phone: String?) {
        val msg = when {
            phone.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
           !VerifyConstUtil.verifyPhone(phone) -> LocalUtils.getString(R.string.error_phone_number)
            else -> null
        }
        _phoneMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkEmail(email: String?) {
        val msg = when {
            email.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyMail(email) -> LocalUtils.getString(R.string.error_e_mail)
            else -> null
        }
        _emailMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkPostal(postalCode: String?) {
        val msg = when {
            postalCode.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _postalMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkProvince(province: String?) {
        val msg = when {
            province.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _provinceMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkCity(city: String?) {
        val msg = when {
            city.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _cityMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkAddress(address: String?) {
        val msg = when {
            address.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _addressMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkWeChat(weChat: String?) {
        val msg = when {
            weChat.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyWeChat(weChat) -> LocalUtils.getString(R.string.error_wechat)
            else -> null
        }
        _weChatMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkZalo(zalo: String?) {
        val msg = when {
            zalo.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyZalo(zalo) -> LocalUtils.getString(R.string.error_zalo)
            else -> null
        }
        _zaloMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkFacebook(facebook: String?) {
        val msg = when {
            facebook.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyFacebook(facebook) -> LocalUtils.getString(R.string.error_facebook)
            else -> null
        }
        _facebookMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkWhatsApp(whatsApp: String?) {
        val msg = when {
            whatsApp.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyWhatsApp(whatsApp) -> LocalUtils.getString(R.string.error_whats_app)
            else -> null
        }
        _whatsAppMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkTelegram(telegram: String?) {
        val msg = when {
            telegram.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyTelegram(telegram) -> LocalUtils.getString(R.string.error_telegram)
            else -> null
        }
        _telegramMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkSecurityPb(securityPb: String?) {
        val msg = when {
            securityPb.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _securityPbMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkSecurityCode(securityCode: String?) {
        val msg = when {
            securityCode.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifySecurityCode(securityCode) -> LocalUtils.getString(R.string.error_verification_code_by_sms)
            else -> null
        }
        _securityCodeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkValidCode(validCode: String?) {
        val msg = when {
            validCode.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyValidCode(validCode) -> LocalUtils.getString(R.string.error_verification_code)
            else -> null
        }
        _validCodeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
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

    fun queryPlatform(inviteCode: String) {

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.bettingStationService.queryPlatform(inviteCode)
            }
            _checkBettingResult.postValue(result)

            //投注站與國家幣種選擇互斥, 僅會存在其中一種類型, 若投注站關閉則根據回傳的國家幣種去配置
            if (sConfigData?.enableBettingStation != FLAG_OPEN) {
                if (result?.success == true) {
                    setupInviteNationCurrency(result)
                } else {
                    _inviteCodeMsg.value = result?.msg
                    focusChangeCheckAllInputComplete(1)
                }
            }
        }
    }

    /**
     * 根據邀請碼配置國家、幣種欄位
     */
    private fun setupInviteNationCurrency(result: CheckBettingResult) {
        var inviteNation: NationCurrency? = null
        var inviteCurrency: Currency? = null
        result.checkBettingData?.nationCode?.let { inviteNationCode ->
            result.checkBettingData.currency?.let { inviteCurrencyCode ->
                inviteNation = nationCurrencyList.value?.firstOrNull { nationCurrency ->
                    nationCurrency.nationCode == inviteNationCode
                }

                inviteCurrency = inviteNation?.currencyList?.firstOrNull { configCurrency ->
                    configCurrency.currency == inviteCurrencyCode
                }
            }
        }

        //選中邀請碼配置的國家清單
        var inviteNationCurrencyList: List<NationCurrency>? = null
        inviteNation?.let { nationCurrency ->
            inviteNationCurrencyList =
                nationCurrencyList.value?.onEach { it.isSelected = it.nationCode == nationCurrency.nationCode }
        }

        //選中邀請碼配置的幣種清單
        var inviteCurrencyList: List<Currency>? = null
        var inviteNationPhoneCode: String? = ""
        inviteCurrency?.let { inviteCurrencyNotNull ->
            inviteCurrencyList = inviteNationCurrencyList?.firstOrNull { it.isSelected }
                ?.apply { phoneCode?.let { inviteNationPhoneCode = it } }?.currencyList?.onEach {
                    it.isSelected = it.currency == inviteCurrencyNotNull.currency
                }
        }

        viewModelScope.launch(Dispatchers.Main) {
            inviteNationCurrencyList?.let {
                _nationCurrencyList.value = it
                _nationPhoneCode.value = inviteNationPhoneCode ?: ""
            }

            inviteCurrencyList?.let {
                _currencyList.value = it
            }
        }
    }

    fun checkCbAgreeAll(checked: Boolean?) {
        _cbAgreeAllChecked.value = checked
        focusChangeCheckAllInputComplete(1)
    }

    fun checkBirth(birth: String?) {
        val msg = when {
            birth.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _birthMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkIdentity(firstFile: File?) {
        val msg = when (firstFile) {
            null -> {
                LocalUtils.getString(R.string.error_identity_photo)
            }
            else -> {
                null
            }
        }
        _identityMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkIdentity(identity: String?) {
        val msg = when {
            identity.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            photoUrlResult.value == null -> LocalUtils.getString(R.string.error_identity_photo)
            else -> null
        }
        _identityMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkBackupIdentity(secondFile: File?) {
        val msg = when (secondFile) {
            null -> {
                LocalUtils.getString(R.string.error_identity_photo)
            }
            else -> {
                null
            }
        }
        _identityBackupMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkIdentityType(identityType: String?) {
        val msg = when {
            identityType.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _identityTypeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkIdentityBackupType(identityType: String?) {
        val msg = when {
            identityType.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _identityBackupTypeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkIdentityNumber(identityNumber: String?) {
        val msg = when {
            identityNumber.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _eetIdentityNumber.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkIdentityBackupNumber(identityNumber2: String?) {
        val msg = when {
            identityNumber2.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _eetIdentityBackupNumber.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }


    fun checkSalary(salary: String?) {
        val msg = when {
            salary.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _salaryMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun checkBettingShop(bettingShop: String?) {
        val msg = when {
            bettingShop.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _bettingShopMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(3)
    }

    fun checkNation(nationCode: String?) {
        val msg = when {
            nationCode.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _nationMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(-1) //國家選項不會出現在有分頁式的註冊頁
    }

    fun checkCurrency(currency: String?) {
        val msg = when {
            currency.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _currencyMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(-1) // 幣種選項不會出現在有分頁式的註冊頁
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
        birth: String?,
        identity: String?,
        identityType: String?,
        salarySource: String?,
        bettingShop: String?,
        nationCode: String?,
        currency: String?,
        firstFile: File?,
        secndFile: File?,
        identityNumber: String?,
        identityNumberBackup: String?,
        identityTypeBackup: String?
    ): Boolean {
        if (sConfigData?.enableInviteCode == FLAG_OPEN)
            checkInviteCode(inviteCode)

        checkMemberAccount(userName)
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
        if (sConfigData?.enableAddress == FLAG_OPEN) {
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
        if (sConfigData?.enableSafeQuestion == FLAG_OPEN)
            checkSecurityPb(securityPb)
        if (sConfigData?.enableSmsValidCode == FLAG_OPEN)
            checkSecurityCode(smsCode)
        if (sConfigData?.enableRegValidCode == FLAG_OPEN)
            checkValidCode(validCode)
        if (sConfigData?.enableBirthday == FLAG_OPEN)
            checkBirth(birth)
        if (sConfigData?.enableKYCVerify == FLAG_OPEN) {
            checkIdentity(firstFile)
            checkIdentityNumber(identityNumber)
            checkIdentityType(identityType)
        }
        if (sConfigData?.enableKYCVerify == FLAG_OPEN && sConfigData?.idUploadNumber.equals("2")) {
            checkBackupIdentity(secndFile)
            checkIdentityBackupNumber(identityNumberBackup)
            checkIdentityBackupType(identityTypeBackup)
        }
        if (!isOKPlat()) {
            if (sConfigData?.enableIdentityNumber == FLAG_OPEN)
                checkIdentity(identity)
            if (sConfigData?.enableIdentityNumber == FLAG_OPEN)
                checkIdentityType(identityType)
        }
        if (sConfigData?.enableSalarySource == FLAG_OPEN)
            checkSalary(salarySource)
        if (sConfigData?.enableBettingStation == FLAG_OPEN)
            checkBettingShop(bettingShop)
        if (sConfigData?.enableNationCurrency == FLAG_OPEN) {
            checkNation(nationCode)
            checkCurrency(currency)
        }

        checkCbAgreeAll(cbAgreeAllChecked)

        return checkAllInputComplete(3)
    }

     fun focusChangeCheckAllInputComplete(page: Int) {
         _registerEnable.value = checkAllInputComplete(page)
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
        if (sConfigData?.enableAddress == FLAG_OPEN && (checkInputPair(postalMsg) || checkInputPair(
                provinceMsg
            ) || checkInputPair(cityMsg) || checkInputPair(addressMsg))
        )
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
        if (sConfigData?.enableBirthday == FLAG_OPEN && checkInputPair(birthMsg))
            return false
        if (sConfigData?.enableIdentityNumber == FLAG_OPEN && checkInputPair(identityMsg))
            return false
        if (sConfigData?.enableIdentityNumber == FLAG_OPEN && checkInputPair(identityTypeMsg))
            return false
        if (sConfigData?.enableSalarySource == FLAG_OPEN && checkInputPair(salaryMsg))
            return false
        if (sConfigData?.enableBettingStation == FLAG_OPEN && checkInputPair(bettingShopMsg))
            return false
        if (sConfigData?.enableNationCurrency == FLAG_OPEN && (checkInputPair(nationMsg) || checkInputPair(currencyMsg)))
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

    private fun checkAllInputComplete(page: Int): Boolean {
            when (page) {
                1 -> {
                    if (inviteCodeMsg.value != null) {
                        return false
                    }
                    if (checkInputPair(memberAccountMsg)) {
                        return false
                    }
                    if (checkAccountMsg.value?.isExist == true) {
                        return false
                    }
                    if (checkInputPair(loginPasswordMsg)) {
                        return false
                    }
                    if (checkInputPair(confirmPasswordMsg)) {
                        return false
                    }
                    if (cbAgreeAllChecked.value != true)
                        return false
                }

                2 -> {
                    if (sConfigData?.enableFullName == FLAG_OPEN && checkInputPair(fullNameMsg))
                        return false
                    if (sConfigData?.enableFundPwd == FLAG_OPEN && checkInputPair(fundPwdMsg))
                        return false
                    if (sConfigData?.enablePhone == FLAG_OPEN && checkInputPair(phoneMsg))
                        return false
                    if (sConfigData?.enableSmsValidCode == FLAG_OPEN && checkInputPair(
                            securityCodeMsg)
                    )
                        return false
                    if (sConfigData?.enableBirthday == FLAG_OPEN && checkInputPair(birthMsg))
                        return false
                    if (sConfigData?.enableSalarySource == FLAG_OPEN && checkInputPair(salaryMsg))
                        return false
                    if (sConfigData?.enableKYCVerify == FLAG_OPEN && checkInputPair(identityTypeMsg))
                        return false
                    if (sConfigData?.enableKYCVerify == FLAG_OPEN && checkInputPair(
                            eetIdentityNumber)
                    )
                        return false
                    if (sConfigData?.enableKYCVerify == FLAG_OPEN && checkInputPair(identityMsg))
                        return false

                    if (sConfigData?.enableKYCVerify == FLAG_OPEN && sConfigData?.idUploadNumber.equals(
                            "2") && checkInputPair(identityBackupTypeMsg)
                    )
                        return false
                    if (sConfigData?.enableKYCVerify == FLAG_OPEN && sConfigData?.idUploadNumber.equals(
                            "2") && checkInputPair(eetIdentityBackupNumber)
                    )
                        return false
                    if (sConfigData?.enableKYCVerify == FLAG_OPEN && sConfigData?.idUploadNumber.equals(
                            "2") && checkInputPair(identityBackupMsg)
                    )
                        return false
                }
                else -> {
                    if (sConfigData?.enableBettingStation == FLAG_OPEN && checkInputPair(
                            bettingShopMsg)
                    )
                        return false
                    if (sConfigData?.enableEmail == FLAG_OPEN && checkInputPair(emailMsg))
                        return false
                    if (sConfigData?.enableAddress == FLAG_OPEN && (checkInputPair(postalMsg) || checkInputPair(
                            provinceMsg
                        ) || checkInputPair(cityMsg) || checkInputPair(addressMsg))
                    )
                        return false
                    if (sConfigData?.enableQQ == FLAG_OPEN && checkInputPair(qqMsg))
                        return false
                    if (sConfigData?.enableRegValidCode == FLAG_OPEN && checkInputPair(validCodeMsg))
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
                }
            }


        return true
    }

    fun checkAccountExist(account: String) {
        //舊 檢查中字樣 (不過會有消不掉的疑慮 h5沒有此)
//        val msg = LocalUtils.getString(R.string.desc_register_checking_account)
//        _memberAccountMsg.value = Pair(msg, false)
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.indexService.checkAccountExist(account)
            }.let {
                _checkAccountMsg.value = it

                focusChangeCheckAllInputComplete(1)

//            if (it?.success == true) {
//                checkMemberAccount(account, it.isExist ?: false)
//            } else {
//                checkMemberAccount(account, false)
//            }
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
        securityPbTypeCode: String?,
        securityPb: String,
        smsCode: String,
        validCode: String,
        cbAgreeAllChecked: Boolean,
        deviceSn: String,
        deviceId: String,
        birth: String?,
        identity: String?,
        salarySource: String?,
        bettingShop: String?,
        nationCode: String?,
        currency: String?,
        firstFile: File? = null,
        identityType: String? = null,
        identityNumber: String? = null,
        secndFile: File? = null,
        identityTypeBackup: String? = null,
        identityNumberBackup: String? = null
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
                birth,
                identity,
                identityType,
                salarySource,
                bettingShop,
                nationCode,
                currency,
                firstFile,
                secndFile,
                identityNumber,
                identityNumberBackup,
                identityTypeBackup
            )
        ) {
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
                    securityPbTypeCode,
                    securityPb,
                    smsCode,
                    validCode,
                    deviceSn,
                    deviceId,
                    birth,
                    identity,
                    docUrlResult.value?.imgData?.path,
                    photoUrlResult.value?.imgData?.path,
                    salarySource,
                    bettingShop,
                    firstFile,
                    identityType?.toInt(),
                    identityNumber,
                    secndFile,
                    identityTypeBackup?.toInt(),
                    identityNumberBackup,
                    nationCode,
                    currency
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
        securityPbTypeCode: String?,
        securityPb: String,
        smsCode: String,
        validCode: String,
        deviceSn: String,
        deviceId: String,
        birth: String?,
        identity: String?,
        verifyPhoto1: String?,
        verifyPhoto2: String?,
        salarySource: String?,
        bettingShop: String?,
        identityPhoto: File?,
        identityType: Int?,
        identityNumber: String?,
        identityPhotoBackup: File?,
        identityTypeBackup: Int?,
        identityNumberBackup: String?,
        nationCode: String?,
        currency: String?
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
            if (sConfigData?.enableSafeQuestion == FLAG_OPEN) {
                this.safeQuestionType = securityPbTypeCode
                this.safeQuestion = securityPb
            }
            if (sConfigData?.enableSmsValidCode == FLAG_OPEN)
                this.securityCode = smsCode
            if (sConfigData?.enableRegValidCode == FLAG_OPEN) {
                this.validCodeIdentity = validCodeResult.value?.validCodeData?.identity
                this.validCode = validCode
            }
            if (sConfigData?.enableBirthday == FLAG_OPEN)
                this.birthday = birth

            if (!isOKPlat()) {
                if (sConfigData?.enableIdentityNumber == FLAG_OPEN) {
                    this.identityType = identityType
                    this.identityNumber = identity
                    this.verifyPhoto1 = verifyPhoto1
                    this.verifyPhoto2 = verifyPhoto2
                }
            }
            if (sConfigData?.enableKYCVerify == FLAG_OPEN) {
                _identityPhoto.value = identityPhoto
                this.identityType = identityType
                this.identityNumber = identityNumber
            }
            if (sConfigData?.enableKYCVerify == FLAG_OPEN && sConfigData?.idUploadNumber.equals("2")) {
                _identityPhotoBackup.value = identityPhotoBackup
                this.identityTypeBackup = identityTypeBackup
                this.identityNumberBackup = identityNumberBackup
            }
            if (sConfigData?.enableSalarySource == FLAG_OPEN)
                this.salarySource = salarySource
            if (sConfigData?.enableBettingStation == FLAG_OPEN)
                this.bettingStationId = bettingShop
            if (sConfigData?.enableNationCurrency == FLAG_OPEN) {
                this.nationCode = nationCode
                this.currency = currency
            }
        }
    }

    private fun register(registerRequest: RegisterRequest) {
        viewModelScope.launch {
            if (_identityPhoto.value != null && registerRequest.identityNumber != null) {
                _identityPhoto.value?.let {
                    val docResponse = doNetwork(androidContext) {
                        OneBoSportApi.uploadImgService.uploadImg(
                            UploadVerifyDocRequest(
                                userInfo.value?.userId.toString(),
                                _identityPhoto.value!!
                            ).toPars()
                        )
                    }
                    when {
                        docResponse == null -> _docUrlResult.postValue(
                            UploadImgResult(
                                -1,
                                androidContext.getString(R.string.unknown_error),
                                false,
                                null
                            )
                        )
                        docResponse.success -> {
                            _docUrlResult.postValue(docResponse)
                            registerRequest.apply {
                                this.identityPhoto = docResponse.imgData?.path
                            }
                            if (_identityPhotoBackup != null && registerRequest.identityNumberBackup != null) {
                                val photoResponse = doNetwork(androidContext) {
                                    OneBoSportApi.uploadImgService.uploadImg(
                                        UploadVerifyDocRequest(
                                            userInfo.value?.userId.toString(),
                                            _identityPhotoBackup.value!!
                                        ).toPars()
                                    )
                                }
                                when {
                                    photoResponse == null -> _photoUrlResult.postValue(
                                        UploadImgResult(
                                            -1,
                                            androidContext.getString(R.string.unknown_error),
                                            false,
                                            null
                                        )
                                    )
                                    photoResponse.success -> {
                                        _photoUrlResult.postValue(photoResponse)
                                        registerRequest.apply {
                                            this.identityPhotoBackup = photoResponse.imgData?.path
                                        }
                                        doNetwork(androidContext) {
                                            loginRepository.register(registerRequest)
                                        }?.let { result ->
                                            // TODO 20220108 更新UserInfo by Hewie
                                            userInfoRepository.getUserInfo()
                                            _registerResult.postValue(result)
                                            _identityPhoto.postValue(null)
                                            _identityPhotoBackup.postValue(null)
                                            AFInAppEventUtil.register("username")
                                        }
                                    }
                                    else -> {
                                        val error =
                                            UploadImgResult(
                                                photoResponse.code,
                                                photoResponse.msg,
                                                photoResponse.success,
                                                null
                                            )
                                        _identityPhoto.postValue(null)
                                        _identityPhotoBackup.postValue(null)
                                        _photoUrlResult.postValue(error)
                                    }
                                }
                            } else {
                                doNetwork(androidContext) {
                                    loginRepository.register(registerRequest)
                                }?.let { result ->
                                    // TODO 20220108 更新UserInfo by Hewie
                                    userInfoRepository.getUserInfo()
                                    _registerResult.postValue(result)
                                    _identityPhoto.postValue(null)
                                    _identityPhotoBackup.postValue(null)
                                    AFInAppEventUtil.register("username")
                                }
                            }
                        }
                        else -> {
                            val error = UploadImgResult(
                                docResponse.code, docResponse.msg, docResponse.success, null
                            )
                            _docUrlResult.postValue(error)
                        }
                    }
                }
            } else {
                doNetwork(androidContext) {
                    loginRepository.register(registerRequest)
                }?.let { result ->
                    // TODO 20220108 更新UserInfo by Hewie
                    userInfoRepository.getUserInfo()
                    _registerResult.postValue(result)
                    AFInAppEventUtil.register("username")
                }
            }
        }
    }

    fun bettingStationQuery() {
        viewModelScope.launch(Dispatchers.IO) {
            doNetwork(androidContext) {
                bettingStationService.bettingStationsQuery()
            }?.let { result ->
                val bettingStationSheetList = mutableListOf<StatusSheetData>()
                result.bettingStationList.map { bettingStation ->
                    bettingStationSheetList.add(
                        StatusSheetData(
                            bettingStation.id.toString(),
                            bettingStation.name
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    _bettingStationList.value = bettingStationSheetList
                }
            }
        }
    }

    //region 證件照片
    fun uploadVerifyPhoto(docFile: File, photoFile: File) {
        viewModelScope.launch {
            val docResponse = doNetwork(androidContext) {
                OneBoSportApi.uploadImgService.uploadImg(
                    UploadVerifyDocRequest(
                        "9999",
                        docFile
                    ).toPars()
                )
            }
            when {
                docResponse == null -> _docUrlResult.postValue(
                    UploadImgResult(
                        -1,
                        LocalUtils.getString(R.string.unknown_error),
                        false,
                        null
                    )

                )
                //上傳第一張照片成功
                docResponse.success -> {
                    _docUrlResult.postValue(docResponse)

                    val photoResponse = doNetwork(androidContext) {
                        OneBoSportApi.uploadImgService.uploadImg(
                            UploadVerifyDocRequest(
                                "9999",
                                photoFile
                            ).toPars()
                        )
                    }
                    when {
                        photoResponse == null -> _photoUrlResult.postValue(
                            UploadImgResult(
                                -1,
                                LocalUtils.getString(R.string.unknown_error),
                                false,
                                null
                            )
                        )
                        //上傳第二張照片成功
                        photoResponse.success -> {
                            _photoUrlResult.postValue(photoResponse)
                        }
                        else -> {
                            val error =
                                UploadImgResult(
                                    photoResponse.code,
                                    photoResponse.msg,
                                    photoResponse.success,
                                    null
                                )
                            _photoUrlResult.postValue(error)
                        }
                    }
                }
                else -> {
                    val error = UploadImgResult(
                        docResponse.code, docResponse.msg, docResponse.success, null
                    )
                    _docUrlResult.postValue(error)
                }
            }
        }
    }

    /**
     * 清除照片上傳狀態
     */
    fun resetCredentialsStatus() {
        _docUrlResult.value = null
        _photoUrlResult.value = null
    }
//endregion
}