package org.cxct.sportlottery.network.matchresult


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "league")
    val league: League,
    @Json(name = "list")
    val list: List<Match>
)