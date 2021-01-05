package org.cxct.sportlottery.network.sport


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Menu(
    @Json(name = "inPlay")
    val inPlay: Sport,
    @Json(name = "today")
    val today: Sport,
    @Json(name = "early")
    val early: Sport,
    @Json(name = "parlay")
    val parlay: Sport,
    @Json(name = "outright")
    val outright: Sport
)