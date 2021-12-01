package org.cxct.sportlottery.network.bet.add


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class StatusNameI18n(
    @Json(name = "mapKey")
    val mapKey: String
) : Parcelable