package org.cxct.sportlottery.network.money

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//TODO : 與MoneyRechCfg重複, 需整理移除冗余
@JsonClass(generateAdapter = true)
data class MoneyRechCfgData(
    @Json(name = "banks")
    val banks: List<MoneyRechCfg.Bank>,//银行lfny
    @Json(name = "rechCfgs")
    val rechCfgs: List<MoneyRechCfg.RechConfig>,//充值配置
    @Json(name = "rechTypes")
    val rechTypes: List<MoneyRechCfg.RechType>,//充值类型，name：名称，value：类型（onlinePayment：在线支付，bankTransfer：银行汇款，alipay：支付宝支付，weixin：微信支付，cft：财付通）
    @Json(name = "uwTypes")
    val uwTypes: List<MoneyRechCfg.UwTypeCfg>//提现相关设定 单笔提现最大金额 单笔提现最小金额 提现费率
)




