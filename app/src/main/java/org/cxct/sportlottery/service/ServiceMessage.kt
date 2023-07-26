package org.cxct.sportlottery.service

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.cxct.sportlottery.network.service.UserDiscountChangeEvent
import org.cxct.sportlottery.network.service.close_play_cate.ClosePlayCateEvent
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
import org.cxct.sportlottery.network.service.producer_up.ProducerUpEvent
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.network.service.sys_maintenance.SportMaintenanceEvent
import org.cxct.sportlottery.network.service.sys_maintenance.SysMaintenanceEvent
import org.cxct.sportlottery.network.service.user_level_config_change.UserLevelConfigListEvent
import org.cxct.sportlottery.network.service.user_money.LockMoneyEvent
import org.cxct.sportlottery.network.service.user_money.UserMoneyEvent
import org.cxct.sportlottery.network.service.user_notice.UserNoticeEvent
import org.cxct.sportlottery.util.fastjson.FastJsonUtils


object ServiceMessage {

    private val moshi: Moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }


    fun getGlobalStop(messageStr: String): GlobalStopEvent? {
//        val adapter = moshi.adapter(GlobalStopEvent::class.java)

        return FastJsonUtils.jsonToObject(messageStr,GlobalStopEvent::class.java)
    }

    fun getProducerUp(messageStr: String): ProducerUpEvent? {
//        val adapter = moshi.adapter(ProducerUpEvent::class.java)
        return FastJsonUtils.jsonToObject(messageStr,ProducerUpEvent::class.java)
    }

    fun getSysMaintenance(messageStr: String): SysMaintenanceEvent? {
//        val adapter = moshi.adapter(SysMaintenanceEvent::class.java)
        return FastJsonUtils.jsonToObject(messageStr,SysMaintenanceEvent::class.java)
    }

    fun getSportMaintenance(messageStr: String): SportMaintenanceEvent? {
//        val adapter = moshi.adapter(SysMaintenanceEvent::class.java)
        return FastJsonUtils.jsonToObject(messageStr,SportMaintenanceEvent::class.java)
    }

    fun getMatchClock(messageStr: String): MatchClockEvent? {
//        val adapter = moshi.adapter(MatchClockEvent::class.java)
        return FastJsonUtils.jsonToObject(messageStr,MatchClockEvent::class.java)
    }

    fun getMatchStatusChange(messageStr: String): MatchStatusChangeEvent? {
        return try {
//            val adapter = moshi.adapter(MatchStatusChangeEvent::class.java)
            return FastJsonUtils.jsonToObject(messageStr,MatchStatusChangeEvent::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getNotice(messageStr: String): NoticeEvent? {
//        val adapter = moshi.adapter(NoticeEvent::class.java)
//        return adapter.fromJson(messageStr)
        return  FastJsonUtils.jsonToObject(messageStr,NoticeEvent::class.java)
    }

    fun getMatchOddsChange(messageStr: String): MatchOddsChangeEvent? {
//        val adapter = moshi.adapter(MatchOddsChangeEvent::class.java)
//        return adapter.fromJson(messageStr)
        return  FastJsonUtils.jsonToObject(messageStr,MatchOddsChangeEvent::class.java)
    }

    fun getOddsChange(messageStr: String): OddsChangeEvent? {
//        val adapter = moshi.adapter(OddsChangeEvent::class.java)
//        return adapter.fromJson(messageStr)

        return  FastJsonUtils.jsonToObject(messageStr,OddsChangeEvent::class.java)

    }

    fun getOrderSettlement(messageStr: String): OrderSettlementEvent? {
//        val adapter = moshi.adapter(OrderSettlementEvent::class.java)
//        return adapter.fromJson(messageStr)
        return FastJsonUtils.jsonToObject(messageStr,OrderSettlementEvent::class.java)
    }

    fun getPingPong(messageStr: String): PingPongEvent? {
//        val adapter = moshi.adapter(PingPongEvent::class.java)
//        return adapter.fromJson(messageStr)
        return FastJsonUtils.jsonToObject(messageStr,PingPongEvent::class.java)
    }

    fun getUserMoney(messageStr: String): UserMoneyEvent? {
//        val adapter = moshi.adapter(UserMoneyEvent::class.java)
//        return adapter.fromJson(messageStr)
        return FastJsonUtils.jsonToObject(messageStr,UserMoneyEvent::class.java)
    }

    fun getLockMoney(messageStr: String): LockMoneyEvent? {
//        val adapter = moshi.adapter(LockMoneyEvent::class.java)
//        return adapter.fromJson(messageStr)
        return FastJsonUtils.jsonToObject(messageStr,LockMoneyEvent::class.java)
    }

    fun getUserNotice(messageStr: String): UserNoticeEvent? {
//        val adapter = moshi.adapter(UserNoticeEvent::class.java)
//        return adapter.fromJson(messageStr)
        return FastJsonUtils.jsonToObject(messageStr,UserNoticeEvent::class.java)
    }

    fun getLeagueChange(messageStr: String): LeagueChangeEvent? {
//        val adapter = moshi.adapter(LeagueChangeEvent::class.java)
//        return adapter.fromJson(messageStr)
        return FastJsonUtils.jsonToObject(messageStr,LeagueChangeEvent::class.java)
    }

    fun getMatchOddsLock(messageStr: String): MatchOddsLockEvent? {
//        val adapter = moshi.adapter(MatchOddsLockEvent::class.java)
//        return adapter.fromJson(messageStr)
        return FastJsonUtils.jsonToObject(messageStr,MatchOddsLockEvent::class.java)
    }

    fun getUserDiscountChange(messageStr: String): UserDiscountChangeEvent? {
//        val adapter = moshi.adapter(UserDiscountChangeEvent::class.java)
//        return adapter.fromJson(messageStr)
        return FastJsonUtils.jsonToObject(messageStr,UserDiscountChangeEvent::class.java)
    }

    fun getUserMaxBetMoney(messageStr: String): UserLevelConfigListEvent? {
//        val adapter = moshi.adapter(UserLevelConfigListEvent::class.java)
//        return adapter.fromJson(messageStr)
        return FastJsonUtils.jsonToObject(messageStr,UserLevelConfigListEvent::class.java)
    }

    fun getUserInfoChange(messageStr: String): UserLevelConfigListEvent? {
//        val adapter = moshi.adapter(UserLevelConfigListEvent::class.java)
//        return adapter.fromJson(messageStr)
        return FastJsonUtils.jsonToObject(messageStr,UserLevelConfigListEvent::class.java)
    }

    fun getClosePlayCate(messageStr: String): ClosePlayCateEvent? {
        return FastJsonUtils.jsonToObject(messageStr,ClosePlayCateEvent::class.java)
    }

    fun getRecondNew(messageStr: String): RecordNewEvent? {
        return FastJsonUtils.jsonToObject(messageStr,RecordNewEvent::class.java)
    }

    fun getRecondResult(messageStr: String): RecordNewEvent? {
        return FastJsonUtils.jsonToObject(messageStr,RecordNewEvent::class.java)
    }

}