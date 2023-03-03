package org.cxct.sportlottery.common

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import org.cxct.sportlottery.util.LanguageManager
import java.util.*

class ResourceWrapper(private val context: Context, origin: Resources): Resources(origin.assets, origin.displayMetrics, origin.configuration) {

    private var currentLocale: Locale = LanguageManager.getSetLanguageLocale(context)
    private var localeContext: Context = getLocalizedContext(currentLocale)

    override fun getString(id: Int): String {
        checkLocal()
        return localeContext.getString(id)
    }

    override fun getString(id: Int, vararg formatArgs: Any?): String {
        checkLocal()
        return localeContext.getString(id, formatArgs)
    }

    private fun checkLocal() {
        val selectedLocal = LanguageManager.getSetLanguageLocale(context)
        if (currentLocale != selectedLocal) {
            currentLocale = selectedLocal
            localeContext = getLocalizedContext(currentLocale)
        }
    }

    @Synchronized
    private fun getLocalizedContext(selectedLocal: Locale): Context {
        val conf = Configuration(configuration)
        conf.setLocale(selectedLocal)
        return context.createConfigurationContext(conf)
    }
}