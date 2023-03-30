package org.cxct.sportlottery.network.myfavorite

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class MyFavorite(
    @Json(name = "playCate")
    val playCate: List<PlayCate>?,
    @Json(name = "league")
    val league: String?,
    @Json(name = "match")
    val match: String?,
    @Json(name = "outright")
    val outright: String?,
    @Json(name = "sport")
    val sport: String?
)