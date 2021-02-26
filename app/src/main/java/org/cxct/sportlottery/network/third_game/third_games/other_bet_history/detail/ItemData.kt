package org.cxct.sportlottery.network.third_game.third_games.other_bet_history.detail


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemData(
    @Json(name = "orderList")
    val orderList: OrderList?,
    @Json(name = "totalBet")
    val totalBet: Int?,
    @Json(name = "totalCount")
    val totalCount: Int?,
    @Json(name = "totalWin")
    val totalWin: Int?
)