package org.cxct.sportlottery.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.logout.LogoutRequest
import org.cxct.sportlottery.network.index.logout.LogoutResult
import org.cxct.sportlottery.network.index.register.RegisterRequest
import org.cxct.sportlottery.util.AesCryptoUtil
import retrofit2.Response

const val NAME_LOGIN = "login"
const val KEY_TOKEN = "token"
const val KEY_ACCOUNT = "account"
const val KEY_PWD = "pwd"
const val KEY_REMEMBER_PWD = "remember_pwd"

class LoginRepository(private val androidContext: Context) {
    private val sharedPref: SharedPreferences by lazy {
        androidContext.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    val isLogin: LiveData<Boolean>
        get() = _isLogin

    private val _isLogin = MutableLiveData<Boolean>()

    var account
        get() = sharedPref.getString(KEY_ACCOUNT, "")
        set(value) {
            with(sharedPref.edit()) {
                putString(KEY_ACCOUNT, value)
                apply()
            }
        }

    var password: String?
        get() {
            return try {
                val securityKey = AesCryptoUtil.encrypt(KEY_PWD)
                val securityValue = sharedPref.getString(securityKey, "")?: ""
                AesCryptoUtil.decrypt(securityValue)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        set(value) {
            try {
                with(sharedPref.edit()) {
                    val securityKey = AesCryptoUtil.encrypt(KEY_PWD)
                    val securityValue = AesCryptoUtil.encrypt(value?: "")
                    putString(securityKey, securityValue)
                    commit()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    var isRememberPWD
        get() = sharedPref.getBoolean(KEY_REMEMBER_PWD, false)
        set(value) {
            with(sharedPref.edit()) {
                putBoolean(KEY_REMEMBER_PWD, value)
                commit()
            }
        }


    suspend fun login(loginRequest: LoginRequest): Response<LoginResult> {
        val loginResponse = OneBoSportApi.indexService.login(loginRequest)

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                sLoginData = it.loginData

                _isLogin.postValue(it.loginData != null)

                with(sharedPref.edit()) {
                    putString(KEY_TOKEN, it.loginData?.token)
                    apply()
                }
            }
        }

        return loginResponse
    }

    suspend fun logout(): Response<LogoutResult> {
        _isLogin.postValue(false)
        return OneBoSportApi.indexService.logout(LogoutRequest())
    }

    suspend fun register(registerRequest: RegisterRequest): Response<LoginResult> {
        val loginResponse = OneBoSportApi.indexService.register(registerRequest)

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                account = registerRequest.userName //預設存帳號
                sLoginData = it.loginData

                _isLogin.postValue(it.loginData != null)

                with(sharedPref.edit()) {
                    putString(KEY_TOKEN, it.loginData?.token)
                    apply()
                }
            }
        }

        return loginResponse
    }

    fun clear() {
        with(sharedPref.edit()) {
            remove(KEY_TOKEN)
            apply()
        }
    }
}