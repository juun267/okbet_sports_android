package org.cxct.sportlottery.ui.home.broadcast

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_odds_change.Odd
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.notice.NoticeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.ping_pong.PingPongEvent
import org.cxct.sportlottery.network.service.producer_up.ProducerUpEvent
import org.cxct.sportlottery.network.service.user_money.UserMoneyEvent
import org.cxct.sportlottery.network.service.user_notice.UserNoticeEvent
import org.cxct.sportlottery.util.MoshiUtil


object ServiceMessage {

    private val moshi: Moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() } //.add(KotlinJsonAdapterFactory())

    fun getGlobalStop(messageStr: String): GlobalStopEvent? {
        val adapter = moshi.adapter(GlobalStopEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getProducerUp(messageStr: String): ProducerUpEvent? {
        val adapter = moshi.adapter(ProducerUpEvent::class.java)
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

    fun getMatchOddsChange(messageStr: String): MatchOddsChangeEvent? {
        val adapter = moshi.adapter(MatchOddsChangeEvent::class.java)
        val data = adapter.fromJson(messageStr)
//        val jsonStructure = adapter.toJsonValue(obj)
//        val type = Types.newParameterizedType(MutableMap::class.java, String::class.java, Odd::class.java)
//        val adapter2 = moshi.adapter<Map<String, Odd>>(type)
//        MoshiUtil.fromJson<List<Odd>>(getRechargeConfig(mContext), Types.newParameterizedType(MutableList::class.java, MoneyPayWayData::class.java))
        return adapter.fromJson(messageStr)
    }

    fun getOddsChange(messageStr: String): OddsChangeEvent? {
        val adapter = moshi.adapter(OddsChangeEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getOrderSettlement(messageStr: String): OrderSettlementEvent? {
        val adapter = moshi.adapter(OrderSettlementEvent::class.java)
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