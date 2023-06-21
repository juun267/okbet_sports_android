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
    DISPENSHIN("ic_juancash"),//202 Peter說要隱藏掉
    ONLINEBANK("ic_internet_bank"),//TODO Bill等補圖 203:網銀
    GCASH("ic_g_cash"),//204:Gcash
    GRABPAY("ic_grab_pay"),//205:GrabPay
    PAYMAYA("ic_pay_maya"),//206:PayMaya
    PAYPAL("ic_paypal"),//210 Paypal okBet要用
    DRAGONPAY("ic_gragon_pay"),//211:DragonPay
    DRAGONPAY_TYPE("gragon"),//211:DragonPay
    MOMOPAY("ic_momopay"),//102:MoMoPay
    ZALOPAY("ic_zalopay"),//103:ZaloPay
    VIETTELPAY("ic_viettelpay"),//107:ViettelPay
    RECHARGE_CARD("ic_recharge_card"),//108:刮刮卡充值 //Recharge Card
    QQONLINE("ic_online_qq"),//4:在線QQ
    FORTUNE_PAY("ic_fortunepay"),//212 fortunePay
    AUB("ic_aub"),//213 AUB
    EPON("ic_epon")//214 E-PON
}