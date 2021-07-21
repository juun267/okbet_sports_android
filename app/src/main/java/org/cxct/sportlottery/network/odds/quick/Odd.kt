package org.cxct.sportlottery.network.odds.quick


import com.squareup.moshi.Json

data class Odd(
    @Json(name = "extInfo")
    val extInfo: String?,
    @Json(name = "hkOdds")
    val hkOdds: Double?,
    @Json(name = "id")
    val id: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "odds")
    val odds: Double?,
    @Json(name = "producerId")
    val producerId: Int?,
    @Json(name = "spread")
    val spread: String?,
    @Json(name = "status")
    val status: Int?
)