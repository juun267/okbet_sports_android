package org.cxct.sportlottery.network.user.authbind


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class AuthBindData(
    @Json(name = "userName")
    val userName: String,
    @Json(name = "msg")
    val msg: String,
) {
}