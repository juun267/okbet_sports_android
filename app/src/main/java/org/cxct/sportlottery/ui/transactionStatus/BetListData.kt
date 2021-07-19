package org.cxct.sportlottery.ui.transactionStatus

import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.menu.OddsType

data class BetListData(
    val row: List<Row>,
    val oddsType: OddsType,
    val totalMoney: Long,
    val page: Int,
    val isLastPage: Boolean
)
