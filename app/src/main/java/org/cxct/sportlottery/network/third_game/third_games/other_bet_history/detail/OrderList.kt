package org.cxct.sportlottery.network.third_game.third_games.other_bet_history.detail


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class OrderList(
    @Json(name = "data")
    val dataList: List<OrderData>?,
    @Json(name = "totalCount")
    val totalCount: Int?
)