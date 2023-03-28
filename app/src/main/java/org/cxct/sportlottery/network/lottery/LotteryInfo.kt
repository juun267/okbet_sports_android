package org.cxct.sportlottery.network.lottery

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class LotteryInfo(
    @Json(name = "activityContent")
    val activityContent: String,
    @Json(name = "activityName")
    val activityName: String,
    @Json(name = "activityRule")
    val activityRule: String,
    @Json(name = "allWins")
    val allWins: List<AllWin>? = null,
    @Json(name = "pcBanner")
    val pcBanner: String,
    @Json(name = "h5Banner")
    val h5Banner: String,
    @Json(name = "endTime")
    val endTime: Long?,
    @Json(name = "id")
    val id: Int,
    @Json(name = "lastDrawTime")
    val lastDrawTime: Long?,
    @Json(name = "nextDrawTime")
    val nextDrawTime: Long?,
    @Json(name = "showEndTime")
    val showEndTime: Long?,
    @Json(name = "showStartTime")
    val showStartTime: Long?,
    @Json(name = "startTime")
    val startTime: Long?,
    @Json(name = "tickets")
    val tickets: List<Ticket>? = null,
    @Json(name = "wins")
    val wins: List<Win>? = null,
)
