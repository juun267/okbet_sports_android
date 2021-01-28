package org.cxct.sportlottery.network.outright.bet.add


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "eventType")
    val eventType: Int,
    @Json(name = "matchOdds")
    val matchOdds: List<MatchOdd>,
    @Json(name = "orderNo")
    val orderNo: String,
    @Json(name = "parlay")
    val parlay: Int,
    @Json(name = "parlayType")
    val parlayType: String,
    @Json(name = "stake")
    val stake: Int,
    @Json(name = "status")
    val status: Int
)