package org.cxct.sportlottery.ui.betRecord

import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.bet.list.Row

data class BetListData(
    val row: List<Row>,
    val oddsType: OddsType,
    val totalMoney: Double,
    val page: Int,
    val isLastPage: Boolean,
)