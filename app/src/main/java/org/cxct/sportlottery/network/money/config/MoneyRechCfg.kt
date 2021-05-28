package org.cxct.sportlottery.network.money


class MoneyRechCfg {
    enum class Switch(val code: Int) { ON(1), CLOSE(0) }
}

/**
 * MoneyConfig, uwTypes 提現Type
 */
enum class TransferType(val type: String) {
    BANK("bankTransfer"), //銀行卡
    CRYPTO("cryptoTransfer") //虛擬幣
}