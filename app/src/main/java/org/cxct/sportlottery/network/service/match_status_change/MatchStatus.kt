package org.cxct.sportlottery.network.service.match_status_change

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchStatus(
    @Json(name = "homeScore")
    val homeScore: Int? = null,
    @Json(name = "awayScore")
    val awayScore: Int? = null
)
