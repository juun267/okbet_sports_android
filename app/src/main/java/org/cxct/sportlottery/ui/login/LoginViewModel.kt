package org.cxct.sportlottery.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.LoginResult
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.MD5Util


class LoginViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {
    val loginFormState: LiveData<LoginFormState>
        get() = _loginFormState
    val loginResult: LiveData<LoginResult>
        get() = _loginResult

    private val _loginFormState = MutableLiveData<LoginFormState>()
    private val _loginResult = MutableLiveData<LoginResult>()

    fun loginDataChanged(context: Context, account: String, password: String) {
        val accountError = checkAccount(context, account)
        val passwordError = checkPassword(context, password)
        val isDataValid = accountError == null && passwordError == null
        _loginFormState.value = LoginFormState(accountError, passwordError, isDataValid)
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            doNetwork {
                loginRepository.login(
                    username,
                    MD5Util.MD5Encode(password)
                )
            }?.let { result ->
                _loginResult.postValue(result)
            }
        }
    }

    private fun checkAccount(context: Context, username: String): String? {
        return when {
            username.isBlank() -> context.getString(R.string.error_required_field)
            else -> null
        }
    }

    private fun checkPassword(context: Context, password: String): String? {
        return when {
            password.isBlank() -> context.getString(R.string.error_required_field)
            password.length < 6 -> context.getString(R.string.error_last_6_pw)
            else -> null
        }
    }
}