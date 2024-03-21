package org.cxct.sportlottery.network.infoCenter

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
@Parcelize
data class SetReadData(
    @Json(name = "unReadCounts")
    val unReadCounts: Int,
): Parcelable
