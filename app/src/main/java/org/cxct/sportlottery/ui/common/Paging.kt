package org.cxct.sportlottery.ui.common

interface Paging {
    var pageSize: Int
    var pageSizeLoad: Int
    var pageSizeTotal: Int

    fun initPage() {
        pageSizeLoad = 0
        pageSizeTotal = 0
    }

    fun getPageIndex(): Int {
        return (pageSizeLoad / pageSize)
    }

    fun isLastPage(): Boolean {
        return pageSizeLoad >= pageSizeTotal
    }
}