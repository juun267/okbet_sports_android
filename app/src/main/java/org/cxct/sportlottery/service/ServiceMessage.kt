package org.cxct.sportlottery.service

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
import org.cxct.sportlottery.network.service.user_level_config_change.UserLevelConfigListEvent
import org.cxct.sportlottery.network.service.user_money.LockMoneyEvent
import org.cxct.sportlottery.network.service.user_money.UserMoneyEvent
import org.cxct.sportlottery.network.service.user_notice.UserNoticeEvent


object ServiceMessage {

    private val moshi: Moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

    fun getGlobalStop(messageStr: String): GlobalStopEvent? {
        val adapter = moshi.adapter(GlobalStopEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getProducerUp(messageStr: String): ProducerUpEvent? {
        val adapter = moshi.adapter(ProducerUpEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getSysMaintenance(messageStr: String): SysMaintenanceEvent? {
        val adapter = moshi.adapter(SysMaintenanceEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getMatchClock(messageStr: String): MatchClockEvent? {
        val adapter = moshi.adapter(MatchClockEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getMatchStatusChange(messageStr: String): MatchStatusChangeEvent? {
        return try {
            val adapter = moshi.adapter(MatchStatusChangeEvent::class.java)
            return adapter.fromJson(messageStr)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getNotice(messageStr: String): NoticeEvent? {
        val adapter = moshi.adapter(NoticeEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getMatchOddsChange(messageStr: String): MatchOddsChangeEvent? {
        val adapter = moshi.adapter(MatchOddsChangeEvent::class.java)
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

    fun getLockMoney(messageStr: String): LockMoneyEvent? {
        val adapter = moshi.adapter(LockMoneyEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getUserNotice(messageStr: String): UserNoticeEvent? {
        val adapter = moshi.adapter(UserNoticeEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getPlayQuotaChange(messageStr: String): PlayQuotaChangeEvent? {
        val adapter = moshi.adapter(PlayQuotaChangeEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getLeagueChange(messageStr: String): LeagueChangeEvent? {
        val adapter = moshi.adapter(LeagueChangeEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getMatchOddsLock(messageStr: String): MatchOddsLockEvent? {
        val adapter = moshi.adapter(MatchOddsLockEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getUserDiscountChange(messageStr: String): UserDiscountChangeEvent? {
        val adapter = moshi.adapter(UserDiscountChangeEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getUserMaxBetMoney(messageStr: String): UserLevelConfigListEvent? {
        val adapter = moshi.adapter(UserLevelConfigListEvent::class.java)
        return adapter.fromJson(messageStr)
    }

    fun getUserInfoChange(messageStr: String): UserLevelConfigListEvent? {
        val adapter = moshi.adapter(UserLevelConfigListEvent::class.java)
        return adapter.fromJson(messageStr)
    }

}