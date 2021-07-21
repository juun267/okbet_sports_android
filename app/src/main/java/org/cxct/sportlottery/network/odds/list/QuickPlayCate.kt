package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuickPlayCate(
    @Json(name = "code")
    val code: String?,
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "sort")
    val sort: Int?
)