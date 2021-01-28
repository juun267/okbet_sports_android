package org.cxct.sportlottery.network.outright.add


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Stake(
    @Json(name = "parlayType")
    val parlayType: String,
    @Json(name = "stake")
    val stake: Int
)