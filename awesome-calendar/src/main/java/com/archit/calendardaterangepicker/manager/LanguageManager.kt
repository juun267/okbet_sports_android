package com.archit.calendardaterangepicker.manager

import android.content.Context
import android.content.SharedPreferences
import java.util.*

object LanguageManager {

    enum class Language(val key: String) { ZH("zh"), ZHT("zht"), EN("en"), VI("vi"),PHI("phi") }

    fun getSelectLanguage(context: Context): Language {
        val savedLanguage = Language.values()
            .firstOrNull { it.key.equals(SPUtilLang.getInstance(context).getSelectLanguage()) }

        if (savedLanguage != null) {
            return savedLanguage
        }

        val local = context.resources.configuration.locale
        val localLanguage = Language.values()
            .firstOrNull {
                it.key.equals(local.language)
            }

        if (localLanguage != null) {
            return localLanguage
        }

        return Language.EN
    }

    fun getSetLanguageLocale(context: Context): Locale {
        return when (getSelectLanguage(context)) {
            Language.ZH, Language.ZHT -> Locale.SIMPLIFIED_CHINESE
            Language.EN -> Locale.ENGLISH
            Language.VI -> Locale("vi")
            Language.PHI -> Locale("phi")
        }
    }
}

object SPUtilLang {
    private const val SP_NAME = "language_setting"
    private const val TAG_LANGUAGE = "language_select"
    private var mSharedPreferences: SharedPreferences? = null
    var systemCurrentLocal: Locale = Locale.ENGLISH

    fun getInstance(context: Context?): SPUtilLang {
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
}