package org.cxct.sportlottery.network.third_game.third_games.hot


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchInfo(
    @Json(name = "id")//赛事或赛季id
    val id: String,
     @Json(name = "leagueId")//联赛ID
    val leagueId: String,
    @Json(name = "leagueName")//联赛名称
    val leagueName: String,


    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "endTime")
    val endTime: String?,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "playCateNum")//该赛事之全部可投注玩法类数量
    val playCateNum: Int?,
    @Json(name = "startTime")
    val startTime: Long,
    @Json(name = "status")
    val status: Int,
    @Json(name = "img")
    val img: Long,
    @Json(name = "msg")
    val msg: Long,
    @Json(name = "homeIcon")
    val homeIcon: Long,
    @Json(name = "homeIconSvg")
    val homeIconSvg: Long,
    @Json(name = "awayIcon")
    val awayIcon: Long,
    @Json(name = "awayIconSvg")
    val awayIconSvg: Long,

    @Json(name = "roundNo")//房间号
    val roundNo: Long,
    @Json(name = "streamerIcon")//主播头像
    val streamerIcon: Long,
    @Json(name = "StreamerName")//主播名称
    val StreamerName: Long,
     @Json(name = "frontCoverUrl")//主播名称
    val frontCoverUrl: Long,
     @Json(name = "gameType")//游戏类型
    val gameType: Long,
    @Json(name = "source")//数据源
    val source: Long


)