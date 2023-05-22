package org.cxct.sportlottery.network.user.credit

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.PagingParams

@KeepMembers
data class CreditCircleHistoryRequest(
    override val page: Int,
    override val pageSize: Int
) : PagingParams
