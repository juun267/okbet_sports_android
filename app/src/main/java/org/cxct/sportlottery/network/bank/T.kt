package org.cxct.sportlottery.network.bank


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class T(
    @Json(name = "addTime")
    val addTime: String,
    @Json(name = "bankName")
    val bankName: String,
    @Json(name = "cardNo")
    val cardNo: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "subAddress")
    val subAddress: String,
    @Json(name = "updateTime")
    val updateTime: String,
    @Json(name = "userId")
    val userId: Int,
    @Json(name = "uwType")
    val uwType: String
)