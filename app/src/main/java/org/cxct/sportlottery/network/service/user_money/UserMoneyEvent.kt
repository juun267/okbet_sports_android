package org.cxct.sportlottery.network.service.user_money

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class UserMoneyEvent(
    @Json(name = "eventType")
    override val eventType: String = EventType.USER_MONEY.value,
    @Json(name = "money")
    val money: Double?,
): ServiceEventType