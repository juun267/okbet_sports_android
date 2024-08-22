package org.cxct.sportlottery.net.money.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class FirstDepositConfig(
    val effectiveTime: Int,
    val enable: Boolean,
    val flowRatio: Int,//所需流水倍数
    val limit: Int,
    val percent: Float,
    val principal: Boolean,
    val rewards: Boolean,
    val type: Int,
    val validBetMoney: Int,
    val rewardAmount: Int,
    val checkAmount: Int //流水稽需要检查的基数
):Parcelable