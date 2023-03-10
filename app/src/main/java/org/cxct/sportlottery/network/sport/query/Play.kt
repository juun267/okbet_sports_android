package org.cxct.sportlottery.network.sport.query


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Play(
    @Json(name = "code")
    val code: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "num")
    val num: Int?,
    @Json(name = "selectionType")
    val selectionType: Int?,
    @Json(name = "playCateList")
    val playCateList: List<PlayCate>?
) {
    var isSelected = false
    var isLocked:Boolean? = null
}