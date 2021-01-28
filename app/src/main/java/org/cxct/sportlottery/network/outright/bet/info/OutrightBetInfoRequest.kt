package org.cxct.sportlottery.network.outright.bet.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OutrightBetInfoRequest(
    @Json(name = "oddsList")
    val oddsList: List<Odds>,
    @Json(name = "oddsType")
    val oddsType: String
)