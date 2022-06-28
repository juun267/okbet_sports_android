package org.cxct.sportlottery.network.money

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult


@JsonClass(generateAdapter = true)
data class RedEnvelopeInfo(
    val redenpId: Int,//主键 红包id
    val redenpStartTime: String?,//红包雨开始时间
    val redenpEndTime: String?,//红包雨结束时间
    val serverTime: String?,//服务器系统时间（毫秒）

)
