package org.cxct.sportlottery.network.withdraw.list


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class Row(
    @Json(name = "accountMoney")
    val accountMoney: Double?,
    @Json(name = "appendInfo")
    val appendInfo: String?,
    @Json(name = "applyMoney")
    val applyMoney: Double?,
    @Json(name = "applyTime")
    val applyTime: Long?,
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
    val operatorTime: Long?,
    @Json(name = "orderNo")
    val orderNo: String?,
    @Json(name = "reason")
    val reason: String?,
    @Json(name = "userId")
    val userId: Long,
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "uwType")
    val uwType: String?,
    @Json(name = "deductMoney")
    val deductMoney: Double?,
    @Json(name = "appointmentDate")
    val appointmentDate: String?,
    @Json(name = "appointmentHour")
    val appointmentHour: String?,
    @Json(name = "actualMoney")
    val actualMoney: Double?,
): Parcelable {
    var withdrawDateAndTime: String? = null
    var operatorDateAndTime: String? = null
    var withdrawDate: String? = null
    var withdrawTime: String? = null
    var withdrawState: String? = null
    var withdrawType: String? = null
    var displayMoney: String? = null
    var withdrawDeductMoney: String? = null
    var orderState: Int? = null
    var children: List<Row>? = null
}