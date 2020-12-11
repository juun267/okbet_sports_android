package org.cxct.sportlottery.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import com.github.jokar.multilanguages.library.MultiLanguage
import org.cxct.sportlottery.R
import java.util.*

object LanguageManager {

    enum class Language { AUTO, CN, EN }

    /**
     * 获取系统的locale
     *
     * @return Locale对象
     */
    fun getSystemLocale(context: Context?): Locale {
        return SPUtil.getInstance(context).getSystemCurrentLocal()
    }

    fun getSelectLanguage(context: Context): Language {
        return when (SPUtil.getInstance(context).getSelectLanguage()) {
            Language.AUTO.name -> Language.AUTO
            Language.CN.name -> Language.CN
            Language.EN.name ->  Language.EN
            else -> Language.AUTO
        }
    }

    /**
     * 获取选择的语言设置
     *
     * @param context
     * @return
     */
    fun getSetLanguageLocale(context: Context?): Locale {
        return when (SPUtil.getInstance(context).getSelectLanguage()) {
            Language.AUTO.name -> getSystemLocale(context)
            Language.CN.name -> Locale.CHINA
            Language.EN.name -> Locale.ENGLISH
            else -> getSystemLocale(context)
        }
    }

    fun saveSystemCurrentLanguage(context: Context?) {
        SPUtil.getInstance(context).setSystemCurrentLocal(MultiLanguage.getSystemLocal(context))
    }

    /**
     * 保存系统语言
     * @param context
     * @param newConfig
     */
    fun saveSystemCurrentLanguage(
        context: Context?,
        newConfig: Configuration?
    ) {
        SPUtil.getInstance(context).setSystemCurrentLocal(MultiLanguage.getSystemLocal(newConfig))
    }

    fun saveSelectLanguage(context: Context?, select: Language) {
        SPUtil.getInstance(context).saveLanguage(select.name)
        MultiLanguage.setApplicationLanguage(context)
    }
}

object SPUtil {
    private const val SP_NAME = "language_setting"
    private const val TAG_LANGUAGE = "language_select"
    private var mSharedPreferences: SharedPreferences? = null
    private var systemCurrentLocal = Locale.ENGLISH

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


    fun getSystemCurrentLocal(): Locale {
        return systemCurrentLocal
    }

    fun setSystemCurrentLocal(local: Locale) {
        systemCurrentLocal = local
    }

}