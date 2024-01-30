package org.cxct.sportlottery.network.news

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
class SportNewsRequest(
    val messageType:Int,
    val startTime: String? = null,
    val endTime: String? = null,
    val page: Int? = 1,
    val pageSize: Int? = 20,
    val typeList: Array<Int>? = null) {
}