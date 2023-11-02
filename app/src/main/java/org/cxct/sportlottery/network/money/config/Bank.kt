package org.cxct.sportlottery.network.money.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Bank(

    @Json(name = "value")
    val value: String?,
    @Json(name = "bankType")
    val bankType: Int
) {
    @Json(name = "name")
    var name: String? = null
    get() {
        return if (field == "PayMaya") "Maya" else field
    }
    var isSelected = false
}
