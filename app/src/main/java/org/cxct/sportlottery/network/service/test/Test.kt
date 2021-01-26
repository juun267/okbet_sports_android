package org.cxct.sportlottery.network.service.test


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Test(
    @Json(name = "eventId")
    val eventId: String,
    @Json(name = "eventType")
    val eventType: String,
    @Json(name = "isLongTermEvent")
    val isLongTermEvent: Int,
    @Json(name = "odds")
    val odds: Odds
)