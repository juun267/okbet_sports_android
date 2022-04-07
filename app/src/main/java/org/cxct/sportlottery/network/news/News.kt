package org.cxct.sportlottery.network.news


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class News(
    @Json(name = "addTime")
    val addTime: String,
    @Json(name = "message")
    val message: String,
    @Json(name = "title")
    val title: String,
) : Parcelable {
    var showDate: String = ""
}