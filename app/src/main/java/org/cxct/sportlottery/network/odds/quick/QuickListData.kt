package org.cxct.sportlottery.network.odds.quick


import com.squareup.moshi.Json
import org.cxct.sportlottery.network.odds.Odd

data class QuickListData(
    @Json(name = "quickOdds")
    val quickOdds: Map<String, MutableMap<String, List<Odd?>?>>?
)