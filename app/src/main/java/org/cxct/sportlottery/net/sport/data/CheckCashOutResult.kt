package org.cxct.sportlottery.net.sport.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
data class CheckCashOutResult(
    val cashoutAmount: String,
    val cashoutStatus: Int,
    val orderNo: String,
    val uniqNo: String
): Parcelable