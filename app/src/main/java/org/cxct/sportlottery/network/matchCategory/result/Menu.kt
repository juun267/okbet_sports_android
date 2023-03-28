package org.cxct.sportlottery.network.matchCategory.result


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Menu(
    @Json(name = "code")
    val code: String?,
    @Json(name = "id")
    val id: Long?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "sortNum")
    val sortNum: Int?
)