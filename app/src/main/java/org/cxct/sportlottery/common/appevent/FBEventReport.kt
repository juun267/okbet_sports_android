package org.cxct.sportlottery.common.appevent

import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import org.cxct.sportlottery.application.MultiLanguagesApplication

// facebook 事件埋点统计
object FBEventReport {

    private val logger = AppEventsLogger.newLogger(MultiLanguagesApplication.mInstance)


    fun reportEvent(eventName: String, params: Bundle) {
        logger.logEvent(eventName, params)
    }


}