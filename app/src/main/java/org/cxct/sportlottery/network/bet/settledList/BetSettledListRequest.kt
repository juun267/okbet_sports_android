 package org.cxct.sportlottery.network.bet.settledList

import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

data class BetSettledListRequest(
    val gameType: String? = null,
    override val startTime: String? = null,
    override val endTime: String? = null,
    override val page: Int? = null,
    override val pageSize: Int? = null,
) : TimeRangeParams, PagingParams
