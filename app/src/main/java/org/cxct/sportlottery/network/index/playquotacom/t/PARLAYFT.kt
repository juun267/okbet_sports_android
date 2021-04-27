package org.cxct.sportlottery.network.index.playquotacom.t


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PARLAYFT(
    @Json(name = "code")
    val code: String,
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "max")
    val max: Int,
    @Json(name = "min")
    val min: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "platformId")
    val platformId: Int
)