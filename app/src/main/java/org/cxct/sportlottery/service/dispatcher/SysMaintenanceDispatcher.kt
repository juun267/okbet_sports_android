package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner

import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.service.EventDispatcher
import org.cxct.sportlottery.util.SingleLiveEvent

object SysMaintenanceDispatcher: EventDispatcher<Boolean> {

    private val _sysMaintenance = SingleLiveEvent<Boolean>()
    override fun eventType() = EventType.SYS_MAINTENANCE

    override suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any?): Boolean {
        event.sysMaintainEvent?.let { _sysMaintenance.postValue(it.status == 1) }
        return true
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: (Boolean) -> Unit) {
        _sysMaintenance.observe(lifecycleOwner, observer)
    }

}