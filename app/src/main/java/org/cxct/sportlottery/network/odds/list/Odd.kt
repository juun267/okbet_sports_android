package org.cxct.sportlottery.network.odds.list

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
    val status: Int //0:活跃可用，可投注、1：临时锁定，不允许投注、2：不可用，不可见也不可投注
)