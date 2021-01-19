package org.cxct.sportlottery.ui.home.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.notice.NoticeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.ping_pong.PingPongEvent
import org.cxct.sportlottery.network.service.user_money.UserMoneyEvent
import org.cxct.sportlottery.network.service.user_notice.UserNoticeEvent
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.ui.home.MainViewModel
import org.json.JSONArray

class ServiceBroadcastReceiver(val viewModel: MainViewModel) : BroadcastReceiver() {
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

                        }
                        EventType.GLOBAL_STOP.value -> {
                            val data = ServiceMessage.getGlobalStop(jObjStr)

                        }
                    }
                }

                //用户私人频道
                BackService.URL_PRIVATE -> {
                    when (eventType) {
                        EventType.USER_MONEY.value -> {
                            val data = ServiceMessage.getUserMoney(jObjStr)
                            viewModel.setUserMoney(data?.money) //testing
                        }
                        EventType.USER_NOTICE.value -> {
                            val data = ServiceMessage.getUserNotice(jObjStr)

                        }
                        EventType.ORDER_SETTLEMENT.value -> {

                        }
                        EventType.PING_PONG.value -> {

                        }
                    }
                }

                //大廳賠率
                BackService.URL_HALL -> {
                    when (eventType) {
                        EventType.MATCH_STATUS_CHANGE.value -> {

                        }
                        EventType.MATCH_CLOCK.value -> {

                        }
                        EventType.ODDS_CHANGE.value -> {

                        }
                    }
                }

                //具体赛事/赛季频道
                BackService.URL_EVENT -> {
                    when (eventType) {
                        EventType.ODDS_CHANGE.value -> {

                        }
                    }

                }

                BackService.URL_PING -> {
                    when (eventType) {
                        EventType.PING_PONG.value -> {

                        }
                    }
                }

            }
        }

    }

}
