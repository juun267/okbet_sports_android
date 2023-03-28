package org.cxct.sportlottery.network.odds.detail


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class PlayCateType(
    @Json(name = "code")
    val code: String,
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "remark")
    val remark: String,
    @Json(name = "sort")
    val sort: Int
)