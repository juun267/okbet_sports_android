package com.archit.calendardaterangepicker.manager

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.LocaleList
import java.util.*

object LanguageManager {

    enum class Language(val key: String) { ZH("zh"), ZHT("zht"), EN("en"), VI("vi") }

    fun getSystemLocal(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0]
        } else {
            Locale.getDefault()
        }
    }

    fun getSelectLanguage(context: Context): Language {
        val savedLanguage = Language.values()
            .firstOrNull { it.key.equals(SPUtil.getInstance(context).getSelectLanguage()) }

        if (savedLanguage != null) {
            return savedLanguage
        }

        val localLanguage = Language.values()
            .firstOrNull { it.key.equals(getSystemLocal().language) }

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
        }
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