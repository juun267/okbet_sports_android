package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class VipUserLevelLimit(
    val activityId: Int,
    val allowReceiveDays: Int,
    val currency: String,
    val forcedAudit: Boolean,
    val registerDays: Int,
    val type: Int,
    val withdrawMultiple: Int
): Parcelable