package org.cxct.sportlottery.network.news

import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

class SportNewsRequest(
    val messageType:Int,
    override val startTime: String? = null,
                       override val endTime: String? = null,
                       override val page: Int? = 1,
                       override val pageSize: Int? = 20): TimeRangeParams, PagingParams {
}