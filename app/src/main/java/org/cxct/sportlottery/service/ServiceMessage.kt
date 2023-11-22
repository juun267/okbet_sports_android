package org.cxct.sportlottery.service

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.network.service.sys_maintenance.SportMaintenanceEvent
import org.cxct.sportlottery.network.service.user_money.LockMoneyEvent
import org.cxct.sportlottery.network.service.user_money.UserMoneyEvent
import org.cxct.sportlottery.util.fastjson.FastJsonUtils


object ServiceMessage {

    private val moshi: Moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

    fun getSportMaintenance(messageStr: String): SportMaintenanceEvent? {
//        val adapter = moshi.adapter(SysMaintenanceEvent::class.java)
        return FastJsonUtils.jsonToObject(messageStr,SportMaintenanceEvent::class.java)
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

    fun getRecondNew(messageStr: String): RecordNewEvent? {
        val recode = FastJsonUtils.jsonToObject(messageStr,RecordNewEvent::class.java)
        recode.isWS = true
        return recode
    }

    fun getRecondResult(messageStr: String): RecordNewEvent? {
        val recode = FastJsonUtils.jsonToObject(messageStr,RecordNewEvent::class.java)
        recode.isWS = true
        return recode
    }

    fun <T> parseResult(messageStr: String, clazz: Class<T>): T? {
        return FastJsonUtils.jsonToObject(messageStr, clazz)
    }

}