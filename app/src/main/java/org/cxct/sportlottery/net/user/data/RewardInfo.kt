package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class RewardInfo(
    val exclusiveService: Boolean,
    val expressWithdrawal: Boolean,
    val highestRebateRate: Double,
    val levelCode: String,
    val levelName: String,
    val levelV2Id: Int,
    val rewardDetail: List<RewardDetail>,
    val sportHighestRebateRate: Double,
    val upgradeExp: Long
): Parcelable