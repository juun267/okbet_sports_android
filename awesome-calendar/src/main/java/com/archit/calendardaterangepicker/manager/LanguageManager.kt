package com.archit.calendardaterangepicker.manager

import android.content.Context
import android.content.SharedPreferences

object LanguageManager {

    enum class Language(val key: String) { ZH("zh"), ZHT("zht"), EN("en"), VI("vi") }

    fun getSelectLanguage(context: Context?): Language {
        return Language.values().firstOrNull { it.key == SPUtil.getInstance(context).getSelectLanguage() }
            ?: Language.EN
    }
}

object SPUtil {
    private const val SP_NAME = "language_setting"
    private const val TAG_LANGUAGE = "language_select"
    private var mSharedPreferences: SharedPreferences? = null

    fun getInstance(context: Context?): SPUtil {
        if (mSharedPreferences == null)
            mSharedPreferences = context?.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        return this
    }

    fun getSelectLanguage(): String? {
        return mSharedPreferences?.getString(TAG_LANGUAGE, null)
    }

}