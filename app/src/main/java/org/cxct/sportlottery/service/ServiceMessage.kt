package org.cxct.sportlottery.service

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.sys_maintenance.SportMaintenanceEvent
import org.cxct.sportlottery.util.fastjson.FastJsonUtils


object ServiceMessage {

    private val moshi: Moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

    fun getSportMaintenance(messageStr: String): SportMaintenanceEvent? {
//        val adapter = moshi.adapter(SysMaintenanceEvent::class.java)
        return FastJsonUtils.jsonToObject(messageStr,SportMaintenanceEvent::class.java)
    }

    fun getOrderSettlement(messageStr: String): OrderSettlementEvent? {
//        val adapter = moshi.adapter(OrderSettlementEvent::class.java)
//        return adapter.fromJson(messageStr)
        return FastJsonUtils.jsonToObject(messageStr,OrderSettlementEvent::class.java)
    }

    fun <T> parseResult(messageStr: String, clazz: Class<T>): T? {
        return FastJsonUtils.jsonToObject(messageStr, clazz)
    }

}