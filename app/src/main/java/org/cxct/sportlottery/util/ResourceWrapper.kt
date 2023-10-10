package org.cxct.sportlottery.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.*

class ResourceWrapper(private val context: Context, origin: Resources): Resources(origin.assets, origin.displayMetrics, origin.configuration) {

    private var currentLocale: Locale = LanguageManager.getSetLanguageLocale(context)

    override fun getString(id: Int): String {
        checkLocal()
        return super.getString(id)
    }

    private fun checkLocal() {
        val selectedLocal = LanguageManager.getSetLanguageLocale(context)
        if (currentLocale != selectedLocal) {
            currentLocale = selectedLocal
            getLocalizedContext(currentLocale)
        }
    }

    @Synchronized
    private fun getLocalizedContext(selectedLocal: Locale): Context {
        val conf = Configuration(configuration)
        conf.setLocale(selectedLocal)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(selectedLocal)
            LocaleList.setDefault(localeList)
            conf.setLocales(localeList)
            Locale.setDefault(selectedLocal)
        }


        updateConfiguration(conf, displayMetrics)
        return context.createConfigurationContext(conf)
    }

    override fun getInteger(id: Int): Int {
        return try {
            super.getInteger(id)

        } catch (e: Exception) {
            try {
                context.resources.getInteger(id)
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }

        }

    }


}