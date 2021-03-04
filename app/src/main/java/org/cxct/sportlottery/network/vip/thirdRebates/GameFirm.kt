package org.cxct.sportlottery.network.vip.thirdRebates


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameFirm(
    @Json(name = "enableDemo")
    val enableDemo: Int,
    @Json(name = "firmCode")
    val firmCode: String,
    @Json(name = "firmName")
    val firmName: String,
    @Json(name = "firmShowName")
    val firmShowName: String,
    @Json(name = "firmType")
    val firmType: String,
    @Json(name = "iconUrl")
    val iconUrl: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "open")
    val `open`: Int,
    @Json(name = "pageUrl")
    val pageUrl: String,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "playCode")
    val playCode: String,
    @Json(name = "sort")
    val sort: Double,
    @Json(name = "sysOpen")
    val sysOpen: Int
)