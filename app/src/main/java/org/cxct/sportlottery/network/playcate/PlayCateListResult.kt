package org.cxct.sportlottery.network.playcate


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayCateListResult(
    @Json(name = "code")
    val code: Int,
    @Json(name = "msg")
    val msg: String,
    @Json(name = "rows")
    val rows: List<Row>,
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "total")
    val total: Int
)