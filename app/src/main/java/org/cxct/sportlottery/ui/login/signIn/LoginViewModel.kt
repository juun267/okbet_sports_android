package org.cxct.sportlottery.ui.login.signIn

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseViewModel


class LoginViewModel(
    private val androidContext: Context,
    private val loginRepository: LoginRepository
) : BaseViewModel() {
    val loginFormState: LiveData<LoginFormState>
        get() = _loginFormState
    val loginResult: LiveData<LoginResult>
        get() = _loginResult
    val validCodeResult: LiveData<ValidCodeResult?>
        get() = _validCodeResult

    private val _loginFormState = MutableLiveData<LoginFormState>()
    private val _loginResult = MutableLiveData<LoginResult>()
    private val _validCodeResult = MutableLiveData<ValidCodeResult?>()

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
                _loginResult.postValue(result)
            }
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

    private fun checkValidCode(context: Context, validCode: String): String? {
        return when {
            validCode.isBlank() -> context.getString(R.string.hint_verification_code)
            else -> null
        }
    }
}