package org.cxct.sportlottery.network.withdraw.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "accountMoney")
    val accountMoney: Double?,
    @Json(name = "appendInfo")
    val appendInfo: String?,
    @Json(name = "applyMoney")
    val applyMoney: Double?,
    @Json(name = "applyTime")
    val applyTime: String?,
    @Json(name = "bankAccount")
    val bankAccount: String?,
    @Json(name = "bankCard")
    val bankCard: String?,
    @Json(name = "bankName")
    val bankName: String?,
    @Json(name = "channel")
    val channel: Int?,
    @Json(name = "checkStatus")
    val checkStatus: Int?,
    @Json(name = "fee")
    val fee: Double?,
    @Json(name = "id")
    val id: Long,
    @Json(name = "operatorTime")
    val operatorTime: String?,
    @Json(name = "orderNo")
    val orderNo: String?,
    @Json(name = "reason")
    val reason: String?,
    @Json(name = "userId")
    val userId: Long,
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "uwType")
    val uwType: String?
)