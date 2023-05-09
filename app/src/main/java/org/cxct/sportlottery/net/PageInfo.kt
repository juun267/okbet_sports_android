package org.cxct.sportlottery.net

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
open class PageInfo<T>() : java.io.Serializable {
    var pageNum: Int = 0
    var pageSize: Int = 0
    var totalNum: Int = 0
    var totalSize: Int = 0
    var records: List<T>? = null
}
