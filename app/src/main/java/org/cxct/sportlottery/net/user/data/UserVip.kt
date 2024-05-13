package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class UserVip(
    val exp: Long,
    val levelCode: String?,
    val levelName: String,
    val protectionLevelGrowthValue: Long,
    val protectionStatus: Int,
    val rewardInfo: List<RewardInfo>,
    val upgradeExp: Long
): Parcelable