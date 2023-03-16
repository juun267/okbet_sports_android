package org.cxct.sportlottery.network.money

import com.squareup.moshi.Json


data class RedEnvelopeInfo(
    @Json(name = "redenpId")
    val redenpId: Int,//主键 红包id
    @Json(name = "redenpStartTime")
    val redenpStartTime: Long,//红包雨开始时间
    @Json(name = "redenpEndTime")
    val redenpEndTime: Long,//红包雨结束时间
    @Json(name = "serverTime")
    val serverTime: Long,//服务器系统时间（毫秒）



)