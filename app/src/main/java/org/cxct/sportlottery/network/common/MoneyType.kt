package org.cxct.sportlottery.network.common

enum class MoneyType(val code: String) {
    BANK("bankTransfer"),
    ALI("alipay"),
    WX("weixin"),
    CTF("cft"),
    ONLINE("onlinePayment")
}