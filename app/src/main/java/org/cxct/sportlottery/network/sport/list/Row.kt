package org.cxct.sportlottery.network.sport.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Row(
    @Json(name = "code")
    val code: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "sortNum")
    val sortNum: Int,
    @Json(name = "state")
    val state: Int, //状态（1：启用；0：停用）
)