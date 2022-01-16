package org.cxct.sportlottery.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import com.github.jokar.multilanguages.library.MultiLanguage
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import java.util.*

object LanguageManager {

    enum class Language(val key: String) { ZH("zh"), ZHT("zht"), EN("en"), VI("vi") }

    /**
     * 获取系统的locale
     *
     * @return Locale对象
     */
    fun getSystemLocale(context: Context?): Locale {
        return SPUtil.getInstance(context).systemCurrentLocal
    }

    fun getSelectLanguage(context: Context?): Language {
        //TODO 20210217 Simon 紀錄：目前只有 簡體中文、英文 選項，且預設是簡體中文，待之後 review
        return when (SPUtil.getInstance(context).getSelectLanguage()) {
            Language.ZH.key, Language.ZHT.key -> Language.ZH
            Language.EN.key -> Language.EN
            Language.VI.key -> Language.VI
            else -> {
                //若APP local 未設定過語系，就使用系統語系判斷
                val local = getSystemLocale(context)

                when {
                    local.language == Locale.ENGLISH.language -> Language.EN
                    local.language == Locale("vi").language -> Language.VI
                    (local.language == Locale.SIMPLIFIED_CHINESE.language && local.country == Locale.SIMPLIFIED_CHINESE.country)
                            || local.language == Locale.TRADITIONAL_CHINESE.language -> Language.ZH
                    else -> Language.values().find { it.key == BuildConfig.DEFAULT_LANGUAGE } ?: Language.EN
                }
//                Language.EN //2021/10/04 與PM確認過，不管手機是什麼語系，都預先使用英文版本
            }
        }
    }

    fun getLanguageFlag(context: Context?): Int {
        return when (getSelectLanguage(context)) {
            Language.ZH -> R.drawable.ic_flag_cn
            Language.VI -> R.drawable.ic_flag_vi
            else -> R.drawable.ic_flag_en
        }
    }

    fun getLanguageString(context: Context?): String {
        return when (getSelectLanguage(context)) {
            Language.ZH -> "zh"
            Language.VI -> "vi"
            else -> "en"
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
            Language.ZH, Language.ZHT -> Locale.SIMPLIFIED_CHINESE
            Language.EN -> Locale.ENGLISH
            Language.VI -> Locale("vi")
        }
    }

    fun saveSystemCurrentLanguage(context: Context?) {
        SPUtil.getInstance(context).systemCurrentLocal = MultiLanguage.getSystemLocal(context)
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
        SPUtil.getInstance(context).systemCurrentLocal = MultiLanguage.getSystemLocal(newConfig)
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

}