package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.MatchInfo

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo? = null,
    @Json(name = "odds")
    var odds: MutableMap<String, MutableList<Odd>> = mutableMapOf(PlayType.HDP.code to mutableListOf(),
                                                                  PlayType.OU.code to mutableListOf(),
                                                                  PlayType.X12.code to mutableListOf())
) {
    var isExpand = false
}