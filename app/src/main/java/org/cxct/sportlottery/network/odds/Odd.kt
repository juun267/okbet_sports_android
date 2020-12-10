package org.cxct.sportlottery.network.odds

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Odd(
    @Json(name = "id")
    val id: String,
    @Json(name = "odds")
    val odds: Double,
    @Json(name = "producerId")
    val producerId: Int,
    @Json(name = "spread")
    val spread: String?,
    @Json(name = "status")
    val status: Int
)
