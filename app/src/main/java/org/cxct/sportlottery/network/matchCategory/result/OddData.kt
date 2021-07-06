package org.cxct.sportlottery.network.matchCategory.result

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.service.odds_change.DynamicMarkets

@JsonClass(generateAdapter = true)
data class OddData(
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: DynamicMarkets?,
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo?,
    @Json(name = "odds")
    val odds: Map<String, Odd>?,
    @Json(name = "oddsList")
    val oddsList: List<Odd>?,
    @Json(name = "quickPlayCateList")
    val quickPlayCateList: List<QuickPlayCate>?
)