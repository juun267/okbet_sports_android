package org.cxct.sportlottery.network.matchCategory.result

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.DynamicMarket

@JsonClass(generateAdapter = true)
data class OddData(
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: Map<String, DynamicMarket>?,
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo?,
    @Json(name = "odds")
    var odds: Map<String, List<Odd>>?,
    @Json(name = "oddsList")
    val oddsList: List<Odd>?,
    @Json(name = "quickPlayCateList")
    val quickPlayCateList: List<QuickPlayCate>?
)