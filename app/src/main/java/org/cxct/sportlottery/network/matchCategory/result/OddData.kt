package org.cxct.sportlottery.network.matchCategory.result

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.Odds
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.ui.common.PlayCateMapItem

@JsonClass(generateAdapter = true)
data class OddData(
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: Map<String, DynamicMarket>?,
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo?,
    @Json(name = "odds")
    override val oddsMap: MutableMap<String, MutableList<Odd?>?>,
    @Json(name = "oddsList")
    val oddsList: List<Odd>?,
    @Json(name = "quickPlayCateList")
    override val quickPlayCateList: MutableList<QuickPlayCate>?,
    @Json(name = "oddsSort")
    override val oddsSort: String? = null,
    @Json(name = "betPlayCateNameMap")
    override val betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
    @Json(name = "playCateNameMap")
    override val playCateNameMap: Map<String?, Map<String?, String?>?>?,
) : MatchOdd {
    override val oddsEps: Odds? = null
    override var playCateMappingList: List<PlayCateMapItem>? = null
    override var stopped: Int? = null//賽事是否暫停倒數计时 1:是 ，0：否

    fun sortOddsMap() {
        this.oddsMap.forEach { (_, value) ->
            if (value?.size!! > 2 && value.first()?.marketSort != 0) {
                value?.sortBy { it?.marketSort }
            }
        }
    }
}