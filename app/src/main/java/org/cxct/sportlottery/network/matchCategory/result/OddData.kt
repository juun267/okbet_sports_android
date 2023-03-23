package org.cxct.sportlottery.network.matchCategory.result

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.util.sortOddsMap

@JsonClass(generateAdapter = true)
@KeepMembers
data class OddData(
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: Map<String, DynamicMarket>?,
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo?,
    @Json(name = "odds")
    override var oddsMap: MutableMap<String, MutableList<Odd>?>?,
    @Json(name = "oddsList")
    val oddsList: List<Odd>?,
    @Json(name = "quickPlayCateList")
    override var quickPlayCateList: MutableList<QuickPlayCate>?,
    @Json(name = "oddsSort")
    override val oddsSort: String? = null,
    @Json(name = "betPlayCateNameMap")
    override var betPlayCateNameMap:  MutableMap<String?, Map<String?, String?>?>?,
    @Json(name = "playCateNameMap")
    override var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
) : MatchOdd {
    override val oddsEps: EpsOdd? = null

    fun sortOddsMap() {
        this.oddsMap?.sortOddsMap(2)
    }
}