package org.cxct.sportlottery.network.money.config


class MoneyRechCfg {
    enum class Switch(val code: Int) { ON(1) }
}

/**
 * MoneyConfig, uwTypes 提現Type
 */
enum class TransferType(val type: String) {
    BANK("bankTransfer"), //銀行卡
    CRYPTO("cryptoTransfer") //虛擬幣
}