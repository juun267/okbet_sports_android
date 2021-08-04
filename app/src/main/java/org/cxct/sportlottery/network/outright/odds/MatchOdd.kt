package org.cxct.sportlottery.network.outright.odds

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo?,
    @Json(name = "odds")
    val odds: Map<String, List<Odd?>> = mapOf(),
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: Map<String, DynamicMarket>,
    @Json(name = "oddsList")
    val oddsList: List<String?> ?= listOf(), //TODO Cheryl : 目前回傳都是null, 待測試
    @Json(name = "quickPlayCateList")
    val quickPlayCateList: List<QuickPlayCate?> ?= listOf(), //(新)赛事可玩的快捷玩法列表
) {
    var startDate: String = ""
    var startTime: String = ""
}