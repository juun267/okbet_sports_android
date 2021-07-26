package org.cxct.sportlottery.network.odds.list

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.detail.Odd

@JsonClass(generateAdapter = true)
data class Odds(
    @Json(name = "EPS")
    val eps: List<Odd>?)