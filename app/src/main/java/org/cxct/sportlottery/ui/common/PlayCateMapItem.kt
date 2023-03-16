package org.cxct.sportlottery.ui.common


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class PlayCateMapItem(
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "playCateCode")
    val playCateCode: String,
    @Json(name = "playCateName")
    val playCateName: String,
    @Json(name = "playCateName-english")
    val playCateNameEn: String,
    @Json(name = "playCateName-vietnamese")
    val playCateNameVi: String,
    @Json(name = "playCateNum")
    val playCateNum: Int
)