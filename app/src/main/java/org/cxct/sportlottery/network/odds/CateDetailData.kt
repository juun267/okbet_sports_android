package org.cxct.sportlottery.network.odds


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CateDetailData(
    @Json(name = "name")
    val name: String,
    @Json(name = "odds")
    val odds: List<Odd>,
    @Json(name = "typeCodes")
    val typeCodes: String
)