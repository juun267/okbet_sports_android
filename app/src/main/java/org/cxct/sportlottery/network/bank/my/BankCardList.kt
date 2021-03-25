package org.cxct.sportlottery.network.bank.my


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.money.TransferType
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class BankCardList(
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
    val uwType: String,
    @Json(name = "bankCode")
    val bankCode: String,
) : Serializable {
    var transferType: TransferType = TransferType.BANK
}