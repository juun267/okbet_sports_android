package org.cxct.sportlottery.network.odds.eps


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchInfo(
    @Json(name = "id")
    val id: String,
    @Json(name = "leagueId")
    val leagueId: String,
    @Json(name = "leagueName")
    val leagueName: String?,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "startTime")
    val startTime: String,
    @Json(name = "endTime")
    val endTime: String?,
    @Json(name = "status")
    var status: Int,//赛事状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
    @Json(name = "playCateNum")
    val playCateNum: Int,//该赛事之全部可投注玩法类数量
    @Json(name = "name")
    val name: String?,//賽季名稱
    @Json(name = "img")
    val img: String?,//圖片
    @Json(name = "msg")
    val msg: String?//訊息
)