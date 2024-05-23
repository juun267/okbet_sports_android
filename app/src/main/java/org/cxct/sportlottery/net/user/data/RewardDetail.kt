package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class RewardDetail(
    val enable: Boolean=true,
    val remainingWeekRedenpAmount: Double? = null,
    val rewardType: Int = 0,//(4:晋级礼金 5:生日礼金 6:每周红包 7:专属红包)
    val status: Int = 0,//1:未中奖,2:待审核,3:审核不通过,4:审核通过,5:已领取,6:已失效,7:未领取, null:無資格（專屬紅包則視為未申請）
    val sumValidBetAmount: Double = 0.0,
    val value: Double = 0.0,
    val weekRedenpValidAmount: Double = 0.0,
    val otherType: Int = 0 //exclusiveService=true 为1, expressWithdrawal=true 为2,
): Parcelable