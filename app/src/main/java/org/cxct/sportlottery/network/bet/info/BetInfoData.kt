package org.cxct.sportlottery.network.bet.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class BetInfoData(
    @Json(name = "matchOdds")
    val matchOdds: List<MatchOdd>,
    @Json(name = "parlayOdds")
    val parlayOdds: List<ParlayOdd>
)