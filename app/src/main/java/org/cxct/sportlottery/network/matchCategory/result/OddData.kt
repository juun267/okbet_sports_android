package org.cxct.sportlottery.network.matchCategory.result

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.Odds
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.ui.common.PlayCateMapItem

@JsonClass(generateAdapter = true)
data class OddData(
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: Map<String, DynamicMarket>?,
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo?,
    @Json(name = "odds")
    override var oddsMap: MutableMap<String, MutableList<Odd?>>,
    @Json(name = "oddsList")
    val oddsList: List<Odd>?,
    @Json(name = "quickPlayCateList")
    override var quickPlayCateList: List<QuickPlayCate>?,
    @Json(name = "oddsSort")
    override val oddsSort: String? = null
) : MatchOdd {
    override val oddsEps: Odds? = null
    override var playCateMappingList: List<PlayCateMapItem>? = null
}