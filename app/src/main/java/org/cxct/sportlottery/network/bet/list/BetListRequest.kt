package org.cxct.sportlottery.network.bet.list

import org.cxct.sportlottery.network.common.IdParams
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

data class BetListRequest(
    val championOnly: Int,
    val statusList: List<Int?> = listOf(),
    val gameType: String? = null,
    override val userId: Int? = null,
    override val platformId: Int? = null,
    override val startTime: String? = null,
    override val endTime: String? = null,
    override val page: Int? = null,
    override val pageSize: Int? = null,
) : TimeRangeParams, PagingParams, IdParams
