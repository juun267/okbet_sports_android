package org.cxct.sportlottery.network.service.match_status_change

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchStatus(
    @Json(name = "homeScore")
    val homeScore: Int? = null,
    @Json(name = "awayScore")
    val awayScore: Int? = null,
    @Json(name = "statusCode")
    val statusCode: Int? = null,
    @Json(name = "statusName")
    val statusName: String? = null,
    @Json(name = "statusNameI18n")
    val statusNameI18n: Map<String, String>? = mapOf()
)
