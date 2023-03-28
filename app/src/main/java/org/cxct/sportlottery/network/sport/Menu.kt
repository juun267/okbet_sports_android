package org.cxct.sportlottery.network.sport


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Menu(
    @Json(name = "inPlay")
    val inPlay: Sport, //滚球
    @Json(name = "today")
    val today: Sport, //今日
    @Json(name = "early")
    val early: Sport, //早盘
    @Json(name = "cs")
    val cs: Sport, //波胆
    @Json(name = "parlay")
    val parlay: Sport, //串连
    @Json(name = "outright")
    val outright: Sport, //冠軍
    @Json(name = "eps")
    val eps: Sport?, //特优赔率
    @Json(name = "bkEnd")
    val bkEnd: Sport? // 篮球末位比分
)