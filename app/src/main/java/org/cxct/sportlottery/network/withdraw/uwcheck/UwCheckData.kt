package org.cxct.sportlottery.network.withdraw.uwcheck

import com.squareup.moshi.Json

class UwCheckData(
    @Json(name = "needCheck")
    val needCheck: Boolean?,
    @Json(name = "checkList")
    val checkList: List<CheckList>?,
    @Json(name = "total")
    val total: TotalData?
)