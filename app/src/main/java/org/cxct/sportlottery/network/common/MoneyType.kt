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
    GCASH_TYPE("gcash"),
    GRABPAY_TYPE("grabPay"),
    PAYMAYA_TYPE("payMaya"),
    PAYPAL_TYPE("paypal"),
    ONLINE_TYPE("ic_online_pay"),
    CRYPTO_TYPE("cryptoPay"),
    JUAN_ONLINE_TYPE("ic_juancash"), // 菲律賓在線網銀 JuanCash
    DISPENSHIN("ic_juancash"),//TODO Bill 等補圖 202:出款
    ONLINEBANK("ic_juancash"),//等補圖 203:網銀
    GCASH("ic_g_cash"),//204:Gcash
    GRABPAY("ic_grab_pay"),//205:GrabPay
    PAYMAYA("ic_pay_maya"),//206:PayMaya
    PAYPAL("ic_paypal"),//210 Paypal okBet要用
}