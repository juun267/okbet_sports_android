package org.cxct.sportlottery.network.service.match


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchDisposableItem(
    @Json(name = "eventType")
    val eventType: String,
    @Json(name = "matchClockCO")
    val matchClockCO: MatchClockCO,
    @Json(name = "matchStatusCO")
    val matchStatusCO: MatchStatusCO
)