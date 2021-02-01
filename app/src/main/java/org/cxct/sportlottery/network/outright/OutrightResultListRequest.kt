package org.cxct.sportlottery.network.outright

import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

data class OutrightResultListRequest(
    val gameType: String,
    override val startTime: String? = null,
    override val endTime: String? = null,
    override val page: Int? = null,
    override val pageSize: Int? = null,
) : TimeRangeParams, PagingParams
