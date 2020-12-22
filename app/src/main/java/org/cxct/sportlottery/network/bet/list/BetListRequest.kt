package org.cxct.sportlottery.network.bet.list

import org.cxct.sportlottery.network.common.IdParams
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams
/*

data class BetListRequest(
    val statusList: List<Int>? = null, //状态 0：未确认，1：未结算，2：全赢，3：赢半，4：全输，5：输半，6：和，7：已取消
    val gameType: String? = null,
    val pagingParams: PagingParams? = null,
    val timeRangeParams: TimeRangeParams? = null,
    val idParams: IdParams? = null
)
*/

data class BetListRequest(
    val page: Int? = null,
    val pageSize: Int? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val statusList: List<Int>? = null, //状态 0：未确认，1：未结算，2：全赢，3：赢半，4：全输，5：输半，6：和，7：已取消
    val gameType: String? = null,
    val userId: Int? = null,
    val platformId: Int? = null
)
