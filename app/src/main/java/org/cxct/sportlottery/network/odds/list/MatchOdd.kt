package org.cxct.sportlottery.network.odds.list


import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.eps.Odds
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.ui.common.PlayCateMapItem

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "betPlayCateNameMap")
    override val betPlayCateNameMap: Map<String?, Map<String?, String?>?>? = null,
    @Json(name = "playCateNameMap")
    override val playCateNameMap: Map<String?, Map<String?, String?>?>? = null,
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo? = null,
    @Json(name = "odds")
    override var oddsMap: MutableMap<String, MutableList<Odd?>?> = mutableMapOf(
        PlayCate.HDP.value to mutableListOf(),
        PlayCate.OU.value to mutableListOf(),
        PlayCate.SINGLE.value to mutableListOf()
    ),
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: Map<String, DynamicMarket>? = null,
    @Json(name = "quickPlayCateList")
    override val quickPlayCateList: MutableList<QuickPlayCate>? = null,
    @Json(name = "oddsSort")
    override val oddsSort: String? = null
) : MatchOdd {

    override val oddsEps: Odds? = null

    override var playCateMappingList: List<PlayCateMapItem>? = null

    var isExpand = false
    var leagueTime: Int? = null

    var positionButtonPage = 0

    fun sortOddsMap() {
        this.oddsMap.forEach { (_, value) ->
            if (value?.size!! > 3 && value.first()?.marketSort != 0 && (value.first()?.odds != value.first()?.malayOdds)) {
                value?.sortBy {
                    it?.marketSort
                }
            }
        }
    }
}