package org.cxct.sportlottery.network.third_game.third_games


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameFirmValues(
    @Json(name = "enableDemo")
    val enableDemo: Int?, //试玩状态，0： 不支持1：支持关闭 2：支持开启
    @Json(name = "firmCode")
    val firmCode: String?,
    @Json(name = "firmName")
    val firmName: String?,
    @Json(name = "firmShowName")
    val firmShowName: String?,
    @Json(name = "firmType")
    val firmType: String?,
    @Json(name = "iconUrl")
    val iconUrl: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "open")
    val open: Int?, //平台开关状态,0-关闭，1-开启
    @Json(name = "pageUrl")
    val pageUrl: String?,
    @Json(name = "platformId")
    val platformId: Int?,
    @Json(name = "playCode")
    val playCode: String?,
    @Json(name = "sort")
    val sort: Double?,
    @Json(name = "sysOpen")
    val sysOpen: Int
)