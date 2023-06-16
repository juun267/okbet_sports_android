package org.cxct.sportlottery.network.odds.eps

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.Odd

@Parcelize
@JsonClass(generateAdapter = true)
@KeepMembers
data class EpsOdd(
    @Json(name = "EPS")
    val eps: List<Odd>?,
) : Parcelable