package org.cxct.sportlottery.util

import android.content.Context
import android.content.res.Configuration
import org.cxct.sportlottery.application.MultiLanguagesApplication

@Deprecated("多语言问题已处理，直接用Context的Resources获取相应的资源就行")
object LocalUtils {


    fun getString(resId:Int): String {
        val localizedContext = getLocalizedContext()
        return localizedContext.resources.getString(resId)
    }

    fun getLocalizedContext(): Context {
        val context = MultiLanguagesApplication.appContext
        val locale = LanguageManager.getSetLanguageLocale()
        var conf = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(locale)
        return context.createConfigurationContext(conf)
    }

}