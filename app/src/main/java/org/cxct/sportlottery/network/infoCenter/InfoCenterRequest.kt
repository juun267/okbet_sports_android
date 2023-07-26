package org.cxct.sportlottery.network.infoCenter

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.PagingParams

@KeepMembers
data class InfoCenterRequest(
    override val page: Int? = null,//当前页
    override val pageSize: Int? = null,//每页条数
    val isRead: Int? = null//1:已讀,0:未讀
) : PagingParams
