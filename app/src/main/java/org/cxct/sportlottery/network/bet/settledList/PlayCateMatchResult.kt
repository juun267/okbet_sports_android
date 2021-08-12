package org.cxct.sportlottery.network.bet.settledList

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PlayCateMatchResult(
    @Json(name = "statusNameI18n")
    val statusNameI18n: Map<String, String>?,
    @Json(name = "score")
    val score: String?,
) : Parcelable
