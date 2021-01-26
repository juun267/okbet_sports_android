package org.cxct.sportlottery.network.service.match_odds_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Odds(
    /*
    @Json(name = "HDP")
    val hDP: HDP,
    @Json(name = "O/E")
    val oE: OE,
    @Json(name = "W3")
    val w3: W3
*/
    @Json(name = "typeCodes")
    val typeCodes: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "odds")
    val odds: List<Odd>,
)