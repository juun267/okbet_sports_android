package org.cxct.sportlottery.network.bet.settledDetailList

data class ParlayComsDetailVO(
    val matchOddsVOList: List<MatchOddsVO>,
    val stake: Double,
    val status: Int,
    val winMoney: Double
)