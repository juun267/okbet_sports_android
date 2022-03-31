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
    @Json(name = "id")
    val id: Int?,
    @Json(name = "message")
    val message: String,
    @Json(name = "msgType")
    val msgType: Int,
    @Json(name = "platformId")
    val platformId: Int?,
    @Json(name = "rechLevels")
    val rechLevels: String,
    @Json(name = "sort")
    val sort: Int,
    @Json(name = "title")
    val title: String,
    @Json(name = "type")
    val type: Int,
    @Json(name = "updateTime")
    val updateTime: String
) : Parcelable {
    var showDate: String = ""
}