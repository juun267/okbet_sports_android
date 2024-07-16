package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.service.EventDispatcher

object OrderSettlementDispatcher: EventDispatcher<FrontWsEvent.BetSettlementEvent> {

    private val _orderSettlement = MutableLiveData<FrontWsEvent.BetSettlementEvent>()
    override fun eventType() = EventType.ORDER_SETTLEMENT

    override suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any?): Boolean {
        event.betSettlementEvent?.let { _orderSettlement.postValue(it) }
        return true
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: (FrontWsEvent.BetSettlementEvent) -> Unit) {
        _orderSettlement.observe(lifecycleOwner, observer)
    }
}