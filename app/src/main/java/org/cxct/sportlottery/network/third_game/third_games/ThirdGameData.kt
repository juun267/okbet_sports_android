package org.cxct.sportlottery.network.third_game.third_games


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class ThirdGameData(
    @Json(name = "gameCategories")
    val gameCategories: List<GameCategory>?= listOf(),
    @Json(name = "gameFirmMap")
    val gameFirmMap: Map<String, GameFirmValues>?= mapOf(),
    @Json(name = "thirdDictMap")
    val thirdDictMap: Map<String, List<ThirdDictValues?>?>?= mapOf()
)