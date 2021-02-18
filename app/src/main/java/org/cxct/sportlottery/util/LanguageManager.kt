package org.cxct.sportlottery.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import com.github.jokar.multilanguages.library.MultiLanguage
import java.util.*

object LanguageManager {

    enum class Language(val key: String) { ZH("zh"), ZHT("zht"), EN("en"), VI("vi") }

    /**
     * 获取系统的locale
     *
     * @return Locale对象
     */
    fun getSystemLocale(context: Context?): Locale {
        return SPUtil.getInstance(context).getSystemCurrentLocal()
    }

    fun getSelectLanguage(context: Context?): Language {
        //TODO 20210217 Simon 紀錄：目前只有 簡體中文、英文 選項，且預設是簡體中文，待之後 review
        return when (SPUtil.getInstance(context).getSelectLanguage()) {
            Language.ZH.key -> Language.ZH
//            Language.ZHT.key -> Language.ZHT
            Language.EN.key ->  Language.EN
//            Language.VI.key -> Language.VI
            else -> {
                //若APP local 未設定過語系，就使用系統語系判斷
                val local = getSystemLocale(context)

                when {
                    local.language == Locale.ENGLISH.language -> Language.EN
//                    local.language == Locale("vi").language -> Language.VI
//                    local.language == Locale.SIMPLIFIED_CHINESE.language && local.country == Locale.SIMPLIFIED_CHINESE.country -> Language.ZH
//                    local.language == Locale.TRADITIONAL_CHINESE.language -> Language.ZHT
                    else -> Language.ZH
                }
            }
        }
    }

    /**
     * 获取选择的语言设置
     *
     * @param context
     * @return
     */
    fun getSetLanguageLocale(context: Context?): Locale {
        return when (getSelectLanguage(context)) {
            Language.ZH -> Locale.SIMPLIFIED_CHINESE
            Language.ZHT -> Locale.TRADITIONAL_CHINESE
            Language.EN -> Locale.ENGLISH
            Language.VI -> Locale("vi")
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
        SPUtil.getInstance(context).saveLanguage(select.key)
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