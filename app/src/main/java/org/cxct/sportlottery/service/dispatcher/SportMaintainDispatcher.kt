package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.service.EventDispatcher

object SportMaintainDispatcher: EventDispatcher<FrontWsEvent.SportMaintainEvent> {

    private val sportMaintenance = MutableLiveData<FrontWsEvent.SportMaintainEvent>()
    override fun eventType() = EventType.SPORT_MAINTAIN_STATUS

    override suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any?): Boolean {
        sportMaintenance.postValue(event.sportMaintainEvent)
        return true
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: (FrontWsEvent.SportMaintainEvent) -> Unit) {
        sportMaintenance.observe(lifecycleOwner, observer)
    }
}