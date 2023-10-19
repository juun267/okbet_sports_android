package org.cxct.sportlottery.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.util.DisplayMetrics
import com.luck.picture.lib.basic.PictureSelectorSupporterActivity
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.crash.FirebaseLog
import org.cxct.sportlottery.repository.sConfigData
//import org.cxct.sportlottery.util.language.MultiLanguages
import java.util.*

object LanguageManager {

    private val languageChangeListeners = mutableListOf<(Language, Language) -> Unit>()

    fun addLanguageChangedListener(block: (Language, Language) -> Unit) {
        languageChangeListeners.add(block)
    }

    enum class Language(val key: String) {
        ZH("zh"), ZHT("zht"), EN("en"), VI("vi"), TH("th"), PHI("ph")
    }

    /**
     * 获取系统的locale
     *
     * @return Locale对象
     */
    private fun getSystemLocale(context: Context?): Locale {
        return SPUtil.getInstance(context).systemCurrentLocal
    }

    fun getSelectLanguage(context: Context?): Language {
        //[Martin]無腦鎖定為英文
//        if(BuildConfig.CHANNEL_NAME == "spkx"){
//            return Language.EN
//        }
        //TODO 20210217 Simon 紀錄：目前只有 簡體中文、英文 選項，且預設是簡體中文，待之後 review
        return when (SPUtil.getInstance(context).getSelectLanguage()) {
            Language.ZH.key, Language.ZHT.key -> Language.ZH
            Language.EN.key -> Language.EN
            Language.VI.key -> Language.VI
            Language.TH.key -> Language.TH
            Language.PHI.key -> Language.PHI
            else -> {
                //若APP local 未設定過語系，就使用系統語系判斷
                val local = getSystemLocale(context)
                when {
                    local.language == Locale.ENGLISH.language -> Language.EN
                    local.language == Locale("vi").language -> Language.VI
                    local.language == Locale("th").language -> Language.TH
                    local.language == Locale("phi").language || local.language == Locale("fil").language -> Language.PHI
                    (local.language == Locale.SIMPLIFIED_CHINESE.language && local.country == Locale.SIMPLIFIED_CHINESE.country)
                            || local.language == Locale.TRADITIONAL_CHINESE.language -> Language.ZH

                    else -> Language.values().find { it.key == BuildConfig.DEFAULT_LANGUAGE }
                        ?: Language.EN
                }
//                Language.EN //2021/10/04 與PM確認過，不管手機是什麼語系，都預先使用英文版本
            }
        }
    }

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is PictureSelectorSupporterActivity) {
                    val resources: Resources = activity.resources
                    val config = resources.configuration
                    val locale = config.locale
                    val dm: DisplayMetrics = resources.getDisplayMetrics()
                    config.setLocale(locale)
                    activity.createConfigurationContext(config)
                    resources.updateConfiguration(config, dm)
                }

            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {

            }

        })
    }

    fun getSelectLanguageName(): String {
        return "${SPUtil.getSelectLanguage()}"
    }

    fun getLanguageFlag(context: Context?): Int {
        return when (getSelectLanguage(context)) {
            Language.ZH -> R.drawable.ic_flag_cn
            Language.VI -> R.drawable.ic_flag_vi
            Language.TH -> R.drawable.ic_flag_th
            Language.PHI -> R.drawable.ic_flag_phi
            else -> R.drawable.ic_flag_en
        }
    }

    fun getLanguageString2(context: Context?): String {
        return when (getSelectLanguage(context)) {
            Language.ZH -> "zh"
            Language.VI -> "vi"
            Language.TH -> "th"
            Language.PHI -> "ph"
            else -> "en"
        }
    }

    fun getLanguageString(context: Context?): String {
        return when (getSelectLanguage(context)) {
            Language.ZH -> "zh"
            Language.VI -> "vi"
            Language.TH -> "th"
            Language.PHI -> "phi"
            else -> "en"
        }
    }

    fun getLanguageStringResource(context: Context?): String {
        return when (getSelectLanguage(context)) {
            Language.ZH -> context?.resources?.getString(R.string.language_cn) ?: ""
            Language.VI -> context?.resources?.getString(R.string.language_vi) ?: ""
            Language.TH -> context?.resources?.getString(R.string.language_th) ?: ""
            Language.PHI -> context?.resources?.getString(R.string.language_phi) ?: ""
            else -> context?.resources?.getString(R.string.language_en) ?: ""
        }
    }

    fun getLanguageStringList(context: Context?): List<String> {
        return listOf(
            context?.resources?.getString(R.string.language_cn) ?: "",
            context?.resources?.getString(R.string.language_en) ?: "",
            context?.resources?.getString(R.string.language_vi) ?: "",
            context?.resources?.getString(R.string.language_th) ?: "",
            context?.resources?.getString(R.string.language_phi) ?: "",
            ""
        )
    }

    private lateinit var selectedLocale: Locale

    /**
     * 获取选择的语言设置
     *
     * @param context
     * @return
     */
    fun getSetLanguageLocale(context: Context?): Locale {
        if (!::selectedLocale.isInitialized) {
            selectedLocale = convert(getSelectLanguage(context))
        }
        return selectedLocale
    }

    private fun convert(language: Language): Locale {
        return when (language) {
            Language.ZH, Language.ZHT -> Locale.SIMPLIFIED_CHINESE
            Language.EN -> Locale.ENGLISH
            Language.VI -> Locale("vi")
            Language.TH -> Locale("th")
            Language.PHI -> Locale("phi")
            else -> {
                Locale("en")
            }
        }
    }

    fun saveSystemCurrentLanguage(context: Context) {
        SPUtil.getInstance(context).systemCurrentLocal = getSystemLocal()
    }

    /**
     * 保存系统语言
     * @param context
     * @param newConfig
     */
    fun saveSystemCurrentLanguage(
        context: Context,
        newConfig: Configuration
    ) {
        SPUtil.getInstance(context).systemCurrentLocal = getSystemLocal(newConfig)
    }

    fun saveSelectLanguage(context: Context, select: Language) {
        val lastLanguage = getSelectLanguage(context)
        FirebaseLog.addLogInfo("currentLanguage", select.name) // 在崩溃日志中记录当前的语言类型
        selectedLocale = convert(select)
//        MultiLanguages.setAppLanguage(context, selectedLocale)
        SPUtil.getInstance(context).saveLanguage(select.key)
        KvUtils.removeKey("splashAd")
        setApplicationLanguage(context)
        if (lastLanguage != select) {
            languageChangeListeners.forEach { it.invoke(lastLanguage, select) }
        }
    }

    private fun getSystemLocal(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0]
        } else {
            Locale.getDefault()
        }
    }

    private fun getSystemLocal(newConfig: Configuration): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            newConfig.locales[0]
        } else {
            newConfig.locale
        }
    }

    fun setApplicationLanguage(context: Context) {
        val resources = context.applicationContext.resources
        val dm = resources.displayMetrics
        val config = resources.configuration
        val locale: Locale = getSetLanguageLocale(context)
        config.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
            context.applicationContext.createConfigurationContext(config)
            Locale.setDefault(locale)
        }
        resources.updateConfiguration(config, dm)
    }

    fun makeUseLanguage() : MutableList<Language>{
        val list = mutableListOf(
                Language.EN,
                Language.PHI,
                Language.ZH,
                Language.VI,
            )
        if (sConfigData?.supportLanguage?.isNotEmpty() == true) {
            val sup = sConfigData?.supportLanguage?.split(",")
            if (sup?.isNotEmpty() == true) {
                list.listIterator().let {
                    while (it.hasNext()) {
                        if (!sup.contains(it.next().key)) {
                            it.remove()
                        }
                    }
                }
                list.sortWith { t1, t2 ->
                    sup.indexOf(t1.key).compareTo(sup.indexOf(t2.key))
                }
            }
        }
        return list
    }
}

