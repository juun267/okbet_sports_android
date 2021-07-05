package org.cxct.sportlottery.ui.transactionStatus

import org.cxct.sportlottery.network.bet.list.Row

data class BetListData(val row: List<Row>, val totalMoney: Long, val page: Int, val isLastPage: Boolean)
