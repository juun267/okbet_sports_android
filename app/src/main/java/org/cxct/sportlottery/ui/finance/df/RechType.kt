package org.cxct.sportlottery.ui.finance.df

enum class RechType(val type: String) {
    ONLINE_PAYMENT("onlinePayment"),
    BANK_TRANSFER("bankTransfer"),
    ALIPAY("alipay"),
    WEIXIN("weixin"),
    CFT("cft"),
    ADMIN_ADD_MONEY("adminAddMoney")
}