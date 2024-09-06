package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.service.EventDispatcher

object BetCashOutDispatcher: EventDispatcher<FrontWsEvent.BetCashOutEvent> {
    private val _cashout = MutableSharedFlow<FrontWsEvent.BetCashOutEvent>()
    override fun eventType() = EventType.CASH_OUT

    override suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any?): Boolean {
        event.betCashOutEvent?.let { _cashout.emit(it) }
        return true
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: (FrontWsEvent.BetCashOutEvent) -> Unit) {
        _cashout.collectWith(lifecycleOwner.lifecycleScope, observer)
    }

}
