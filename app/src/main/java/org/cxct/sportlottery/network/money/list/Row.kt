package org.cxct.sportlottery.network.money.list


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class Row(
    @Json(name = "account")
    val account: String,
    @Json(name = "accountMoney")
    val accountMoney: Double,
    @Json(name = "actualMoney")
    val actualMoney: Double?,
    @Json(name = "channel")
    val channel: Double?,
    @Json(name = "fullName")
    val fullName: String,
    @Json(name = "id")
    val id: Double,
    @Json(name = "onlineType")
    val onlineType: Double?,
    @Json(name = "operatorTime")
    val operatorTime: Long?,
    @Json(name = "orderNo")
    val orderNo: String,
    @Json(name = "payee")
    val payee: String,
    @Json(name = "payeeBankName")
    val payeeBankName: String?,
    @Json(name = "payeeName")
    val payeeName: String,
    @Json(name = "payer")
    val payer: String?,
    @Json(name = "payerBankName")
    val payerBankName: String?,
    @Json(name = "payerInfo")
    val payerInfo: String?,
    @Json(name = "payerName")
    val payerName: String,
    @Json(name = "rebateMoney")
    val rebateMoney: Double?,
    @Json(name = "rechLevel")
    val rechLevel: String,
    @Json(name = "rechMoney")
    val rechMoney: Double,
    @Json(name = "rechName")
    val rechName: String,
    @Json(name = "rechTime")
    val rechTime: Long,
    @Json(name = "addTime")
    val addTime: Long,
    @Json(name = "rechType")
    val rechType: String,
    @Json(name = "remark")
    val remark: String?,
    @Json(name = "statDate")
    val statDate: Long,
    @Json(name = "status")
    val status: Int,
    @Json(name = "thirdChannel")
    val thirdChannel: String?,
    @Json(name = "thirdOrderNo")
    val thirdOrderNo: String?,
    @Json(name = "userId")
    val userId: Double,
    @Json(name = "userName")
    val userName: String,
    @Json(name = "reason")
    val reason: String?
): Parcelable{
    var rechDateAndTime: String? = null
    var rechDateStr: String? = null
    var rechTimeStr: String? = null
    var rechState: String? = null
    var rechTypeDisplay: String? = null
    var displayMoney: String? = null
}
