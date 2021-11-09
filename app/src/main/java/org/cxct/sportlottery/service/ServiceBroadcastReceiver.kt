package org.cxct.sportlottery.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.UserDiscountChangeEvent
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.league_change.LeagueChangeEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_odds_lock.MatchOddsLockEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.notice.NoticeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.ping_pong.PingPongEvent
import org.cxct.sportlottery.network.service.play_quota_change.PlayQuotaChangeEvent
import org.cxct.sportlottery.network.service.producer_up.ProducerUpEvent
import org.cxct.sportlottery.network.service.sys_maintenance.SysMaintenanceEvent
import org.cxct.sportlottery.network.service.user_notice.UserNoticeEvent
import org.cxct.sportlottery.service.BackService.Companion.CHANNEL_KEY
import org.cxct.sportlottery.service.BackService.Companion.CONNECT_STATUS
import org.cxct.sportlottery.service.BackService.Companion.SERVER_MESSAGE_KEY
import org.cxct.sportlottery.service.BackService.Companion.mUserId
import org.cxct.sportlottery.util.addOddDiscount
import org.json.JSONArray
import timber.log.Timber

open class ServiceBroadcastReceiver : BroadcastReceiver() {

    val globalStop: LiveData<GlobalStopEvent?>
        get() = _globalStop

    val matchClock: LiveData<MatchClockEvent?>
        get() = _matchClock

    val matchOddsChange: LiveData<MatchOddsChangeEvent?>
        get() = _matchOddsChange

    val matchStatusChange: LiveData<MatchStatusChangeEvent?>
        get() = _matchStatusChange

    val notice: LiveData<NoticeEvent?>
        get() = _notice

    val oddsChange: LiveData<OddsChangeEvent?>
        get() = _oddsChange

    val orderSettlement: LiveData<OrderSettlementEvent?>
        get() = _orderSettlement

    val pingPong: LiveData<PingPongEvent?>
        get() = _pingPong

    val producerUp: LiveData<ProducerUpEvent?>
        get() = _producerUp

    val userMoney: LiveData<Double?>
        get() = _userMoney

    val userNotice: LiveData<UserNoticeEvent?>
        get() = _userNotice

    val sysMaintenance: LiveData<SysMaintenanceEvent?>
        get() = _sysMaintenance

    val serviceConnectStatus: LiveData<ServiceConnectStatus>
        get() = _serviceConnectStatus

    val playQuotaChange: LiveData<PlayQuotaChangeEvent?>
        get() = _playQuotaChange

    val leagueChange: LiveData<LeagueChangeEvent?>
        get() = _leagueChange

    val matchOddsLock: LiveData<MatchOddsLockEvent?>
        get() = _matchOddsLock

    val userDiscountChange: LiveData<UserDiscountChangeEvent?>
        get() = _userDiscountChange

    private val _globalStop = MutableLiveData<GlobalStopEvent?>()
    private val _matchClock = MutableLiveData<MatchClockEvent?>()
    private val _matchOddsChange = MutableLiveData<MatchOddsChangeEvent?>()
    private val _matchStatusChange = MutableLiveData<MatchStatusChangeEvent?>()
    private val _notice = MutableLiveData<NoticeEvent?>()
    private val _oddsChange = MutableLiveData<OddsChangeEvent?>()
    private val _orderSettlement = MutableLiveData<OrderSettlementEvent?>()
    private val _pingPong = MutableLiveData<PingPongEvent?>()
    private val _producerUp = MutableLiveData<ProducerUpEvent?>()
    private val _userMoney = MutableLiveData<Double?>()
    private val _userNotice = MutableLiveData<UserNoticeEvent?>()
    private val _sysMaintenance = MutableLiveData<SysMaintenanceEvent?>()
    private val _serviceConnectStatus = MutableLiveData<ServiceConnectStatus>()
    private val _playQuotaChange = MutableLiveData<PlayQuotaChangeEvent?>()
    private val _leagueChange = MutableLiveData<LeagueChangeEvent?>()
    private val _matchOddsLock = MutableLiveData<MatchOddsLockEvent?>()
    private val _userDiscountChange = MutableLiveData<UserDiscountChangeEvent?>()


