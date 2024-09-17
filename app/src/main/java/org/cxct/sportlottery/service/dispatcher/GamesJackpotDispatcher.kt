package org.cxct.sportlottery.service.dispatcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.service.EventDispatcher

object GamesJackpotDispatcher: EventDispatcher<FrontWsEvent.JackPotGameEvent> {

    private val jackpotEvent = MutableSharedFlow<FrontWsEvent.JackPotGameEvent>()
    override fun eventType() = EventType.RECORD_RESULT_JACKPOT_GAMES

    override suspend fun handleEvent(eventType: String, event: FrontWsEvent.Event, obj: Any?): Boolean {
        event.jackPotGameEvent?.let { jackpotEvent.emit(it) }
        return true
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: (FrontWsEvent.JackPotGameEvent) -> Unit) {
        jackpotEvent.collectWith(lifecycleOwner.lifecycleScope, observer)
    }
}