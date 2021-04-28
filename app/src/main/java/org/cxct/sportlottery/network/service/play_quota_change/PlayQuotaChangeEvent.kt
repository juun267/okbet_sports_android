package org.cxct.sportlottery.network.service.play_quota_change

import com.squareup.moshi.Json
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuotaComData
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

data class PlayQuotaChangeEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.PLAY_QUOTA_CHANGE.value,
    @Json(name = "t")
    val playQuotaComData: PlayQuotaComData
) : ServiceEventType
