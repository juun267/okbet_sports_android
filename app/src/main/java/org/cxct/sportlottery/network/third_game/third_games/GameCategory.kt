package org.cxct.sportlottery.network.third_game.third_games


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameCategory(
    @Json(name = "code")
    val code: String?,
    @Json(name = "gameFirmIds")
    val gameFirmIds: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "sort")
    val sort: Double?,
    @Json(name = "typeName")
    val typeName: String?
)