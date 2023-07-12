package org.cxct.sportlottery.net.user.data


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
@JsonClass(generateAdapter = true)
data class ActivityImageList(
    @Json(name = "activityId")
    val activityId: String?,
    @Json(name = "activityType")
    val activityType: Int,
    @Json(name = "amount")
    val amount: Int,
    @Json(name = "contentImage")
    val contentImage: String,
    @Json(name = "contentText")
    val contentText: String,
    @Json(name = "createdAt")
    val createdAt: Long,
    @Json(name = "endTime")
    val endTime: Long,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "imageSort")
    val imageSort: Int,
    @Json(name = "indexImage")
    val indexImage: String,
    @Json(name = "multiple")
    val multiple: Int,
    @Json(name = "popImage")
    val popImage: String,
    @Json(name = "reward")
    val reward: Int,
    @Json(name = "startTime")
    val startTime: Long,
    @Json(name = "subTitleText")
    val subTitleText: String,
    @Json(name = "titleImage")
    val titleImage: String,
    @Json(name = "titleText")
    val titleText: String
):Parcelable