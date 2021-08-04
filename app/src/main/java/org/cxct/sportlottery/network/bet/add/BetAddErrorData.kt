package org.cxct.sportlottery.network.bet.add

data class BetAddErrorData(
    val id: String?,
    val odds: Double?,
    val hkOdds: Double?,
    val producerId: Int?,
    val spread: String?,
    val status: Int?
)