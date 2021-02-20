package org.cxct.sportlottery.network.outright.odds

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DynamicMarket(
    @Json(name = "en")
    val en: String,
    @Json(name = "zh")
    val zh: String
)
