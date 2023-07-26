package org.cxct.sportlottery.network.money.list

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

@KeepMembers
data class RechargeListRequest(
    val rechType: String? = null,
    val status: Int? = null,
    override val startTime: String? = null,
    override val endTime: String? = null,
    override val page: Int? = 1,
    override val pageSize: Int? = 20
) : TimeRangeParams, PagingParams
