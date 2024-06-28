package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.service.EventDispatcher
import org.cxct.sportlottery.util.Event

object ClosePlayCateDispatcher: EventDispatcher<Event<FrontWsEvent.ClosePlayCateEvent>> {

    private val _closePlayCate = MutableLiveData<Event<FrontWsEvent.ClosePlayCateEvent>>()
    override fun eventType() = EventType.CLOSE_PLAY_CATE

    override suspend fun handleEvent(
        eventType: String,
        event: FrontWsEvent.Event,
        obj: Any?
    ): Boolean {
        event.closePlayCateEvent?.let { _closePlayCate.postValue(Event(it)) }
        return true
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: (Event<FrontWsEvent.ClosePlayCateEvent>) -> Unit) {
        _closePlayCate.observe(lifecycleOwner, observer)
    }
}