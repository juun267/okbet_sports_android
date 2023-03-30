package org.cxct.sportlottery.network.service.match_status_change

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class MatchStatus(
    @Json(name = "homeScore")
    val homeScore: Int? = null,
    @Json(name = "awayScore")
    val awayScore: Int? = null,
    @Json(name = "homePoints")
    val homePoints: String? = null,
    @Json(name = "awayPoints")
    val awayPoints: String? = null,
    @Json(name = "homeCards")
    val homeCards: Int? = null,
    @Json(name = "awayCards")
    val awayCards: Int? = null,
    @Json(name = "statusCode")
    val statusCode: Int? = null,
    @Json(name = "statusName")
    val statusName: String? = null,
    @Json(name = "statusNameI18n")
    val statusNameI18n: Map<String, String>? = mapOf()
)
