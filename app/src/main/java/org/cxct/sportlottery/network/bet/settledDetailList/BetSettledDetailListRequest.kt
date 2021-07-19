 package org.cxct.sportlottery.network.bet.settledDetailList

import org.cxct.sportlottery.network.common.IdParams
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

data class BetSettledDetailListRequest(
    val gameType: String? = null,
    override val startTime: String? = null,
    override val endTime: String? = null,
    override val page: Int? = null,
    override val pageSize: Int? = null,
) : TimeRangeParams, PagingParams
