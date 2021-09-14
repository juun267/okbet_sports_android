package org.cxct.sportlottery.ui.common


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayCateMapItem(
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "menuCode")
    val menuCode: String,
    @Json(name = "playCateCode")
    val playCateCode: String,
    @Json(name = "playCateName")
    val playCateName: String,
    @Json(name = "playCateNameEn")
    val playCateNameEn: String,
    @Json(name = "playCateNum")
    val playCateNum: String
)