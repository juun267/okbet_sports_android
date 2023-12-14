package org.cxct.sportlottery.net

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
open class PageData<T>() : java.io.Serializable {
    var totalCount: Int = 0
    var data: List<T>? = null
}
