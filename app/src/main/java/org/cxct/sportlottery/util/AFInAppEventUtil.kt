package org.cxct.sportlottery.util

import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import org.cxct.sportlottery.application.MultiLanguagesApplication

object AFInAppEventUtil {
    fun login(uid: String,eventValues: HashMap<String, Any>) {
        eventValues.put(AFInAppEventParameterName.ACHIEVEMENT_ID, uid)
        AppsFlyerLib.getInstance().logEvent(
            MultiLanguagesApplication.getInstance(),
            AFInAppEventType.LOGIN, eventValues)
    }

    fun register(method: String,eventValues: HashMap<String, Any>) {
        eventValues.put(AFInAppEventParameterName.REGSITRATION_METHOD, method)
        AppsFlyerLib.getInstance().logEvent(
            MultiLanguagesApplication.getInstance(),
            AFInAppEventType.COMPLETE_REGISTRATION, eventValues)
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
}