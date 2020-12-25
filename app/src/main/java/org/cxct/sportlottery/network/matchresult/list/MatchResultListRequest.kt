package org.cxct.sportlottery.network.matchresult.list

import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

data class MatchResultListRequest(
    val gameType: String,
    override val startTime: String?,
    override val endTime: String?,
    override val page: Int? = null,
    override val pageSize: Int? = null,
) : TimeRangeParams, PagingParams