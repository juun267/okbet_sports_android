package org.cxct.sportlottery.repository

import android.content.Context
import android.content.SharedPreferences
import liveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.*
import org.cxct.sportlottery.util.AesCryptoUtil
import retrofit2.Response

const val NAME_LOGIN = "login"
const val KEY_TOKEN = "token"
const val KEY_ACCOUNT = "account"
const val KEY_PWD = "pwd"
const val KEY_REMEMBER_PWD = "remember_pwd"

var sLoginData: LoginData? = null

class LoginRepository(private val androidContext: Context) {
    private val sharedPref: SharedPreferences by lazy {
        androidContext.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    val token = sharedPref.liveData(KEY_TOKEN, "")

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


    suspend fun login(userName: String, password: String): Response<LoginResult> {
        val loginResponse = OneBoSportApi.indexService.login(
            LoginRequest(userName, password)
        )

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                sLoginData = it.loginData

                with(sharedPref.edit()) {
                    putString(KEY_TOKEN, it.loginData?.token)
                    apply()
                }
            }
        }

        return loginResponse
    }

    suspend fun logout(): Response<LogoutResult> {
        with(sharedPref.edit()) {
            remove(KEY_TOKEN)
            apply()
        }

        return OneBoSportApi.indexService.logout(LogoutRequest())
    }
}