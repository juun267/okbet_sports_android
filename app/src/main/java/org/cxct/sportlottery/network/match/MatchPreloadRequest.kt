package org.cxct.sportlottery.network.match

import org.cxct.sportlottery.network.common.TimeRangeParams

data class MatchPreloadRequest(
    val matchType: String,
    val num: Int? = null,
    override val startTime: String? = null,
    override val endTime: String? = null,
) : TimeRangeParams