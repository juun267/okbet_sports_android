package org.cxct.sportlottery.network.service.record

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true)
@KeepMembers
data class RecordNewEvent(
    @Json(name = "eventType")
    override val eventType: String,
    @Json(name = "player")
    val player: String,
    @Json(name = "games")
    val games: String,
    @Json(name = "betAmount")
    val betAmount: String,
    @Json(name = "profitAmount")
    var profitAmount: String,
    ) : ServiceEventType