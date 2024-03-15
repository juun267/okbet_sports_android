package org.cxct.sportlottery.network.withdraw.uwcheck

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
class TotalData(
    @Json(name = "deductMoney")
    val deductMoney: Double?,
    @Json(name = "unFinishValidAmount")
    val unFinishValidAmount: Double?
):Parcelable