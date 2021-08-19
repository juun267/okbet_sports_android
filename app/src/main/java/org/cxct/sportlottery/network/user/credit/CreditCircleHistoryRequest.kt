package org.cxct.sportlottery.network.user.credit

import org.cxct.sportlottery.network.common.PagingParams

data class CreditCircleHistoryRequest(
    override val page: Int,
    override val pageSize: Int
) : PagingParams
