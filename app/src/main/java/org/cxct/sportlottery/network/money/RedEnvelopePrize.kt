package org.cxct.sportlottery.network.money

data class RedEnvelopePrize(
    val grabMoney: String,//抢包金额
    val betMoney: String?,//红包雨设置打码量
    val userBetMoney: String?,//用户实际打码量

)
