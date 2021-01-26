package org.cxct.sportlottery.network.service.test


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OE(
    @Json(name = "name")
    val name: String,
    @Json(name = "odds")
    val odds: List<OddX>,
    @Json(name = "typeCodes")
    val typeCodes: String
)