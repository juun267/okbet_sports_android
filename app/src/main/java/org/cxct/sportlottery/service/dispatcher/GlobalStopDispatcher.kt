package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.service.EventDispatcher

object GlobalStopDispatcher: EventDispatcher<FrontWsEvent.GlobalStopEvent> {

    private val _globalStop = MutableSharedFlow<FrontWsEvent.GlobalStopEvent>()
    override fun eventType() = EventType.GLOBAL_STOP

    override fun observe(lifecycleOwner: LifecycleOwner, observer: (FrontWsEvent.GlobalStopEvent) -> Unit) {
        _globalStop.collectWith(lifecycleOwner.lifecycleScope, observer)
    }

    override suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any?): Boolean {
        event.globalStopEvent?.let { _globalStop.emit(it) }
        return true
    }

}