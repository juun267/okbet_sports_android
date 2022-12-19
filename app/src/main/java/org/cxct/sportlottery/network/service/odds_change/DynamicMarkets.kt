package org.cxct.sportlottery.network.service.odds_change

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LocalUtils

@JsonClass(generateAdapter = true)
data class DynamicMarkets (
    @Json(name = "zh")
    val zh: String?,
    @Json(name = "en")
    val en: String?
)

fun DynamicMarkets.get(): String {
    return when (LanguageManager.getSelectLanguage(LocalUtils.getLocalizedContext())) {
        LanguageManager.Language.ZH -> zh
//        LanguageManager.Language.VI -> vi
//        LanguageManager.Language.TH -> th
        else -> en
    } ?: ""
}