package org.cxct.sportlottery.ui.finance.df

enum class RechType(val type: String) {
    ONLINE_PAYMENT("onlinePayment"),
    BANK_TRANSFER("bankTransfer"),
    ALIPAY("alipay"),
    WEIXIN("weixin"),
    CFT("cft"),
    ADMIN_ADD_MONEY("adminAddMoney"),
    CRYPTO("cryptoPay"), //虛擬幣轉帳
    GCASH("gcash"),
    GRABPAY("grabPay"),
    PAYMAYA("payMaya"),
    BETTING_STATION("bettingStation"),
    BETTING_STATION_AGENT("bettingStationAgent"),
    ACTIVITY("adminActivity"),
    REDEMTIONCODE("redemptionCodeAddMoney"),
}