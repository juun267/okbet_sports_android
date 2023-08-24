package org.cxct.sportlottery.network.service.thirdgames

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@KeepMembers
data class GamesMaintain(
    override val eventType: String = EventType.THIRD_GAME_STATU_CHANGED,
    val firmName: String?,
    val firmCode: String?,
    val firmType: String?,
    val gameType: String?,
    val maintain: Int, // 1.维护
): ServiceEventType {

    fun isMaintain() = 1 == maintain
}
