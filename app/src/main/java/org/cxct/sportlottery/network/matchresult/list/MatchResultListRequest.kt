package org.cxct.sportlottery.network.matchresult.list

import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

data class MatchResultListRequest(
    val gameType: String,
    val page: Int?,
    val pageSize: Int?,
    val startTime: String?,
    val endTime: String?,
)