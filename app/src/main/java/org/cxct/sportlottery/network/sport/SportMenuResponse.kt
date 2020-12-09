package org.cxct.sportlottery.network.sport

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json


@JsonClass(generateAdapter = true)
data class SportMenuResponse(
    @Json(name = "code")
    val code: Int,
    @Json(name = "msg")
    val msg: String,
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "t")
    val sportMenuData: SportMenuData
)

@JsonClass(generateAdapter = true)
data class SportMenuData(
    @Json(name = "atStart")
    val atStart: List<AtStart>,
    @Json(name = "early")
    val early: List<Early>,
    @Json(name = "inPlay")
    val inPlay: List<InPlay>,
    @Json(name = "parlay")
    val parlay: List<Parlay>,
    @Json(name = "today")
    val today: List<Today>
)

@JsonClass(generateAdapter = true)
data class AtStart(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int,
    @Json(name = "play")
    val play: List<Play>,
    @Json(name = "sortNum")
    val sortNum: Int
)

@JsonClass(generateAdapter = true)
data class Early(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int,
    @Json(name = "play")
    val play: List<PlayX>,
    @Json(name = "sortNum")
    val sortNum: Int
)

@JsonClass(generateAdapter = true)
data class InPlay(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int,
    @Json(name = "play")
    val play: List<PlayXX>?,
    @Json(name = "sortNum")
    val sortNum: Int
)

@JsonClass(generateAdapter = true)
data class Parlay(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int,
    @Json(name = "play")
    val play: List<PlayXXX>,
    @Json(name = "sortNum")
    val sortNum: Int
)

@JsonClass(generateAdapter = true)
data class Today(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int,
    @Json(name = "play")
    val play: List<PlayXXXX>,
    @Json(name = "sortNum")
    val sortNum: Int
)

@JsonClass(generateAdapter = true)
data class Play(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int
)

@JsonClass(generateAdapter = true)
data class PlayX(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int
)

@JsonClass(generateAdapter = true)
data class PlayXX(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int
)

@JsonClass(generateAdapter = true)
data class PlayXXX(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int
)

@JsonClass(generateAdapter = true)
data class PlayXXXX(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int
)