package org.cxct.sportlottery.network.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
data class UserGameTypeDiscount(
    var discount: String,
    val gameType: String
):Parcelable
