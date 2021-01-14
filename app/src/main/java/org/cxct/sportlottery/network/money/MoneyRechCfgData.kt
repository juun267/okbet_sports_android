package org.cxct.sportlottery.network.money

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class MoneyRechCfgData(
    @Json(name = "banks")
    var banks: MutableList<MoneyRechCfg.Bank>,//银行lfny
    @Json(name = "rechCfgs")
    var rechCfgs: MutableList<MoneyRechCfg.RechConfig>,//充值配置
    @Json(name = "rechTypes")
    var rechTypes: MutableList<MoneyRechCfg.RechType>,//充值类型，name：名称，value：类型（onlinePayment：在线支付，bankTransfer：银行汇款，alipay：支付宝支付，weixin：微信支付，cft：财付通）
    @Json(name = "withdrawCfg")
    var withdrawCfg: MoneyRechCfg.WithdrawCfg//提现相关设定 单笔提现最大金额 单笔提现最小金额 提现费率
)




