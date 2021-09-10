package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.eps.Odds
import org.cxct.sportlottery.network.outright.odds.DynamicMarket

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo? = null,
    @Json(name = "odds")
    override var odds: MutableMap<String, MutableList<Odd?>> = mutableMapOf(
        PlayCate.HDP.value to mutableListOf(),
        PlayCate.OU.value to mutableListOf(),
        PlayCate.SINGLE.value to mutableListOf()
    ),
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: Map<String, DynamicMarket>? = null,
    @Json(name = "quickPlayCateList")
    override val quickPlayCateList: List<QuickPlayCate>? = null,
    @Json(name = "oddsSort")
    override val oddsSort: String? = null
) : MatchOdd {
    override val oddsEps: Odds? = null

    var isExpand = false
    var leagueTime: Int? = null

    var positionButtonPage = 0
}