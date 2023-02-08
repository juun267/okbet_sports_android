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
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.login.ValidateLoginDeviceSmsRequest
import org.cxct.sportlottery.network.index.logout.LogoutResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel
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
    val validResult: LiveData<LogoutResult>
        get() = _validResult

    private val _loginFormState = MutableLiveData<LoginFormState>()
    private val _loginResult = MutableLiveData<LoginResult>()
    private val _loginSmsResult = MutableLiveData<LogoutResult>()
    private val _validCodeResult = MutableLiveData<ValidCodeResult?>()
    private val _validResult = MutableLiveData<LogoutResult>()

    val accountMsg: LiveData<Pair<String?, Boolean>>
        get() = _accountMsg
    private val _accountMsg = MutableLiveData<Pair<String?, Boolean>>()

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
            loginRepository.password = if (loginRepository.isRememberPWD) originalPassword else null

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

    fun checkAccount(username: String): String? {
        val msg = when {
            username.isBlank() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyLengthRange(username, 4, 16) -> {
                LocalUtils.getString(R.string.error_member_account)
            }
            else -> null
        }
        _accountMsg.value = Pair(msg, msg == null)
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
            validCode.isBlank() -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        _validateCodeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }

    fun focusChangeCheckAllInputComplete() {
        _loginEnable.value = checkAllInputComplete()
    }

    private fun checkAllInputComplete(): Boolean {
        if (checkInputPair(accountMsg)) {
            return false
        }
        if (checkInputPair(passwordMsg)) {
            return false
        }
        if (sConfigData?.enableValidCode == FLAG_OPEN && checkInputPair(validateCodeMsg)) {
            return false
        }
        return true
    }

    private fun checkInputPair(data: LiveData<Pair<String?, Boolean>>): Boolean {
        return data.value?.first != null || data.value?.second != true
    }
}