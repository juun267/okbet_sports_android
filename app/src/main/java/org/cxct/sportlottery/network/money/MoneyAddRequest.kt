package org.cxct.sportlottery.network.money

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class MoneyAddRequest(
    val rechCfgId: Int,//充值配置id
    val bankCode: String?,//银行代码
    val depositMoney: String?,//充值金额
    val payer: String?,//充值账号
    val payerName: String,//充值人名称
    val payerBankName: String?,//充值银行名称
    val payerInfo: String?,//充值附加信息
    val depositDate: Long,//充值日期
    var appsFlyerId: String? = null,
    var appsFlyerKey: String? = null,
    var appsFlyerPkgName: String? = null,
    val clientType: Int = 2,
    val activityType: Int?=null //提交充值后带入此参数原参数participate废弃
    val type: Int? = null
) {
    var payee: String? = null
    var payeeName: String? = null
    var txHashCode: String? = null//虚拟币转账交易单号
    var voucherPath: String? = null//虚拟币转账凭证
    var proofImg: String? = null //支付凭证图片，转账执联

}
