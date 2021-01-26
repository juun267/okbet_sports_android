package org.cxct.sportlottery.network.service.test


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OddXX(
    @Json(name = "extInfo")
    val extInfo: Any,
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "odds")
    val odds: Int,
    @Json(name = "producerId")
    val producerId: Int,
    @Json(name = "spread")
    val spread: String,
    @Json(name = "status")
    val status: Int
)