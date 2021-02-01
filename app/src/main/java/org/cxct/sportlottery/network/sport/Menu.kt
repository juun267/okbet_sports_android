package org.cxct.sportlottery.network.sport


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Menu(
    @Json(name = "inPlay")
    val inPlay: Sport, //滚球
    @Json(name = "today")
    val today: Sport, //今日
    @Json(name = "early")
    val early: Sport, //早盘
    @Json(name = "parlay")
    val parlay: Sport, //串连
    @Json(name = "outright")
    val outright: Sport //冠軍
)