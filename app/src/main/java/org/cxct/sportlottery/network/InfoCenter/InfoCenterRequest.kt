package org.cxct.sportlottery.network.InfoCenter

data class InfoCenterRequest(
    val page: Int,//当前页
    val pageSize: Int//每页条数
)
