package org.cxct.sportlottery.net.money.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class DailyConfig(
    val additional: Float,
    val capped: Int,
    val first: Int,
    val principal: Int,
    val rewards: Int,
    val times: Int,
    val content:String,
    val activityType:Int,
    val type: Int?,
):Parcelable {

    fun isFirstDepositActivity() = activityType == 6

}