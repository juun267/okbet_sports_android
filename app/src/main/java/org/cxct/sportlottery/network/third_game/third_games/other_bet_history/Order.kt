package org.cxct.sportlottery.network.third_game.third_games.other_bet_history


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Order(
    @Json(name = "betAmount")
    val betAmount: Double?,
    @Json(name = "betCount")
    val betCount: Int?,
    @Json(name = "firmType")
    val firmType: String?,
    @Json(name = "gameName")
    val gameName: String?,
    @Json(name = "netAmount")
    val netAmount: Int?,
    @Json(name = "statDate")
    val statDate: String?,
    @Json(name = "userCount")
    val userCount: Int?,
    @Json(name = "validBetAmount")
    val validBetAmount: Int?
)