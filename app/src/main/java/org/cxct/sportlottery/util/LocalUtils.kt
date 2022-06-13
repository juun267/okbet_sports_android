package org.cxct.sportlottery.util

import android.content.Context
import android.content.res.Configuration
import org.cxct.sportlottery.MultiLanguagesApplication

object LocalUtils {
    fun getString(resId:Int): String {
        var context=MultiLanguagesApplication.appContext
        val locale = LanguageManager.getSetLanguageLocale(context)
        var conf = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(locale)
        val localizedContext = context.createConfigurationContext(conf)
        return localizedContext.resources.getString(resId)
    }
}