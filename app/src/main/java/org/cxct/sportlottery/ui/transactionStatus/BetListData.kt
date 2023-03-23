package org.cxct.sportlottery.ui.transactionStatus

import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.common.OddsType

data class BetListData(
    val row: List<Row>,
    val oddsType: OddsType,
    val totalMoney: Double,
    val page: Int,
    val isLastPage: Boolean,
)