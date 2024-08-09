package org.cxct.sportlottery.net.money.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class IsFirstDeposit(
    val effectiveTime: Int,
    val enable: Boolean,
    val flowRatio: Int,
    val limit: Int,
    val percent: Int,
    val principal: Boolean,
    val rewards: Boolean,
    val type: Int,
    val validBetMoney: Int
):Parcelable