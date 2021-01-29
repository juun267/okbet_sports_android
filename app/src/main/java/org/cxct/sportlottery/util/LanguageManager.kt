package org.cxct.sportlottery.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
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
        return when (SPUtil.getInstance(context).getSelectLanguage()) {
            Language.ZH.key -> Language.ZH
            Language.ZHT.key -> Language.ZHT
            Language.EN.key ->  Language.EN
            Language.VI.key -> Language.VI
            else -> {
                //TODO simon test review 目前 mapping 有問題，之後解
                //若APP local 未設定過語系，就使用系統語系判斷
                val local = getSystemLocale(context)

//                Log.e("simon test", "<== language ${local.language}")
//                Log.e("simon test", "==> language ${Locale.SIMPLIFIED_CHINESE.language}")
//                Log.e("simon test", "==> language ${Locale.TRADITIONAL_CHINESE.language}")
//                Log.e("simon test", "==> language ${Locale.ENGLISH.language}")
//                Log.e("simon test", "==> language ${Locale.forLanguageTag("vi_VN").language}")
//                Log.e("simon test", "<== ================= ==>")

                when (local.language) {
                    Locale.SIMPLIFIED_CHINESE.language -> Language.ZH
                    Locale.TRADITIONAL_CHINESE.language -> Language.ZHT
                    Locale.ENGLISH.language ->  Language.EN
                    Locale.forLanguageTag("vi_VN").language -> Language.VI
                    else -> Language.EN
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
            Language.VI -> Locale.forLanguageTag("vi_VN")
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