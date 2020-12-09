package org.cxct.sportlottery.network.odds

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json


@JsonClass(generateAdapter = true)
data class OddsListResponse(
    @Json(name = "code")
    val code: Int,
    @Json(name = "msg")
    val msg: String,
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "t")
    val oddsListData: OddsListData
)

@JsonClass(generateAdapter = true)
data class OddsListData(
    @Json(name = "leagueOdds")
    val leagueOdds: List<LeagueOdd>,
    @Json(name = "sport")
    val sport: Sport
)

@JsonClass(generateAdapter = true)
data class LeagueOdd(
    @Json(name = "league")
    val league: League,
    @Json(name = "matchOdds")
    val matchOdds: List<MatchOdd>
)

@JsonClass(generateAdapter = true)
data class Sport(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String
)

@JsonClass(generateAdapter = true)
data class League(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String
)

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo,
    @Json(name = "odds")
    val odds: Map<String, List<Odd>>
)

@JsonClass(generateAdapter = true)
data class MatchInfo(
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "endTime")
    val endTime: String?,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "playCateNum")
    val playCateNum: Int,
    @Json(name = "startTime")
    val startTime: String,
    @Json(name = "status")
    val status: Int
)

@JsonClass(generateAdapter = true)
data class Odd(
    @Json(name = "id")
    val id: String,
    @Json(name = "odds")
    val odds: Double,
    @Json(name = "producerId")
    val producerId: Int,
    @Json(name = "spread")
    val spread: String?,
    @Json(name = "status")
    val status: Int
)