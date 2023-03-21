package org.cxct.sportlottery.util

import android.content.Context
import android.content.SharedPreferences
import java.util.*

object SPUtil {
    private const val SP_NAME = "language_setting"
    private const val TAG_LANGUAGE = "language_select"

    //上架市场，隐藏功能的开关
    const val MARKET_SWITCH = "market_switch"
    private var mSharedPreferences: SharedPreferences? = null
    var systemCurrentLocal: Locale = Locale.ENGLISH

    fun getInstance(context: Context?): SPUtil {
        if (mSharedPreferences == null)
            mSharedPreferences = context?.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        return this
    }

    fun saveLanguage(select: String?) {
        mSharedPreferences?.edit()
            ?.putString(TAG_LANGUAGE, select)
            ?.apply()
    }

    fun getSelectLanguage(): String? {
        return mSharedPreferences?.getString(TAG_LANGUAGE, null)
    }

    fun saveString(key: String, value: String?) {
        mSharedPreferences?.edit()
            ?.putString(key, value)
            ?.apply()
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return mSharedPreferences?.getString(key, defaultValue)
    }

    fun saveMarketSwitch(value: Boolean) {
        mSharedPreferences?.edit()
            ?.putBoolean(MARKET_SWITCH, value)
            ?.apply()
    }

    fun getMarketSwitch(): Boolean {
        return mSharedPreferences?.getBoolean(MARKET_SWITCH, false) == true
    }
}