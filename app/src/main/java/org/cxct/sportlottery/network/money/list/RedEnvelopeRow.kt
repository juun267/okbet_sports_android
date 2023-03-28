package org.cxct.sportlottery.network.money.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class RedEnvelopeRow(
    @Json(name = "addTime")
    val addTime: Long,
    @Json(name = "balance")
    val balance: Double,
    @Json(name = "creditAccount")
    val creditAccount: Int,
    @Json(name = "currency")
    val currency: String,
    @Json(name = "money")
    val money: Double,
    @Json(name = "operateName")
    val operateName: String,
    @Json(name = "orderNo")
    val orderNo: String,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "remark")
    val remark: String,
    @Json(name = "statDate")
    val statDate: Long,
    @Json(name = "testFlag")
    val testFlag: Int,
    @Json(name = "tranType")
    val tranType: Int,
    @Json(name = "userId")
    val userId: Int,
    @Json(name = "userName")
    val userName: String
){
    var rechDateAndTime: String? = null
    var rechDateStr: String? = null
    var rechTimeStr: String? = null
    var rechTypeDisplay: String? = null
    var displayMoney: String? = null
    var tranTypeDisplay: String? = null
}
