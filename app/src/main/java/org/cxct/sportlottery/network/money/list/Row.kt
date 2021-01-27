package org.cxct.sportlottery.network.money.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "account")
    val account: String,
    @Json(name = "accountMoney")
    val accountMoney: Int,
    @Json(name = "actualMoney")
    val actualMoney: Int,
    @Json(name = "channel")
    val channel: Double,
    @Json(name = "fullName")
    val fullName: String,
    @Json(name = "id")
    val id: Double,
    @Json(name = "onlineType")
    val onlineType: Double,
    @Json(name = "operatorTime")
    val operatorTime: String,
    @Json(name = "orderNo")
    val orderNo: String,
    @Json(name = "payee")
    val payee: String,
    @Json(name = "payeeBankName")
    val payeeBankName: String,
    @Json(name = "payeeName")
    val payeeName: String,
    @Json(name = "payer")
    val payer: String,
    @Json(name = "payerBankName")
    val payerBankName: String,
    @Json(name = "payerInfo")
    val payerInfo: String,
    @Json(name = "payerName")
    val payerName: String,
    @Json(name = "rebateMoney")
    val rebateMoney: Int,
    @Json(name = "rechLevel")
    val rechLevel: String,
    @Json(name = "rechMoney")
    val rechMoney: Int,
    @Json(name = "rechName")
    val rechName: String,
    @Json(name = "rechTime")
    val rechTime: String,
    @Json(name = "rechType")
    val rechType: String,
    @Json(name = "remark")
    val remark: String,
    @Json(name = "statDate")
    val statDate: String,
    @Json(name = "status")
    val status: Double,
    @Json(name = "thirdChannel")
    val thirdChannel: String,
    @Json(name = "thirdOrderNo")
    val thirdOrderNo: String,
    @Json(name = "userId")
    val userId: Double,
    @Json(name = "userName")
    val userName: String
)