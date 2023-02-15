package org.cxct.sportlottery.network.user.authbind


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthBindData(
    @Json(name = "userName")
    val userName: String,
    @Json(name = "msg")
    val msg: String,
) {
}