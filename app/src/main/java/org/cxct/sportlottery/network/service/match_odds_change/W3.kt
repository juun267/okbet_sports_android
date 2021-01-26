package org.cxct.sportlottery.network.service.match_odds_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class W3(
    @Json(name = "name")
    val name: String,
    @Json(name = "odds")
    val odds: List<Odd>,
    @Json(name = "typeCodes")
    val typeCodes: String
)