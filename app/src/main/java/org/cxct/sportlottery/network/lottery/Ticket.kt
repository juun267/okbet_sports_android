package org.cxct.sportlottery.network.lottery

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Ticket(
    @Json(name = "id")
    val id: Int,
    @Json(name = "ticketCode")
    val ticketCode: String,
    @Json(name = "ticketStatus")
    val ticketStatus: Int,
)
