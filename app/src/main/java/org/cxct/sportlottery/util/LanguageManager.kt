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
import android.util.Log
import com.luck.picture.lib.basic.PictureSelectorSupporterActivity
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.crash.FirebaseLog
import org.cxct.sportlottery.repository.sConfigData
import java.util.*


object LanguageManager {

    private const val TAG_LANGUAGE = "language_select"
    private var systemCurrentLocal: Locale = Locale.ENGLISH
    private val languageChangeListeners = mutableListOf<(Language, Language) -> Unit>()

    enum class Language(val key: String) {
        ZH("zh"), ZHT("zht"), EN("en"), VI("vi"), TH("th"), PHI("ph")
    }

    /**
     * 获取系统的locale
     *
     * @return Locale对象
     */
    private fun getSystemLocale() = systemCurrentLocal

    fun getSelectLanguage(context: Context? = null): Language {
        //[Martin]無腦鎖定為英文
//        if(BuildConfig.CHANNEL_NAME == "spkx"){
//            return Language.EN
//        }
        //TODO 20210217 Simon 紀錄：目前只有 簡體中文、英文 選項，且預設是簡體中文，待之後 review
        return when (getSelectLanguageName()) {
            Language.ZH.key, Language.ZHT.key -> Language.ZH
            Language.EN.key -> Language.EN
            Language.VI.key -> Language.VI
            Language.TH.key -> Language.TH
            Language.PHI.key -> Language.PHI
            else -> {
                //若APP local 未設定過語系，就使用系統語系判斷
                val local = getSystemLocale()
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

    fun getSelectLanguageName() = KvUtils.decodeString(TAG_LANGUAGE)

    private fun saveLanguageName(select: String) = KvUtils.put(TAG_LANGUAGE, select)

    fun getLanguageString2(): String {
        return when (getSelectLanguage()) {
            Language.ZH -> "zh"
            Language.VI -> "vi"
            Language.TH -> "th"
            Language.PHI -> "ph"
            else -> "en"
        }
    }

    fun getLanguageString(): String {
        return when (getSelectLanguage()) {
            Language.ZH -> "zh"
            Language.VI -> "vi"
            Language.TH -> "th"
            Language.PHI -> "phi"
            else -> "en"
        }
    }
    fun getLanguageStringWithOutPH(): String {
        return when (getSelectLanguage()) {
            Language.ZH -> "zh"
            Language.VI -> "vi"
            Language.TH -> "th"
            else -> "en"
        }
    }

    private lateinit var selectedLocale: Locale

    /**
     * 获取选择的语言设置
     *
     * @param context
     * @return
     */
    fun getSetLanguageLocale(): Locale {
        if (!::selectedLocale.isInitialized) {
            selectedLocale = convert(getSelectLanguage())
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

    fun saveSystemCurrentLanguage() {
        systemCurrentLocal = getSystemLocal()
    }

    /**
     * 保存系统语言
     * @param newConfig
     */
    fun saveSystemCurrentLanguage(newConfig: Configuration) {
        systemCurrentLocal = getSystemLocal(newConfig)
    }

    fun saveSelectLanguage(context: Context, select: Language) {
        val lastLanguage = getSelectLanguage()
        FirebaseLog.addLogInfo("currentLanguage", select.name) // 在崩溃日志中记录当前的语言类型
        selectedLocale = convert(select)
        saveLanguageName(select.key)
        KvUtils.removeKey("splashAd")
        onConfigurationChanged(context)
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

    fun setLocal(context: Context): Context {
        return updateResources(context, getSetLanguageLocale())
    }


    fun onConfigurationChanged(context: Context): Context {
        setApplicationLanguage(MultiLanguagesApplication.getInstance())
        return setLocal(context)
    }

    private fun updateResources(context: Context, locale: Locale): Context {
        var context = context
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        context = context.createConfigurationContext(config)
        return context
    }

    private fun setApplicationLanguage(context: Context) {
        val application = context.applicationContext
        val resources = application.resources
        val metrics = resources.displayMetrics
        val configuration = resources.configuration
        val locale = getSetLanguageLocale()
        configuration.setLocale(locale);
        configuration.setLocales(LocaleList(locale))
        application.createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, metrics)
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

