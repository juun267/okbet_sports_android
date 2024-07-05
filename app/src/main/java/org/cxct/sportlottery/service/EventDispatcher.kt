package org.cxct.sportlottery.service

import androidx.lifecycle.LifecycleOwner
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent

interface EventDispatcher<T> {
    fun eventType(): String
    fun observe(lifecycleOwner: LifecycleOwner, observer: (T)-> Unit)
    suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any? = null): Boolean
}
