package org.cxct.sportlottery.ui.login

import android.util.Patterns
import androidx.lifecycle.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.LoginResult
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.MD5Util


class LoginViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {
    val loginFormState: LiveData<LoginFormState>
        get() = _loginFormState

    private val _loginFormState = MutableLiveData<LoginFormState>()

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginFormState.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginFormState.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginFormState.value = LoginFormState(isDataValid = true)
        }
    }

    fun login(username: String, password: String): LiveData<LoginResult> {
        return doNetwork {
            loginRepository.login(
                username,
                MD5Util.MD5Encode(password)
            )
        }
    }

    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}