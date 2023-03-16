package org.cxct.sportlottery.network.odds.detail


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.network.odds.League

@JsonClass(generateAdapter = true) @KeepMembers
data class OddsDetailData(
    @Json(name = "league")
    val league: League,
    @Json(name = "matchOdds")
    val matchOdd: MatchOdd
)