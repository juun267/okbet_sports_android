package org.cxct.sportlottery.network.withdraw.list

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams
@KeepMembers
data class WithdrawListRequest(
    val checkStatus: Int? = null,
    val uwType: String? = null,
    override val page: Int? = 1,
    override val pageSize: Int? = 20,
    override val startTime: String? = null,
    override val endTime: String? = null
) : PagingParams, TimeRangeParams
