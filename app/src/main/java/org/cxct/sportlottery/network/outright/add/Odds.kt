package org.cxct.sportlottery.network.outright.add


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Odds(
    @Json(name = "odds")
    val odds: Int,
    @Json(name = "oid")
    val oid: String
)