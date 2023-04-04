package org.cxct.sportlottery.network.service.league_change

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class LeagueChangeEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.LEAGUE_CHANGE,
    @Json(name = "leagueIdList")
    val leagueIdList: List<String>?,
    @Json(name = "matchIdList")
    val matchIdList: List<String>?,
    @Json(name = "gameType")
    val gameType: String?,
    ) : ServiceEventType
