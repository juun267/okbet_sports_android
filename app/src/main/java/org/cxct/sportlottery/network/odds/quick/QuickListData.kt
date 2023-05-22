package org.cxct.sportlottery.network.odds.quick


import com.squareup.moshi.Json
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.Odd

@KeepMembers
data class QuickListData(
    @Json(name = "betPlayCateNameMap")
    var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    @Json(name = "playCateNameMap")
    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    @Json(name = "quickOdds")
    val quickOdds: Map<String, MutableMap<String, List<Odd?>?>>?,
    @Json(name = "oddsSort")
    val oddsSortMap: Map<String?, String?>?
)