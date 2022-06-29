package org.cxct.sportlottery.network.money

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.message.Row



data class RedEnvelopeInfo(
    @Json(name = "redenpId")
    val redenpId: Int,//主键 红包id
    @Json(name = "redenpStartTime")
    val redenpStartTime: String?,//红包雨开始时间
    @Json(name = "redenpEndTime")
    val redenpEndTime: String?,//红包雨结束时间
    @Json(name = "serverTime")
    val serverTime: String?,//服务器系统时间（毫秒）



)