package org.cxct.sportlottery.network.service.match


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchClockCO(
    @Json(name = "eventDate")
    val eventDate: Long,
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "matchId")
    val matchId: String,
    @Json(name = "matchTime")
    val matchTime: Int,
    @Json(name = "remainingTime")
    val remainingTime: Int?,
    @Json(name = "remainingTimeInPeriod")
    val remainingTimeInPeriod: Int?,
    @Json(name = "scheduledTime")
    val scheduledTime: Long,
    @Json(name = "stoppageTime")
    val stoppageTime: Int?,
    @Json(name = "stoppageTimeAnnounced")
    val stoppageTimeAnnounced: Int?,
    @Json(name = "stopped")
    val stopped: Int?
)