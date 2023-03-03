package org.cxct.sportlottery.common

import android.content.Context
import android.content.res.Resources
import org.cxct.sportlottery.util.LanguageManager

class ResourceWrapper(private val context: Context, origin: Resources): Resources(origin.assets, origin.displayMetrics, origin.configuration) {

    override fun getString(id: Int): String {
        checkLocal()
        return super.getString(id)
    }

    override fun getString(id: Int, vararg formatArgs: Any?): String {
        checkLocal()
        return super.getString(id, *formatArgs)
    }

    private fun checkLocal() {
        val selectedLocal = LanguageManager.getSetLanguageLocale(context)
        if (configuration.locale != selectedLocal) {
            configuration.locale = selectedLocal
        }
    }
}