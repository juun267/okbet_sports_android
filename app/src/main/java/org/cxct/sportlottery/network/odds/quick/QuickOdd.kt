package org.cxct.sportlottery.network.odds.quick


import com.squareup.moshi.Json
import org.cxct.sportlottery.network.odds.Odd

data class QuickOdd(
    @Json(name = "name")
    val name: String?,
    @Json(name = "odds")
    var odds: List<Odd>?,
    @Json(name = "rowSort")
    val rowSort: Int?,
    @Json(name = "typeCodes")
    val typeCodes: String?
)