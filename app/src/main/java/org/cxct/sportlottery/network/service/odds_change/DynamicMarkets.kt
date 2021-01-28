package org.cxct.sportlottery.network.service.odds_change

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true)
data class DynamicMarkets (
    @Json(name = "zh")
    val zh: String?,
    @Json(name = "en")
    val en: String?
)
