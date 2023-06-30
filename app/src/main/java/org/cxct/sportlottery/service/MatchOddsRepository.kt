package org.cxct.sportlottery.service

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import org.cxct.sportlottery.common.extentions.doOnDestory
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent


object MatchOddsRepository {
    private val matchStatuObservers = mutableListOf<Observer<MatchStatusChangeEvent>>()

    fun onMatchStatus(statusEvent: MatchStatusChangeEvent) {
        matchStatuObservers.forEach { it.onChanged(statusEvent) }
    }

    fun observerMatchStatus(lifecycleOwner: LifecycleOwner, observer: Observer<MatchStatusChangeEvent>) {
        lifecycleOwner.doOnDestory { matchStatuObservers.remove(observer) }
        matchStatuObservers.add(observer)
    }

}

