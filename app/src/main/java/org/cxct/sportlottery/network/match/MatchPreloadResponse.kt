package org.cxct.sportlottery.network.match

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json


@JsonClass(generateAdapter = true)
data class MatchPreloadResponse(
    @Json(name = "code")
    val code: Int,
    @Json(name = "msg")
    val msg: String,
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "t")
    val matchPreloadData: MatchPreloadData
)

@JsonClass(generateAdapter = true)
data class MatchPreloadData(
    @Json(name = "datas")
    val datas: List<Data>,
    @Json(name = "num")
    val num: Int
)

@JsonClass(generateAdapter = true)
data class Data(
    @Json(name = "code")
    val code: String,
    @Json(name = "matchs")
    val matchs: List<Match>
)

@JsonClass(generateAdapter = true)
data class Match(
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "endTime")
    val endTime: String?,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "playCateNum")
    val playCateNum: Int?,
    @Json(name = "startTime")
    val startTime: Long,
    @Json(name = "status")
    val status: Int
)