package org.cxct.sportlottery.network.sport


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SportMenuData(
    @Json(name = "atStart")
    val atStart: List<Sport>,
    @Json(name = "early")
    val early: List<Sport>,
    @Json(name = "inPlay")
    val inPlay: List<Sport>,
    @Json(name = "parlay")
    val parlay: List<Sport>,
    @Json(name = "today")
    val today: List<Sport>
)