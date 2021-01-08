package org.cxct.sportlottery.network.outright.odds


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Odds(
    @Json(name = "T2SMG")
    val t2SMG: List<T2smg>?,
    @Json(name = "TGS")
    val tGS: List<Tgs>?,
    @Json(name = "WINNER")
    val wINNER: List<Winner>?
)