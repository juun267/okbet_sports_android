package org.cxct.sportlottery.ui.home.broadcast

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.notice.NoticeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.ping_pong.PingPongEvent
import org.cxct.sportlottery.network.service.producer_up.ProducerUpEvent
import org.cxct.sportlottery.network.service.user_notice.UserNoticeEvent

/**
 * https://stackoverflow.com/questions/51490558/accessing-broadcastreceiver-in-viewmodel
 */
class BroadcastRepository {

    companion object {
        private val INSTANCE: BroadcastRepository = BroadcastRepository()
    }

    val globalStop = MediatorLiveData<GlobalStopEvent?>()
    val matchClock = MediatorLiveData<MatchClockEvent?>()
    val matchStatusChange = MediatorLiveData<MatchStatusChangeEvent?>()
    val notice = MediatorLiveData<NoticeEvent?>()
    val oddsChange = MediatorLiveData<OddsChangeEvent?>()
    val orderSettlement = MediatorLiveData<OrderSettlementEvent?>()
    val pingPong = MediatorLiveData<PingPongEvent?>()
    val producerUp = MediatorLiveData<ProducerUpEvent?>()
    val userMoney = MediatorLiveData<Double?>()
    val userNotice = MediatorLiveData<UserNoticeEvent?>()

    fun instance(): BroadcastRepository {
        return INSTANCE
    }

    fun addDataSources(_globalStop: LiveData<GlobalStopEvent?>,
                       _matchClock: LiveData<MatchClockEvent?>,
                       _matchStatusChange: LiveData<MatchStatusChangeEvent?>,
                       _notice: LiveData<NoticeEvent?>,
                       _oddsChange: LiveData<OddsChangeEvent?>,
                       _orderSettlement: LiveData<OrderSettlementEvent?>,
                       _pingPong: LiveData<PingPongEvent?>,
                       _producerUp: LiveData<ProducerUpEvent?>,
                       _userMoney: LiveData<Double?>,
                       _userNotice: LiveData<UserNoticeEvent?>) {
        globalStop.addSource(_globalStop) {
            globalStop.postValue(it)
        }
        matchClock.addSource(_matchClock)  {
            matchClock.postValue(it)
        }
        matchStatusChange.addSource(_matchStatusChange)  {
            matchStatusChange.postValue(it)
        }
        notice.addSource(_notice)  {
            notice.postValue(it)
        }
        oddsChange.addSource(_oddsChange)  {
            oddsChange.postValue(it)
        }
        orderSettlement.addSource(_orderSettlement)  {
            orderSettlement.postValue(it)
        }
        pingPong.addSource(_pingPong)  {
            pingPong.postValue(it)
        }
        producerUp.addSource(_producerUp)  {
            producerUp.postValue(it)
        }
        userMoney.addSource(_userMoney)  {
            userMoney.postValue(it)
        }
        userNotice.addSource(_userNotice) {
            userNotice.postValue(it)
        }
    }

    fun removeDataSource(_globalStop: LiveData<GlobalStopEvent?>, _matchClock: LiveData<MatchClockEvent?>, _matchStatusChange: LiveData<MatchStatusChangeEvent?>, _notice: LiveData<NoticeEvent?>, _oddsChange: LiveData<OddsChangeEvent?>, _orderSettlement: LiveData<OrderSettlementEvent?>, _pingPong: LiveData<PingPongEvent?>, _producerUp: LiveData<ProducerUpEvent?>, _userMoney: LiveData<Double?>, _userNotice: LiveData<UserNoticeEvent?>) {
        globalStop.removeSource(_globalStop)
        matchClock.removeSource(_matchClock)
        matchStatusChange.removeSource(_matchStatusChange)
        notice.removeSource(_notice)
        oddsChange.removeSource(_oddsChange)
        orderSettlement.removeSource(_orderSettlement)
        pingPong.removeSource(_pingPong)
        producerUp.removeSource(_producerUp)
        userMoney.removeSource(_userMoney)
        userNotice.removeSource(_userNotice)
    }

}