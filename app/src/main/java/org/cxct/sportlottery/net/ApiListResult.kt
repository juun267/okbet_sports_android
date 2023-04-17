package org.cxct.sportlottery.net

// 响应列表数据
class ApiListResult<T>(): ApiResult<T>() {

    var total: Int = 0

    private val rows: T? = null

    override fun getData() = rows
}