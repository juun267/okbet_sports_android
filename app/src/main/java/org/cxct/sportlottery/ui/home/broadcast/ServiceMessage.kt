package org.cxct.sportlottery.ui.home.broadcast

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.notice.NoticeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.ping_pong.PingPongEvent
import org.cxct.sportlottery.network.service.user_money.UserMoneyEvent
import org.cxct.sportlottery.network.service.user_notice.UserNoticeEvent

object ServiceMessage {

    private val moshi: Moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() } //.add(KotlinJsonAdapterFactory())

    fun getGlobalStop(messageStr: String): GlobalStopEvent? {
        val adapter = moshi.adapter(GlobalStopEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getMatchClock(messageStr: String): MatchClockEvent? {
        val adapter = moshi.adapter(MatchClockEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getMatchStatusChange(messageStr: String): MatchStatusChangeEvent? {
        val adapter = moshi.adapter(MatchStatusChangeEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getNotice(messageStr: String): NoticeEvent? {
        val adapter = moshi.adapter(NoticeEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getOddsChange(messageStr: String): OddsChangeEvent? {
        val adapter = moshi.adapter(OddsChangeEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getPingPong(messageStr: String): PingPongEvent? {
        val adapter = moshi.adapter(PingPongEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getUserMoney(messageStr: String): UserMoneyEvent? {
        val adapter = moshi.adapter(UserMoneyEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getUserNotice(messageStr: String): UserNoticeEvent? {
        val adapter = moshi.adapter(UserNoticeEvent::class.java)
        return adapter.fromJson(messageStr)
    }

}