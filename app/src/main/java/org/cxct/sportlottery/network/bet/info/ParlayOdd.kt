package org.cxct.sportlottery.network.bet.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ParlayOdd(
    @Json(name = "max")
    val max: Int,
    @Json(name = "min")
    val min: Int,
    @Json(name = "num")
    val num: Int,
    @Json(name = "odds")
    val odds: Double,
    @Json(name = "parlayType")
    val parlayType: String
) {
    var sendOutStatus: Boolean = true
}
