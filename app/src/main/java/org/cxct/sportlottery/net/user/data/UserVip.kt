package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class UserVip(
    val exp: Int,
    val levelCode: String?,
    val levelName: String,
    val protectionLevelGrowthValue: Int,
    val protectionStatus: Int,
    val rewardInfo: List<RewardInfo>,
    val upgradeExp: Long
): Parcelable