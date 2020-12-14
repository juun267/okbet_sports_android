package org.cxct.sportlottery.repository

import android.content.SharedPreferences
import liveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.LoginRequest
import org.cxct.sportlottery.network.index.LoginResult


const val KEY_TOKEN = "token"
const val KEY_USERNAME = "user_name"

class LoginRepository(private val sharedPref: SharedPreferences) {
    val token = sharedPref.liveData(KEY_TOKEN, "")
    val userName = sharedPref.liveData(KEY_TOKEN, "")

    suspend fun login(userName: String, password: String): LoginResult? {
        val loginResponse = OneBoSportApi.indexService.login(
            LoginRequest(userName, password)
        )

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.let {
                with(sharedPref.edit()) {
                    putString(KEY_TOKEN, it.loginData.token)
                    putString(KEY_USERNAME, it.loginData.userName)
                    apply()
                }
            }
        }

        return loginResponse.body()
    }
}