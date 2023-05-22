 package org.cxct.sportlottery.network.bet.settledDetailList

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

 @KeepMembers
data class BetSettledDetailListRequest(
    val gameType: String? = null,
    val statDate: String? = null,
    override val startTime: String? = null,
    override val endTime: String? = null,
    override var page: Int? = null,
    override val pageSize: Int? = null,
) : TimeRangeParams, PagingParams
