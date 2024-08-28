package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
data class CheckSafeQuestionResp(
    val safeQuestion: String,
    val securityCode: String
):Parcelable