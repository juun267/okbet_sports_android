package org.cxct.sportlottery.network.sport

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class sportMenuList(
    @Json(name = "sportMenu")
    var sportMenuList: MutableMap<String?, MutableMap<String?, SportMenuFilter>?>?
)
