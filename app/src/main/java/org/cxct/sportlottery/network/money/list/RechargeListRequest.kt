package org.cxct.sportlottery.network.money.list

import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

data class RechargeListRequest(
    val rechType: String,
    val status: Int? = null,
    override val startTime: String? = null,
    override val endTime: String? = null,
    override val page: Int? = null,
    override val pageSize: Int? = null
) : TimeRangeParams, PagingParams
