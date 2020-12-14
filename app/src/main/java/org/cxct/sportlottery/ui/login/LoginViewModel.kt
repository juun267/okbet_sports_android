package org.cxct.sportlottery.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.LoginRequest
import org.cxct.sportlottery.network.index.LoginResult
import org.cxct.sportlottery.util.MD5Util
import retrofit2.Response


class LoginViewModel() : ViewModel() {
    val loginFormState: LiveData<LoginFormState>
        get() = _loginFormState

    val loginResult: LiveData<Response<LoginResult>>
        get() = _loginResult

    private val _loginFormState = MutableLiveData<LoginFormState>()

    private val _loginResult = MutableLiveData<Response<LoginResult>>()

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginFormState.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginFormState.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginFormState.value = LoginFormState(isDataValid = true)
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val loginResponse = OneBoSportApi.indexService.login(
                LoginRequest(
                    username,
                    MD5Util.MD5Encode(password)
                )
            )
            _loginResult.postValue(loginResponse)
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