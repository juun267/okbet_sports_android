package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class VipDetail(
    val growthMultiple: String,
    val highestRebateRateInfo: List<HighestRebateRateInfo>,
    val otherGrowthMultiple: String,
    val vipUserLevelLimits: List<VipUserLevelLimit>
): Parcelable