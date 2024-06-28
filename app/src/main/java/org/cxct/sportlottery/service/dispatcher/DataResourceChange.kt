package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.network.service.EventType.DATA_SOURCE_CHANGE
import org.cxct.sportlottery.service.EventDispatcher

object DataResourceChange: EventDispatcher<Boolean> {

    private val sourceChanged = MutableSharedFlow<Boolean>()
    override fun eventType() = DATA_SOURCE_CHANGE
    override fun observe(lifecycleOwner: LifecycleOwner, observer: (Boolean) -> Unit) {
        sourceChanged.collectWith(lifecycleOwner.lifecycleScope, observer)
    }

    override suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any?): Boolean {
        sourceChanged.emit(true)
        return true
    }

//    fun observerDataSourceChang(scope: CoroutineScope, collector: FlowCollector<Boolean>) {
//        sourceChanged.collectWith(scope, collector)
//    }
}