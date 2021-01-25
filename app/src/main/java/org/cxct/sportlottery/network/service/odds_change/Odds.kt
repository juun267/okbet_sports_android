package org.cxct.sportlottery.network.service.odds_change

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true)
data class Odds (
    val id: String?,
    val spread:String?,
    val odds: Double?,
    val status: Int?, //0:活跃可用，可投注、1：临时锁定，不允许投注、2：不可用，不可见也不可投注
    val producerId: Int?
)
