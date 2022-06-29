package org.cxct.sportlottery.util

import android.content.Context
import android.content.res.Configuration
import org.cxct.sportlottery.MultiLanguagesApplication

object LocalUtils {
    fun getString(resId:Int): String {
        val localizedContext = getLocalizedContext()
        return localizedContext.resources.getString(resId)
    }

    fun getStringArray(resId:Int): Array<String>{
        val localizedContext = getLocalizedContext()
        return localizedContext.resources.getStringArray(resId)
    }

    fun getLocalizedContext(): Context {
        val context = MultiLanguagesApplication.appContext
        val locale = LanguageManager.getSetLanguageLocale(context)
        var conf = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(locale)
        return context.createConfigurationContext(conf)
    }
}