package org.cxct.sportlottery.network.bet.list

import org.cxct.sportlottery.common.IdParams
import org.cxct.sportlottery.common.PagingParams
import org.cxct.sportlottery.common.TimeRangeParams

data class BetListRequest(
    val statusList: List<Int>,
    val gameType: String? = null,
    val pagingParams: PagingParams? = null,
    val timeRangeParams: TimeRangeParams? = null,
    val idParams: IdParams? = null
)
