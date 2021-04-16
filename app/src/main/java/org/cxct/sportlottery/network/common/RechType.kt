package org.cxct.sportlottery.network.common

enum class RechType(val code: String) {
    BANKTRANSFER("bankTransfer"),
    ONLINEPAYMENT("onlinePayment"),
    CRYPTOPAY("cryptoPay")
}