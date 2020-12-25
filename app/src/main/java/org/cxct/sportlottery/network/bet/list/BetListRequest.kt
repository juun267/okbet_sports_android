package org.cxct.sportlottery.network.bet.list

import org.cxct.sportlottery.network.common.IdParams
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

data class BetListRequest(
    val statusList: List<Int>,
    val gameType: String? = null,
    val idParams: IdParams? = null,
    override val startTime: String? = null,
    override val endTime: String? = null,
    override val page: Int? = null,
    override val pageSize: Int? = null,
) : TimeRangeParams, PagingParams
