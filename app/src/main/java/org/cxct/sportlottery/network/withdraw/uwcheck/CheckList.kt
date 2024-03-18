package org.cxct.sportlottery.network.withdraw.uwcheck

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
class CheckList(
    @Json(name = "addTime")
    val addTime: String?,
    @Json(name = "money")
    val money: Double?,
    @Json(name = "orderNo")
    val orderNo: String?,
    @Json(name = "type")
    val type: String?,
    @Json(name = "validCheckAmount")
    val validCheckAmount: Double?,
    @Json(name = "finishValidAmount")
    val finishValidAmount: Double?,
    @Json(name = "unFinishValidAmount")
    val unFinishValidAmount: Double?,
    @Json(name = "deductMoney")
    val deductMoney: Long?,
    @Json(name = "isPass")
    val isPass: Int?
):Parcelable