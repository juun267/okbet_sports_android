package org.cxct.sportlottery.ui.home.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.notice.NoticeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.ping_pong.PingPongEvent
import org.cxct.sportlottery.network.service.producer_up.ProducerUpEvent
import org.cxct.sportlottery.network.service.user_notice.UserNoticeEvent
import org.cxct.sportlottery.service.BackService
import org.json.JSONArray

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


    override fun onReceive(context: Context?, intent: Intent) {
        val bundle = intent.extras
        val channel = bundle?.getString("channel", "")
        val messageStr = bundle?.getString("serverMessage", "") ?: ""

        val jsonArray = JSONArray(messageStr)
        for (i in 0 until jsonArray.length()) {
            val jObjStr = jsonArray.optJSONObject(i).toString()
            val eventType = jsonArray.optJSONObject(i).optString("eventType")
            when (channel) {
                //全体公共频道
                BackService.URL_ALL -> {
                    when (eventType) {
                        EventType.NOTICE.value -> {
                            val data = ServiceMessage.getNotice(jObjStr)
                            _notice.value = data
                        }
                        EventType.GLOBAL_STOP.value -> {
                            val data = ServiceMessage.getGlobalStop(jObjStr)
                            _globalStop.value = data

                        }
                        EventType.PRODUCER_UP.value -> {
                            val data = ServiceMessage.getProducerUp(jObjStr)
                            _producerUp.value = data
                        }
                    }
                }

                //用户私人频道
                BackService.URL_PRIVATE -> {
                    when (eventType) {
                        EventType.USER_MONEY.value -> {
                            val data = ServiceMessage.getUserMoney(jObjStr)
                            _userMoney.value = data?.money
                        }
                        EventType.USER_NOTICE.value -> {
                            val data = ServiceMessage.getUserNotice(jObjStr)
                            _userNotice.value = data
                        }
                        EventType.ORDER_SETTLEMENT.value -> {
                            val data = ServiceMessage.getOrderSettlement(jObjStr)
                            _orderSettlement.value = data
                        }
                        EventType.PING_PONG.value -> {
                            val data = ServiceMessage.getPingPong(jObjStr)
                            _pingPong.value = data
                        }
                    }
                }
/*
                //大廳賠率
                BackService.URL_HALL -> {
                    when (eventType) {
                        EventType.MATCH_STATUS_CHANGE.value -> {
                            val data = ServiceMessage.getMatchStatusChange(jObjStr)
                            _matchStatusChange.value = data

                        }
                        EventType.MATCH_CLOCK.value -> {
                            val data = ServiceMessage.getMatchClock(jObjStr)
                            _matchClock.value = data

                        }
                        EventType.ODDS_CHANGE.value -> {
                            val data = ServiceMessage.getOddsChange(jObjStr)
                            _oddsChange.value = data

                        }
                    }
                }

                //具体赛事/赛季频道
                BackService.URL_EVENT -> {
                    when (eventType) {
                        EventType.MATCH_ODDS_CHANGE.value -> {
                            val data = ServiceMessage.getMatchOddsChange(jObjStr)
                            _matchOddsChange.value = data
                        }
                    }

                }
*/

                BackService.URL_PING -> {
                    when (eventType) {
                        EventType.PING_PONG.value -> {
                            val data = ServiceMessage.getPingPong(jObjStr)
                            _pingPong.value = data
                        }
                    }
                }
            }

            //大廳賠率
            if (channel?.contains(BackService.URL_HALL) == true) {
                when (eventType) {
                    EventType.MATCH_STATUS_CHANGE.value -> {
                        val data = ServiceMessage.getMatchStatusChange(jObjStr)
                        _matchStatusChange.value = data

                    }
                    EventType.MATCH_CLOCK.value -> {
                        val data = ServiceMessage.getMatchClock(jObjStr)
                        _matchClock.value = data

                    }
                    EventType.ODDS_CHANGE.value -> {
                        val data = ServiceMessage.getOddsChange(jObjStr)
                        _oddsChange.value = data

                    }
                }
            }

            //具体赛事/赛季频道
            if (channel?.contains(BackService.URL_EVENT) == true) {
                when (eventType) {
                    EventType.MATCH_ODDS_CHANGE.value -> {
                        val data = ServiceMessage.getMatchOddsChange(jObjStr)
                        _matchOddsChange.value = data
                    }
                }
            }
        }

    }

}
