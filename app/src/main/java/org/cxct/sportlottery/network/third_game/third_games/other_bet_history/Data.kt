package org.cxct.sportlottery.network.third_game.third_games.other_bet_history


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Data(
    @Json(name = "orderList")
    val orderList: List<Order>?,
    @Json(name = "totalBet")
    val totalBet: Double?,
    @Json(name = "totalCount")
    val totalCount: Int?,
    @Json(name = "totalWin")
    val totalWin: Double?
)