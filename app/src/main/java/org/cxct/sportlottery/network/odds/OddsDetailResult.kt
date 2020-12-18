package org.cxct.sportlottery.network.odds


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OddsDetailResult(
    @Json(name = "code")
    val code: Int,
    @Json(name = "msg")
    val msg: String,
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "t")
    val oddsDetailData: OddsDetailData?
)