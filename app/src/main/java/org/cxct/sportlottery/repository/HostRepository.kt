package org.cxct.sportlottery.repository

import android.content.Context
import android.content.SharedPreferences

class HostRepository(private val androidContext: Context) {
    companion object {
        private const val KEY_IS_NEED_GET_HOST = "key-is-need-get-host"
        private const val KEY_HOST_URL = "key-host-url"
    }

    private val sharedPref: SharedPreferences by lazy {
        androidContext.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
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
