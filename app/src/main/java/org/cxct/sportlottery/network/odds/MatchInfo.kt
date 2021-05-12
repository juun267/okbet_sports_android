package org.cxct.sportlottery.network.odds


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchInfo(
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "endTime")
    val endTime: String?,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "id")
    val id: String, //赛事或赛季id
    @Json(name = "playCateNum")
    val playCateNum: Int,
    @Json(name = "startTime")
    val startTime: String,
    @Json(name = "status")
    var status: Int
) {
    //Live
    var awayScore: Int? = null //客队分数
    var homeScore: Int? = null //主队分数
    var statusName: String? = null //状态名称

    //At Start
    var remainTime: Long? = null

    //Other
    var startDateDisplay: String? = null
    var startTimeDisplay: String? = null
}