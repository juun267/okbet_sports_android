package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.service.EventDispatcher

object NoticeDispatcher: EventDispatcher<FrontWsEvent.NoticeEvent> {

    private val _notice = MutableLiveData<FrontWsEvent.NoticeEvent>()

    override fun eventType() = EventType.NOTICE
    override fun observe(lifecycleOwner: LifecycleOwner, observer: (FrontWsEvent.NoticeEvent) -> Unit) {
        _notice.observe(lifecycleOwner, observer)
    }

    override suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any?): Boolean {
        event.noticeEvent?.let { _notice.postValue(it) }
        return true
    }

}