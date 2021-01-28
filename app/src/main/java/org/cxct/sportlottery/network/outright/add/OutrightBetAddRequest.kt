package org.cxct.sportlottery.network.outright.add


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OutrightBetAddRequest(
    @Json(name = "loginSrc")
    val loginSrc: Int?,
    @Json(name = "oddsChangeOption")
    val oddsChangeOption: Int?,
    @Json(name = "oddsList")
    val oddsList: List<Odds>,
    @Json(name = "oddsType")
    val oddsType: String,
    @Json(name = "stakeList")
    val stakeList: List<Stake>?
)