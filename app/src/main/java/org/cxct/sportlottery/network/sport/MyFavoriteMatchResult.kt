package org.cxct.sportlottery.network.sport

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true)
data class MyFavoriteMatchResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "rows")
    val rows: List<RowsItem>?,
    @Json(name = "total")
    val total: Int?
) : BaseResult()

data class RowsItem(
    val league: League,
    val matchOdds: List<MatchOddsItem>?
)

data class League(
    val name: String,
    val id: String,
    val category: String
)

data class MatchInfo(
    val leagueName: String,
    val awayName: String,
    val leagueId: String,
    val homeName: String,
    val startTime: String,
    val id: String,
    val endTime: String,
    val playCateNum: Int,
    val status: Int
)

data class OddsListItem(
    val id: String,
    val name: String,
    val spread: String,
    val extInfo: String,
    val odds: Double,
    val hkOdds: Double,
    val status: Int,
    val producerId: Int
)

data class DynamicMarkets(val mapKey: String)

data class MatchOddsItem(
    val matchInfo: MatchInfo,
    val oddsList: List<OddsListItem>?,
    val dynamicMarkets: DynamicMarkets
)


