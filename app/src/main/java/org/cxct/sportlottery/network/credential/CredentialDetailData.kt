package org.cxct.sportlottery.network.credential

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class CredentialDetailData(
    @Json(name = "resultCode")
    val resultCode: String?,
    @Json(name = "resultStatus")
    val resultStatus: String?, //结果状态 “S”：成功 “F”：失败 “U”：未知问题
    @Json(name = "resultMessage")
    val resultMessage: String?,
): Parcelable
