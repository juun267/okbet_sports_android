package org.cxct.sportlottery.ui.login.signIn

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.login.LoginCodeRequest
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.login.ValidateLoginDeviceSmsRequest
import org.cxct.sportlottery.network.index.logout.LogoutResult
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity.Companion.LOGIN_TYPE_PWD
import org.cxct.sportlottery.util.AFInAppEventUtil
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.VerifyConstUtil


class LoginViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    protected val userInfoRepository: UserInfoRepository
) : BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {
    val loginFormState: LiveData<LoginFormState>
        get() = _loginFormState
    val loginResult: LiveData<LoginResult>
        get() = _loginResult
    val loginSmsResult: LiveData<LogoutResult>
        get() = _loginSmsResult
    val validCodeResult: LiveData<ValidCodeResult?>
        get() = _validCodeResult
    val msgCodeResult: LiveData<SmsResult?>
        get() = _msgCodeResult
    val validResult: LiveData<LogoutResult>
        get() = _validResult
    val isLoading: LiveData<Boolean> //使用者餘額
        get() = _isLoading
    val inviteCodeMsg: LiveData<String?>
        get() = _inviteCodeMsg

    private val _isLoading = MutableLiveData<Boolean>()
    private val _loginFormState = MutableLiveData<LoginFormState>()
    private val _loginResult = MutableLiveData<LoginResult>()
    private val _loginSmsResult = MutableLiveData<LogoutResult>()
    private val _validCodeResult = MutableLiveData<ValidCodeResult?>()
    private val _validResult = MutableLiveData<LogoutResult>()
    private val _msgCodeResult = MutableLiveData<SmsResult?>()
    private val _inviteCodeMsg = MutableLiveData<String?>()

    val accountMsg: LiveData<Pair<String?, Boolean>>
        get() = _accountMsg
    private val _accountMsg = MutableLiveData<Pair<String?, Boolean>>()

    val msgCodeMsg: LiveData<Pair<String?, Boolean>>
        get() = _msgCodeMsg
    private val _msgCodeMsg = MutableLiveData<Pair<String?, Boolean>>()

    val userNameMsg: LiveData<Pair<String?, Boolean>>
        get() = _userNameMsg
    private val _userNameMsg = MutableLiveData<Pair<String?, Boolean>>()

    val passwordMsg: LiveData<Pair<String?, Boolean>>
        get() = _passwordMsg
    private val _passwordMsg = MutableLiveData<Pair<String?, Boolean>>()

    val validateCodeMsg: LiveData<Pair<String?, Boolean>>
        get() = _validateCodeMsg
    private val _validateCodeMsg = MutableLiveData<Pair<String?, Boolean>>()

    val loginEnable: LiveData<Boolean>
        get() = _loginEnable
    private val _loginEnable = MutableLiveData<Boolean>()

    val account by lazy { loginRepository.account }
    val password by lazy { loginRepository.password }

    var loginType = LOGIN_TYPE_PWD
        set(value) {
            field = value
            checkAllInputComplete()
        }

    var isRememberPWD
        get() = loginRepository.isRememberPWD
        set(value) {
            loginRepository.isRememberPWD = value
        }

    fun login(loginRequest: LoginRequest, originalPassword: String) {
        viewModelScope.launch {
            //預設存帳號
            loginRepository.account = loginRequest.account

            //勾選時記住密碼
//            loginRepository.password = if (loginRepository.isRememberPWD) originalPassword else null

            doNetwork(androidContext) {
                loginRepository.login(loginRequest)
            }?.let { result ->
                // TODO 20220108 更新UserInfo by Hewie
                //若已經驗證過則直接獲取最新的用戶資料, 未驗證需等待驗證後
                if (result.loginData?.deviceValidateStatus == 1)
                    userInfoRepository.getUserInfo()
//                result.loginData?.discount = 0.4f //後台修復中 測試用
                _loginResult.postValue(result)
                AFInAppEventUtil.login(result.loginData?.uid.toString())
            }
        }
    }

    fun loginOrReg(loginRequest: LoginRequest) {
        viewModelScope.launch {
            //預設存帳號
            loginRepository.account = loginRequest.account

            doNetwork(androidContext) {
                loginRepository.loginOrReg(loginRequest)
            }?.let { result ->
                userInfoRepository.getUserInfo()
                _loginResult.postValue(result)
                AFInAppEventUtil.login(result.loginData?.uid.toString())
            }
        }
    }

    fun loginGoogle(token: String) {
        loading()
        viewModelScope.launch {
            //預設存帳號
            doNetwork(androidContext) {
                loginRepository.googleLogin(token)
            }?.let { result ->
                userInfoRepository.getUserInfo()
                _loginResult.postValue(result)
                AFInAppEventUtil.login(result.loginData?.uid.toString())
                hideLoading()
            }
        }
    }

    fun loginFacebook(token: String) {
        loading()
        viewModelScope.launch {
            //預設存帳號
            doNetwork(androidContext) {
                loginRepository.facebookLogin(token)
            }?.let { result ->
                userInfoRepository.getUserInfo()
                _loginResult.postValue(result)
                AFInAppEventUtil.login(result.loginData?.uid.toString())
                hideLoading()
            }
        }
    }

    fun sendLoginDeviceSms(token: String) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.sendLoginDeviceSms(token)
            }?.let { result ->
                _loginSmsResult.postValue(result)
            }
        }
    }

    fun validateLoginDeviceSms(token: String, code: String, deviceId: String) {

        val validateRequest = ValidateLoginDeviceSmsRequest(
            loginEnvInfo = deviceId,
            validCode = code,
            loginSrc = LOGIN_SRC
        )

        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.validateLoginDeviceSms(token, validateRequest)
            }?.let { result ->
                //手機驗證成功後, 獲取最新的用戶資料
                if (result.success) {
                    userInfoRepository.getUserInfo()
                }
                _validResult.postValue(result)
            }
        }
    }


    suspend fun getUserPhone(): String? {
        return withContext(Dispatchers.IO) {
            userInfoRepository.userInfo?.value?.phone.toString()
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.loginForGuest()
            }?.let {
                _loginResult.value = it
            }
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

    fun loginOrRegSendValidCode(loginCodeRequest: LoginCodeRequest) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.loginOrRegSendValidCode(loginCodeRequest)
            }
            _msgCodeResult.postValue(result)
        }
    }

    /**
     * 输入邀请码
     */
    fun checkInviteCode(inviteCode: String?) {
        _inviteCodeMsg.value = when {
            inviteCode.isNullOrEmpty() -> {
                    LocalUtils.getString(R.string.error_input_empty)
            }
            !VerifyConstUtil.verifyInviteCode(inviteCode) -> LocalUtils.getString(if (sConfigData?.enableBettingStation == FLAG_OPEN) R.string.error_recommend_code else R.string.error_recommend_agent)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    /**
     * 手机号/邮箱
     */
    fun checkAccount(username: String): String? {
        val msg = when {
            username.isBlank() -> LocalUtils.getString(R.string.error_input_empty)
            !(VerifyConstUtil.verifyPhone(username) || VerifyConstUtil.verifyMail(username)) -> {
                LocalUtils.getString(R.string.pls_enter_correct_mobile_email)
            }
            else -> null
        }
        _accountMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }

    /**
     * 手机号/邮箱/用户名
     */
    fun checkUserName(username: String): String? {
        val msg = when {
            username.isBlank() -> LocalUtils.getString(R.string.error_input_empty)
            !(VerifyConstUtil.verifyPhone(username) || VerifyConstUtil.verifyMail(username) || VerifyConstUtil.verifyLengthRange(
                username,
                4,
                20)) -> {
                LocalUtils.getString(R.string.pls_enter_correct_mobile_email_username)
            }
            else -> null
        }
        _userNameMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }

    fun checkPassword(password: String): String? {
        val msg = when {
            password.isEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyLengthRange(password,
                6,
                20) -> LocalUtils.getString(R.string.error_register_password)
//             -> LocalUtils.getString(R.string.error_register_password)
//             -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _passwordMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }

    fun checkValidCode(validCode: String): String? {
        val msg = when {
            validCode.isNullOrBlank() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyValidCode(validCode) -> LocalUtils.getString(R.string.verification_not_correct)
            else -> null
        }
        _validateCodeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }

    fun checkMsgCode(validCode: String): String? {
        val msg = when {
            validCode.isNullOrBlank() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyValidCode(validCode) -> LocalUtils.getString(R.string.verification_not_correct)
            else -> null
        }
        _msgCodeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }

    fun focusChangeCheckAllInputComplete() {
        _loginEnable.value = checkAllInputComplete()
    }

    private fun checkAllInputComplete(): Boolean {
        if (loginType == 0) {
            if (checkInputPair(accountMsg)) {
                return false
            }
            if (checkInputPair(msgCodeMsg)) {
                return false
            }
        } else {
            if (checkInputPair(userNameMsg)) {
                return false
            }
            if (checkInputPair(passwordMsg)) {
                return false
            }
        }
        return true
    }

    private fun checkInputPair(data: LiveData<Pair<String?, Boolean>>): Boolean {
        return data.value?.first != null || data.value?.second != true
    }

    private fun loading() {
        _isLoading.postValue(true)
    }

    private fun hideLoading() {
        _isLoading.postValue(false)
    }

    fun queryPlatform(inviteCode: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.bettingStationService.queryPlatform(inviteCode)
            }
            if (result?.success == true) {

            } else {
                _inviteCodeMsg.value = result?.msg
                focusChangeCheckAllInputComplete()
            }
        }
    }
}