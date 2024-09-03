package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.service.EventDispatcher

object OrderSettlementDispatcher: EventDispatcher<FrontWsEvent.BetSettlementEvent> {

    private val _orderSettlement = MutableSharedFlow<FrontWsEvent.BetSettlementEvent>()
    override fun eventType() = EventType.ORDER_SETTLEMENT

    override suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any?): Boolean {
        event.betSettlementEvent?.let { _orderSettlement.emit(it) }
        return true
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: (FrontWsEvent.BetSettlementEvent) -> Unit) {
        _orderSettlement.collectWith(lifecycleOwner.lifecycleScope, observer)
    }
}