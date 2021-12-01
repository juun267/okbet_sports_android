package org.cxct.sportlottery.network.matchCategory.result


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
const val RECOMMEND_OUTRIGHT = 1
@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "isOutright")
    val isOutright: Int?,
    @Json(name = "leagueOdds")
    val leagueOdds: LeagueOdds?,
    @Json(name = "sport")
    val sport: Sport?
)