package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class HighestRebateRateInfo(
    val data: List<RateInfo>,
    val levelCode: String,
    val levelName: String
): Parcelable