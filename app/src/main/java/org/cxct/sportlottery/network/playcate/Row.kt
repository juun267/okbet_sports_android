package org.cxct.sportlottery.network.playcate


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "code")
    val code: String,
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "remark")
    val remark: String,
    @Json(name = "sort")
    val sort: Int
)