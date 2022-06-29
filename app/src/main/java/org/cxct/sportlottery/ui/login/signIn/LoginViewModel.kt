package org.cxct.sportlottery.ui.login.signIn

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
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


    val account by lazy { loginRepository.account }
    val password by lazy { loginRepository.password }

    var isRememberPWD
        get() = loginRepository.isRememberPWD
        set(value) {
            loginRepository.isRememberPWD = value
        }

    fun checkInputData(context: Context, account: String, password: String, validCode: String): Boolean {
        val accountError = checkAccount(context, account)
        val passwordError = checkPassword(context, password)
        val validCodeError = checkValidCode(context, validCode)
        val isDataValid = accountError == null && passwordError == null &&
                (sConfigData?.enableValidCode != FLAG_OPEN || validCodeError == null)
        _loginFormState.value = LoginFormState(accountError, passwordError, validCodeError)

        return isDataValid
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
            }
        }
    }

    fun sendLoginDeviceSms() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.sendLoginDeviceSms()
            }?.let { result ->
                _loginSmsResult.postValue(result)
            }
        }
    }
    fun validateLoginDeviceSms(code: String, deviceId: String) {

        val validateRequest = ValidateLoginDeviceSmsRequest(
            loginEnvInfo = deviceId,
            validCode = code,
            loginSrc = LOGIN_SRC
        )

        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.validateLoginDeviceSms(validateRequest)
            }?.let { result ->
                //手機驗證成功後, 獲取最新的用戶資料
                if (result.success) {
                    userInfoRepository.getUserInfo()
                }
                _validResult.postValue(result)
            }
        }
    }


    suspend fun getUserPhone():String?{
        return withContext(Dispatchers.IO) {
            userInfoRepository.userInfo?.firstOrNull()?.phone.toString()
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

    private fun checkAccount(context: Context, username: String): String? {
        return when {
            username.isBlank() -> context.getString(R.string.error_input_empty)
            else -> null
        }
    }

    private fun checkPassword(context: Context, password: String): String? {
        return when {
            password.isBlank() -> context.getString(R.string.error_input_empty)
            else -> null
        }
    }

     fun checkValidCode(context: Context, validCode: String): String? {
        return when {
            validCode.isBlank() -> context.getString(R.string.error_input_empty)
            else -> null
        }
    }

}