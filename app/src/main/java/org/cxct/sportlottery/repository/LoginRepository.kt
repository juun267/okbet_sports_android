package org.cxct.sportlottery.repository

import android.content.Context
import android.content.SharedPreferences
import liveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.LoginRequest
import org.cxct.sportlottery.network.index.LoginResult
import retrofit2.Response

const val NAME_LOGIN = "login"
const val KEY_TOKEN = "token"
const val KEY_USERNAME = "user_name"

class LoginRepository(private val androidContext: Context) {
    private val sharedPref: SharedPreferences by lazy {
        androidContext.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    val token = sharedPref.liveData(KEY_TOKEN, "")
    val userName = sharedPref.liveData(KEY_USERNAME, "")

    suspend fun login(userName: String, password: String): Response<LoginResult> {
        val loginResponse = OneBoSportApi.indexService.login(
            LoginRequest(userName, password)
        )

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                with(sharedPref.edit()) {
                    putString(KEY_TOKEN, it.loginData?.token)
                    putString(KEY_USERNAME, it.loginData?.userName)
                    apply()
                }
            }
        }

        return loginResponse
    }

    fun logout() {
        with(sharedPref.edit()) {
            remove(KEY_TOKEN)
            remove(KEY_USERNAME)
            apply()
        }
    }
}