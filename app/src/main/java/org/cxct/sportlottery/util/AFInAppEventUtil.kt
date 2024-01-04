package org.cxct.sportlottery.util

import android.os.Bundle
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import org.cxct.sportlottery.application.MultiLanguagesApplication
import splitties.bundle.put

object AFInAppEventUtil {
    fun login(uid: String,eventValues: HashMap<String, Any>) {
        eventValues.put(AFInAppEventParameterName.ACHIEVEMENT_ID, uid)
        AppsFlyerLib.getInstance().logEvent(
            MultiLanguagesApplication.getInstance(),
            AFInAppEventType.LOGIN, eventValues)

        Firebase.analytics.logEvent(AFInAppEventParameterName.ACHIEVEMENT_ID,eventValues.toBundle())
    }

    fun register(method: String,eventValues: HashMap<String, Any>) {
        eventValues.put(AFInAppEventParameterName.REGSITRATION_METHOD, method)
        AppsFlyerLib.getInstance().logEvent(
            MultiLanguagesApplication.getInstance(),
            AFInAppEventType.COMPLETE_REGISTRATION, eventValues)

        Firebase.analytics.logEvent(AFInAppEventParameterName.REGSITRATION_METHOD,eventValues.toBundle())
    }

    fun regAndLogin(eventValues: HashMap<String, Any>) {
        AppsFlyerLib.getInstance().logEvent(
            MultiLanguagesApplication.getInstance(),
            "reportLoginInfo", eventValues)
        Firebase.analytics.logEvent("reportLoginInfo",eventValues.toBundle())
    }

    fun logEvent(name: String,eventName: String,eventValues: String) {
        AppsFlyerLib.getInstance().logEvent(MultiLanguagesApplication.getInstance(), name, mapOf(eventName to eventValues))
        Firebase.analytics.logEvent(name,mapOf(eventName to eventValues).toBundle())
    }



    fun deposit(revenue: String, currency: String) {
//        val eventValues = HashMap<String, Any>()
//        eventValues.put(AFInAppEventParameterName.REVENUE, revenue)
//        eventValues.put(AFInAppEventParameterName.CURRENCY, currency)
//        AppsFlyerLib.getInstance()
//            .logEvent(MultiLanguagesApplication.getInstance(), "deposit", eventValues)
    }

    fun withdrawal(revenue: String, currency: String) {
//        val eventValues = HashMap<String, Any>()
//        eventValues.put(AFInAppEventParameterName.REVENUE, revenue)
//        eventValues.put(AFInAppEventParameterName.CURRENCY, currency)
//        AppsFlyerLib.getInstance().logEvent(MultiLanguagesApplication.getInstance(),
//            "withdrawal", eventValues)
    }
    private fun Map<String,Any>.toBundle():Bundle{
        return Bundle().apply {
            this@toBundle.forEach {
                this.put(it.key,it.value)
            }
        }
    }
}