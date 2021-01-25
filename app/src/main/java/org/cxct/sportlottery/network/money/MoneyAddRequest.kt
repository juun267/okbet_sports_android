package org.cxct.sportlottery.network.money

data class MoneyAddRequest(
    val rechCfgId: Int,//充值配置id
    val bankCode: String,//银行代码
    val depositMoney: Int,//充值金额
    val payer: String,//充值账号
    val payerName: String,//充值人名称
    val payerBankName: String,//充值银行名称
    val payerInfo: String,//充值附加信息
    val depositDate: Long,//充值日期
)
