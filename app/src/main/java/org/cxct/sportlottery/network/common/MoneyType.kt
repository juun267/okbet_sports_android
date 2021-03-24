package org.cxct.sportlottery.network.common

enum class MoneyType(val code: String) {
    BANK("ic_bank_atm"),
    ALI("ic_alipay"),
    WX("ic_wechat_pay"),
    CTF("ic_tenpay"),
    ONLINE("ic_online_pay"),
    CRYPTO("ic_crypto_pay"),
    BANK_TYPE("bankTransfer"),
    ALI_TYPE("alipay"),
    WX_TYPE("weixin"),
    CTF_TYPE("cft"),
    ONLINE_TYPE("ic_online_pay"),
    CRYPTO_TYPE("ic_crypto_pay")
}