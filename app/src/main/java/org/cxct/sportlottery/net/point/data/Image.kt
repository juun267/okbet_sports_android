package org.cxct.sportlottery.net.point.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class Image(
    val path: String,
    val sort: Int
) : Parcelable