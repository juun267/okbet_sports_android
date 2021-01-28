package org.cxct.sportlottery.network.outright.bet.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ParlayOdd(
    @Json(name = "display")
    val display: Int,
    @Json(name = "max")
    val max: Int,
    @Json(name = "min")
    val min: Int,
    @Json(name = "num")
    val num: Int,
    @Json(name = "odds")
    val odds: Int,
    @Json(name = "parlayType")
    val parlayType: String
)