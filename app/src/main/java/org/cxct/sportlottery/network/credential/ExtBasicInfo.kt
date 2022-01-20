package org.cxct.sportlottery.network.credential

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize


@Parcelize
@JsonClass(generateAdapter = true)
data class ExtBasicInfo (
    @Json(name = "certType")
    val certType: String?,
    @Json(name = "certNo")
    val certNo: String?,
    @Json(name = "certName")
    val certName: String?,
): Parcelable
