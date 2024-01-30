package org.cxct.sportlottery.network.news

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

@KeepMembers
class SportNewsRequest(
    val messageType:Int,
    val startTime: String? = null,
    val endTime: String? = null,
    val page: Int? = 1,
    val pageSize: Int? = 20,
    val typelist: Array<Int>? = null) {
}