package org.cxct.sportlottery.network.sport

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class sportMenuList(
    @Json(name = "sportMenu")
    var sportMenuList: MutableMap<String?, MutableMap<String?, SportMenuFilter>?>?
)
