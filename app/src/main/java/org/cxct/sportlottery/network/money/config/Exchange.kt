package org.cxct.sportlottery.network.money.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Exchange(
    @Json(name = "exchangeCurrency")
    val exchangeCurrency: String?,
    @Json(name = "exchangeRate")
    val exchangeRate: Double?
)
