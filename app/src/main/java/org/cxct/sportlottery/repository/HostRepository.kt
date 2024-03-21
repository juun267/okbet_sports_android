package org.cxct.sportlottery.repository

import android.content.Context
import android.content.SharedPreferences
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.util.KvUtils

object HostRepository {
    private const val KEY_HOST_URL = "key-host-url"

    private val sharedPref: SharedPreferences by lazy {
        MultiLanguagesApplication.mInstance.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    var hostUrl: String
        get() = KvUtils.decodeString(KEY_HOST_URL)
        set(value) {
            KvUtils.put(KEY_HOST_URL, value)
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
