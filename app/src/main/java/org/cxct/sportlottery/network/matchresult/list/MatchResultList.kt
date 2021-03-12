package org.cxct.sportlottery.network.matchresult.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.matchresult.list.League
import org.cxct.sportlottery.network.matchresult.list.Match

@JsonClass(generateAdapter = true)
data class MatchResultList(
    @Json(name = "league")
    val league: League,
    @Json(name = "list")
    val list: List<Match>
)