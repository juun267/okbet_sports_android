package org.cxct.sportlottery.network.service.close_play_cate

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class ClosePlayCateEvent(
    @Json(name = "eventType")
    override val eventType: String,
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "playCateCode")
    val playCateCode: String,
) : ServiceEventType