    override fun onReceive(context: Context?, intent: Intent) {
        val bundle = intent.extras
        receiveConnectStatus(bundle)
        receiveMessage(bundle)
    }

    private fun receiveConnectStatus(bundle: Bundle?) {
        val connectStatus = bundle?.get(CONNECT_STATUS) as ServiceConnectStatus?
        connectStatus?.let { status ->
            _serviceConnectStatus.value = status
        }
    }

    private fun receiveMessage(bundle: Bundle?) {
        val channelStr = bundle?.getString(CHANNEL_KEY, "") ?: ""
        val messageStr = bundle?.getString(SERVER_MESSAGE_KEY, "") ?: ""

        val jsonArray = if (messageStr.isNotEmpty()) {
            JSONArray(messageStr)
        } else {
            null
        }
        jsonArray?.let {
            for (i in 0 until jsonArray.length()) {
                val jObjStr = jsonArray.optJSONObject(i).toString()
                val eventType = EventType.getEventType(jsonArray.optJSONObject(i).optString("eventType"))
                //全体公共频道
                when (eventType) {
                    EventType.NOTICE -> {
                        val data = ServiceMessage.getNotice(jObjStr)
                        _notice.value = data
                    }
                    EventType.GLOBAL_STOP -> {
                        val data = ServiceMessage.getGlobalStop(jObjStr)
                        _globalStop.value = data

                    }
                    EventType.PRODUCER_UP -> {
                        val data = ServiceMessage.getProducerUp(jObjStr)
                        _producerUp.value = data
                    }

                    //公共频道(这个通道会通知主站平台维护)
                    EventType.SYS_MAINTENANCE -> {
                        val data = ServiceMessage.getSysMaintenance(jObjStr)
                        _sysMaintenance.value = data
                    }
                    EventType.PLAY_QUOTA_CHANGE -> {
                        val data = ServiceMessage.getPlayQuotaChange(jObjStr)
                        _playQuotaChange.value = data
                    }

                    //用户私人频道
                    EventType.USER_MONEY -> {
                        val data = ServiceMessage.getUserMoney(jObjStr)
                        _userMoney.value = data?.money
                    }
                    EventType.USER_NOTICE -> {
                        val data = ServiceMessage.getUserNotice(jObjStr)
                        _userNotice.value = data
                    }
                    EventType.ORDER_SETTLEMENT -> {
                        val data = ServiceMessage.getOrderSettlement(jObjStr)
                        _orderSettlement.value = data
                    }
                    EventType.PING_PONG -> {
                        val data = ServiceMessage.getPingPong(jObjStr)
                        _pingPong.value = data
                    }

                    //大廳賠率
                    EventType.MATCH_STATUS_CHANGE -> {
                        val data = ServiceMessage.getMatchStatusChange(jObjStr)
                        _matchStatusChange.value = data
                    }
                    EventType.MATCH_CLOCK -> {
                        val data = ServiceMessage.getMatchClock(jObjStr)
                        _matchClock.value = data
                    }
                    EventType.ODDS_CHANGE -> {
                        val data = ServiceMessage.getOddsChange(jObjStr)?.apply {
                            channel = channelStr
                        }
                        data?.addOddDiscount(MultiLanguagesApplication.appContext, mUserId)
                        _oddsChange.value = data
                    }
                    EventType.LEAGUE_CHANGE -> {
                        val data = ServiceMessage.getLeagueChange(jObjStr)
                        _leagueChange.value = data
                    }
                    EventType.MATCH_ODDS_LOCK -> {
                        val data = ServiceMessage.getMatchOddsLock(jObjStr)
                        _matchOddsLock.value = data
                    }


                    //具体赛事/赛季频道
                    EventType.MATCH_ODDS_CHANGE -> {
                        val data = ServiceMessage.getMatchOddsChange(jObjStr)
                        _matchOddsChange.value = data
                    }

                    //賠率折扣
                    EventType.USER_DISCOUNT_CHANGE -> {
                        val data = ServiceMessage.getUserDiscountChange(jObjStr)
                        _userDiscountChange.value = data
                    }

                    EventType.UNKNOWN -> {
                        Timber.i("Receive UnKnown EventType : ${eventType.value}")
                    }
                }
            }
        }
    }
}
