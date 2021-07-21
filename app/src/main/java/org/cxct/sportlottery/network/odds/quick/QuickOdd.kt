package org.cxct.sportlottery.network.odds.quick


import com.squareup.moshi.Json

data class QuickOdd(
    @Json(name = "name")
    val name: String?,
    @Json(name = "odds")
    val odds: List<Odd>?,
    @Json(name = "rowSort")
    val rowSort: Int?,
    @Json(name = "typeCodes")
    val typeCodes: String?
)