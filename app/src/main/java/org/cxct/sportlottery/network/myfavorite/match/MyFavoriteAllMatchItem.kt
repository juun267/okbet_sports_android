package org.cxct.sportlottery.network.myfavorite.match

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.list.LeagueOdd

@JsonClass(generateAdapter = true)
data class MyFavoriteAllMatchItem(
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "leagueOddsList")
    val leagueOddsList: List<LeagueOdd>,
)

