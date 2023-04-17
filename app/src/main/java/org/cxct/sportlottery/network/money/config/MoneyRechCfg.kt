package org.cxct.sportlottery.network.money.config


class MoneyRechCfg {
    enum class Switch(val code: Int) { ON(1) }
}

/**
 * MoneyConfig, uwTypes 提現Type
 */
enum class TransferType(val type: String) {
    BANK("bankTransfer"), //銀行卡
    CRYPTO("cryptoTransfer"), //虛擬幣
    E_WALLET("eWalletTransfer"), //e wallet
    STATION("bettingStation"), //e wallet
    PAYMAYA("paymayaTransfer") //e wallet
}

/**
 * 銀行類型 bankType 0:銀行卡, 1:eWallet
 */
enum class BankType{
    BANK, E_WALLET
}
const val PAYMAYA = "PayMaya"