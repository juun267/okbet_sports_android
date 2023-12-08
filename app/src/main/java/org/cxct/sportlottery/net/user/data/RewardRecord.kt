package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class RewardRecord(
    val activityName: String?=null,
    val addTime: Long,
    val applyTime: String?=null,
    val auditBy: String?=null,
    val auditId: String?=null,
    val auditTime: String?=null,
    val checkAmount: Double,
    val flowRatio: Double,
    val id: Int,
    val platformId: Int,
    val rechargeAmount: Double,
    val recordStatus: Int,//1待审核，2已发放，3已拒绝
    val remark: String?=null,
    val rewardAmount: Double,
    val statDate: Long,
    val userId: Int,
    val userName: String
): Parcelable