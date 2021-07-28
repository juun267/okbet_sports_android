package org.cxct.sportlottery.network.odds.quick


import com.squareup.moshi.Json

data class QuickListData(
    @Json(name = "quickOdds")
    val quickOdds: Map<String, Map<String, QuickOdd?>>?
)