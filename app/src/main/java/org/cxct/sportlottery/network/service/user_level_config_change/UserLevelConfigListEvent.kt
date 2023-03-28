package org.cxct.sportlottery.network.service.user_level_config_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class UserLevelConfigListEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.USER_LEVEL_CONFIG_CHANGE.value,
    @Json(name = "userLevelConfigList")
    val userLevelConfigList: List<UserLevelConfigList>?,
) : ServiceEventType