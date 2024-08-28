package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
data class ForgetPasswordResp(
    val firstPhoneGiveMoney: Boolean,
    val msg: String,
    val userName: String,
    val vipType: Int
): Parcelable