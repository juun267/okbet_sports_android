package org.cxct.sportlottery.network.bettingStation


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BettingStation(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String
)