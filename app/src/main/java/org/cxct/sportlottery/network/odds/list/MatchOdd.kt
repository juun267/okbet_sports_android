package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.service.match_status_change.MatchStatus

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo? = null,
    @Json(name = "odds")
    var odds: MutableMap<String, MutableList<Odd?>> = mutableMapOf(
        PlayType.HDP.code to mutableListOf(),
        PlayType.OU.code to mutableListOf(),
        PlayType.X12.code to mutableListOf()
    ),
    @Json(name = "quickPlayCateList")
    val quickPlayCateList: List<QuickPlayCate>? = null

) {
    var isExpand = false
    var leagueTime: Int? = null
    var matchStatusList: List<MatchStatus> = listOf()
}