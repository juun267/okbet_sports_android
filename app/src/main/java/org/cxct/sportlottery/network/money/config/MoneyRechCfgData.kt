package org.cxct.sportlottery.network.money.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class MoneyRechCfgData(
    @Json(name = "banks")
    val banks: List<Bank>,//银行lfny
    @Json(name = "rechCfgs")
    val rechCfgs: List<RechCfg>,//充值配置
    @Json(name = "rechTypes")
    val rechTypes: List<RechType>,//充值类型，name：名称，value：类型（onlinePayment：在线支付，bankTransfer：银行汇款，alipay：支付宝支付，weixin：微信支付，cft：财付通）
    @Json(name = "uwTypes")
    val uwTypes: List<UwType>,//提现相关设定 单笔提现最大金额 单笔提现最小金额 提现费率
    @Json(name = "rechSort")
    val rechSort: List<RechSort>?=null,//排序值
)




