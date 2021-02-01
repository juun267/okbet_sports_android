package org.cxct.sportlottery.network.service.match_odds_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.detail.Odd

@JsonClass(generateAdapter = true)
data class Odds(
    @Json(name = "typeCodes")
    val typeCodes: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "odds")
    val odds: MutableList<Odd>,
)