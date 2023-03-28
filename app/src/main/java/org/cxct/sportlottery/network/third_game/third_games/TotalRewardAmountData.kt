package org.cxct.sportlottery.network.third_game.third_games


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class TotalRewardAmountData(
    @Json(name = "name")
    val name: String?,
    @Json(name = "amount")
    val amount: String,
    @Json(name = "currency")
    val currency: String,
)