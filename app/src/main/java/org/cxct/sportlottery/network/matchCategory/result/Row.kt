package org.cxct.sportlottery.network.matchCategory.result


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Row(
    @Json(name = "isOutright")
    val isOutright: Int?,
    @Json(name = "leagueOdds")
    val leagueOdds: LeagueOdds?,
    @Json(name = "sport")
    val sport: Sport?
)