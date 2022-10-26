package org.cxct.sportlottery.network.third_game.third_games.hot


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class League(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "categoryIcon")  //国家图标,svg的html元素(已进行UrlEncoded)
    val categoryIcon: String,
    @Json(name = "category") //国家
    val category: String

)