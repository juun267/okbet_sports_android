package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.service.EventDispatcher

object ProducerUpDispatcher: EventDispatcher<FrontWsEvent.ProducerUpEvent> {

    private val _producerUp = MutableSharedFlow<FrontWsEvent.ProducerUpEvent>()
    override fun eventType() = EventType.PRODUCER_UP

    override suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any?): Boolean {
        event.producerUpEvent?.let { _producerUp.emit(it) }
        return true
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: (FrontWsEvent.ProducerUpEvent) -> Unit) {
        _producerUp.collectWith(lifecycleOwner.lifecycleScope, observer)
    }

}
