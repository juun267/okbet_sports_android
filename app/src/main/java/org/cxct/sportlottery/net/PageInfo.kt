package org.cxct.sportlottery.net


open class PageInfo<T>() : java.io.Serializable {
    var pageNum: Int = 0
    var pageSize: Int = 0
    var totalNum: Int = 0
    var totalSize: Int = 0
    var records: List<T>? = null
}
