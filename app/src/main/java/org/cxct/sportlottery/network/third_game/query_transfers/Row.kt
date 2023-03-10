package org.cxct.sportlottery.network.third_game.query_transfers


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Row(
    @Json(name = "addTime")
    val addTime: Long?,
    @Json(name = "dlId")
    val dlId: Int?,
    @Json(name = "dlName")
    val dlName: String?,
    @Json(name = "firmTypeIn")
    val firmTypeIn: String?,
    @Json(name = "firmTypeOut")
    val firmTypeOut: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "money")
    val money: Double?,
    @Json(name = "operator")
    val operator: String?,
    @Json(name = "operatorTime")
    val operatorTime: Long?,
    @Json(name = "orderNo")
    val orderNo: String?,
    @Json(name = "platformId")
    val platformId: Int?,
    @Json(name = "remark")
    val remark: String?,
    @Json(name = "statDate")
    val statDate: Long?,
    @Json(name = "status")
    val status: Int?,
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "userName")
    val userName: String?
)