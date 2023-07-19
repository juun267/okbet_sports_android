package org.cxct.sportlottery.service

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import org.cxct.sportlottery.common.extentions.doOnDestory
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent


object MatchOddsRepository {
    private val matchStatuObservers = mutableListOf<Observer<MatchStatusChangeEvent>>()
    private val matchOddsObservers = mutableListOf<Observer<MatchOddsChangeEvent>>()

    fun onMatchStatus(statusEvent: MatchStatusChangeEvent) {
        matchStatuObservers.forEach { it.onChanged(statusEvent) }
    }

    fun observerMatchStatus(lifecycleOwner: LifecycleOwner, observer: Observer<MatchStatusChangeEvent>) {
        lifecycleOwner.doOnDestory { matchStatuObservers.remove(observer) }
        matchStatuObservers.add(observer)
    }

    fun onMatchOdds(statusEvent: MatchOddsChangeEvent) {
        matchOddsObservers.forEach { it.onChanged(statusEvent) }
    }

    fun observerMatchOdds(lifecycleOwner: LifecycleOwner, observer: Observer<MatchOddsChangeEvent>) {
        lifecycleOwner.doOnDestory { matchOddsObservers.remove(observer) }
        matchOddsObservers.add(observer)
    }

}
