package org.cxct.sportlottery.network.infoCenter

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@KeepMembers
@Parcelize
data class SetReadResult(
    @Json(name = "t")
    val t: SetReadData,
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
): BaseResult(),Parcelable
