package org.cxct.sportlottery.ui.finance.df

enum class UWType(val type: String) {
    BANK_TRANSFER("bankTransfer"),
    ADMIN_SUB_MONEY("adminSubMoney"),
    CRYPTO("cryptoTransfer"),
    E_WALLET("eWalletTransfer"),
    BETTING_STATION("bettingStation"),
    PAY_MAYA("paymayaTransfer"),
    BETTING_STATION_ADMIN("bettingStationAdmin")
}
