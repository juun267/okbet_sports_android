package org.cxct.sportlottery.network.service.test


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Odds(
    @Json(name = "HDP")
    val hDP: HDP,
    @Json(name = "O/E")
    val oE: OE,
    @Json(name = "W3")
    val w3: W3
)