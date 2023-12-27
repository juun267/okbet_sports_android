package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent

@Suppress("ObjectPropertyName")
object SocketRepository {

    private val _oddsChange = MutableSharedFlow<Triple<OddsChangeEvent?, MatchOdd?, Boolean>>(replay = 0)
    val oddsChange = _oddsChange.asSharedFlow()

    private val _matchClock = MutableSharedFlow<FrontWsEvent.MatchClockEvent>(replay = 0)
    val matchClock = _matchClock.asSharedFlow()

    private val _matchOddsLock = MutableSharedFlow<FrontWsEvent.MatchOddsLockEvent>(replay = 0)
    val matchOddsLock = _matchOddsLock.asSharedFlow()

    private val _globalStop = MutableSharedFlow<FrontWsEvent.GlobalStopEvent>(replay = 0)
    val globalStop = _globalStop.asSharedFlow()

    private val _producerUp = MutableSharedFlow<FrontWsEvent.ProducerUpEvent>(replay = 0)
    val producerUp = _producerUp.asSharedFlow()

    private val _closePlayCate = MutableSharedFlow<FrontWsEvent.ClosePlayCateEvent>(replay = 0)
    val closePlayCate = _closePlayCate.asSharedFlow()

    private val _matchStatusChange = MutableSharedFlow<FrontWsEvent.MatchStatusChangeEvent>(replay = 0)
    val matchStatusChange = _matchStatusChange.asSharedFlow()

    private val _leagueChange = MutableSharedFlow<FrontWsEvent.LeagueChangeEvent>(replay = 0)
    val leagueChange = _leagueChange.asSharedFlow()

    private val _matchStatusSwitch = MutableSharedFlow<FrontWsEvent.MatchStatusSwitchEvent>(replay = 0)
    val matchStatusSwitch = _matchStatusSwitch.asSharedFlow()

    private val _switchParlayMatch = MutableSharedFlow<FrontWsEvent.SwitchParlayMatchEvent>(replay = 0)
    val switchParlayMatch = _switchParlayMatch.asSharedFlow()

    private val _matchOddsChange = MutableSharedFlow<MatchOddsChangeEvent>(replay = 0)
    val matchOddsChange = _matchOddsChange.asSharedFlow()

    private val _sportMaintenance = MutableLiveData<Boolean>()
    val sportMaintenance: LiveData<Boolean>
        get() = _sportMaintenance

    suspend fun emitOddsChange(oddsChangeEvent: OddsChangeEvent?, matchOdd: MatchOdd?, update: Boolean){
        _oddsChange.emit(Triple(oddsChangeEvent, matchOdd, update))
    }

    suspend fun emitMatchClock(event: FrontWsEvent.MatchClockEvent){
        _matchClock.emit(event)
    }

    suspend fun emitMatchOddsLock(event: FrontWsEvent.MatchOddsLockEvent){
        _matchOddsLock.emit(event)
    }

    suspend fun emitGlobalStop(event: FrontWsEvent.GlobalStopEvent){
        _globalStop.emit(event)
    }

    suspend fun emitProducerUp(event: FrontWsEvent.ProducerUpEvent){
        _producerUp.emit(event)
    }

    suspend fun emitClosePlayCate(event: FrontWsEvent.ClosePlayCateEvent){
        _closePlayCate.emit(event)
    }

    suspend fun emitMatchStatusChange(event: FrontWsEvent.MatchStatusChangeEvent){
        _matchStatusChange.emit(event)
    }

    suspend fun emitLeagueChange(event: FrontWsEvent.LeagueChangeEvent){
        _leagueChange.emit(event)
    }

    suspend fun emitMatchStatusSwitch(event: FrontWsEvent.MatchStatusSwitchEvent){
        _matchStatusSwitch.emit(event)
    }

    suspend fun emitSwitchParlayMatch(event: FrontWsEvent.SwitchParlayMatchEvent){
        _switchParlayMatch.emit(event)
    }

    suspend fun emitMatchOddsChange(event: MatchOddsChangeEvent){
        _matchOddsChange.emit(event)
    }

    fun setSportMaintenance(isSportMaintenance: Boolean) {
        _sportMaintenance.postValue(isSportMaintenance)
    }
}