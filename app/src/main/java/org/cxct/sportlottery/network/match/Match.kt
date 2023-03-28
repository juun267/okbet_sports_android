package org.cxct.sportlottery.network.match


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Match(
    @Json(name = "id")
    val id: String,
    @Json(name = "homeName")
    val homeName: String, //主队名称
    @Json(name = "awayName")
    val awayName: String, //客队名称
    @Json(name = "startTime")
    val startTime: Long?, //赛事或赛季开始时间
    @Json(name = "endTime")
    val endTime: Long?, //赛事或赛季结束时间
    @Json(name = "playCateNum")
    val playCateNum: Int?, //该赛事之全部可投注玩法类数量
    @Json(name = "status")
    val status: Int //赛事状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
)