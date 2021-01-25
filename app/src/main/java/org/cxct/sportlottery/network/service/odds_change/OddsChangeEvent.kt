package org.cxct.sportlottery.network.service.odds_change

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true)
data class OddsChangeEvent(
    @Json(name = "eventType")
    override val eventType: String = EventType.ODDS_CHANGE.value,
    @Json(name = "isLongTermEvent")
    val isLongTermEvent: Int?, //是否是冠军玩法，1：是，0：否
    @Json(name = "odds")
    val odds: Map<String, Odds>? = mapOf() //key=>玩法类型code, value=>赔率列表
): ServiceEventType
