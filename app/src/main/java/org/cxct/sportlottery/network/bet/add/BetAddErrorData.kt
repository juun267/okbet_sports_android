package org.cxct.sportlottery.network.bet.add

data class BetAddErrorData(
    val id: String?,
    val matchId: String?,
    val odds: Double?,
    val status: Int?
)