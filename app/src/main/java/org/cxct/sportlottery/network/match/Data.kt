package org.cxct.sportlottery.network.match


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.list.MatchOdd

@JsonClass(generateAdapter = true)
data class Data(
    @Json(name = "name")
    val name: String,
    @Json(name = "code")
    val code: String,
    @Json(name = "matchs")
    val matchs: List<Match>,
    @Json(name = "matchOdds")
    val matchOdds: List<MatchOdd>,
    @Json(name = "num")
    val num: Int
)