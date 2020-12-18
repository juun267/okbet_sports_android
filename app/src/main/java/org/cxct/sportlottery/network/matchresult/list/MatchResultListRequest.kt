package org.cxct.sportlottery.network.matchresult.list

import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

data class MatchResultListRequest(
    val gameType: String,
    val pagingParams: PagingParams? = null,
    val timeRangeParams: TimeRangeParams? = null
)