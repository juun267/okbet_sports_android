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
    @Json(name = "hkOdds")
    var hkOdds: Double?,
    @Json(name = "parlayType")
    val parlayType: String
) {
    var sendOutStatus: Boolean = true
    var input: String? = null
    var betAmount: Double = 0.0
    var allSingleInput: String? = null //僅給投注單填充所有單注使用
}
