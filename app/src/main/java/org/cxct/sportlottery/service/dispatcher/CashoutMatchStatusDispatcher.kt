package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.service.EventDispatcher

object CashoutMatchStatusDispatcher: EventDispatcher<FrontWsEvent.CashoutMatchStatusEvent> {

    private val _cashoutMatchStatus = MutableSharedFlow<FrontWsEvent.CashoutMatchStatusEvent>()
    override fun eventType() = EventType.CASHOUT_MATCH_STATUS

    override suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any?): Boolean {
        event.cashoutMatchStatusEvent?.let { _cashoutMatchStatus.emit(it) }
        return true
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: (FrontWsEvent.CashoutMatchStatusEvent) -> Unit) {
        _cashoutMatchStatus.collectWith(lifecycleOwner.lifecycleScope, observer)
    }

}
