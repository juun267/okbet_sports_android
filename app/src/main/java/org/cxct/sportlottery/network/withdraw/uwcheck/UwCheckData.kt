package org.cxct.sportlottery.network.withdraw.uwcheck

import com.squareup.moshi.Json
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
class UwCheckData(
    @Json(name = "needCheck")
    val needCheck: Boolean?,
    @Json(name = "checkList")
    val checkList: List<CheckList>?,
    @Json(name = "total")
    val total: TotalData?
)