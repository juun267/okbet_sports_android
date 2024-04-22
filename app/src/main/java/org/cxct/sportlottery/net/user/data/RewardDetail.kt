package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class RewardDetail(
    val enable: Boolean,
    val remainingWeekRedenpAmount: Double,
    val rewardType: Int,//(4:晋级礼金 5:生日礼金 6:每周红包 7:专属红包)
    val status: Int,//1:未中奖,2:待审核,3:审核不通过,4:审核通过,5:已领取,6:已失效,7:未领取, null:無資格（專屬紅包則視為未申請）
    val sumValidBetAmount: Double,
    val value: Double,
    val weekRedenpValidAmount: Double
): Parcelable