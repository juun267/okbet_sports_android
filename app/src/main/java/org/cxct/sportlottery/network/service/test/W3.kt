package org.cxct.sportlottery.network.service.test


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class W3(
    @Json(name = "name")
    val name: String,
    @Json(name = "odds")
    val odds: List<OddXX>,
    @Json(name = "typeCodes")
    val typeCodes: String
)