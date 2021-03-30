package org.cxct.sportlottery.ui.splash

import android.content.Context
import android.content.SharedPreferences
import org.cxct.sportlottery.repository.KEY_PLATFORM_ID
import org.cxct.sportlottery.repository.NAME_LOGIN

class HostRepository(private val androidContext: Context) {
    companion object {
        private const val KEY_IS_NEED_GET_HOST = "key-is-need-get-host"
        private const val KEY_HOST_URL = "key-host-url"
    }

    private val sharedPref: SharedPreferences by lazy {
        androidContext.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    //20190111 判斷進 MainActivity 後還需不需要再 getHost 一次
    //若 load端一開始為 null,在 SplashActivity 已經 getHost 一次，就不需要再 MainActivity get Host
    var isNeedGetHost
        get() = sharedPref.getBoolean(KEY_IS_NEED_GET_HOST, true)
        set(value) {
            with(sharedPref.edit()) {
                putBoolean(KEY_IS_NEED_GET_HOST, value)
                apply()
            }
        }

    var hostUrl
        get() = sharedPref.getString(KEY_HOST_URL, "")?: ""
        set(value) {
            with(sharedPref.edit()) {
                putString(KEY_HOST_URL, value)
                apply()
            }
        }

    var platformId
        get() = sharedPref.getLong(KEY_PLATFORM_ID, -1)
        set(value) {
            with(sharedPref.edit()) {
                putLong(KEY_PLATFORM_ID, value)
                apply()
            }
        }
}
