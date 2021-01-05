package org.cxct.sportlottery.network.InfoCenter

import org.cxct.sportlottery.network.common.PagingParams

data class InfoCenterRequest(
    override val page: Int? = null,//当前页
    override val pageSize: Int? = null,//每页条数
) : PagingParams
