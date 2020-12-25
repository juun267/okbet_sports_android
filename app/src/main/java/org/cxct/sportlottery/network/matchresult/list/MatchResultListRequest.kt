package org.cxct.sportlottery.network.matchresult.list

import org.cxct.sportlottery.network.common.TimeRangeParams

data class MatchResultListRequest(
    val gameType: String,
    val page: Int?,
    val pageSize: Int?,
    override val startTime: String?,
    override val endTime: String?,
) : TimeRangeParams