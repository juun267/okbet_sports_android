package org.cxct.sportlottery.net.sport.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
data class CashOutResult(
    val cashoutAmount: String,
    val status: Int,
    var uniqNo: String?=null,
):Parcelable