package org.cxct.sportlottery.net.user.data


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.stx.xhb.androidx.entity.BaseBannerInfo
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
    val amount: Double,
    @Json(name = "contentImage")
    val contentImage: String?=null,
    @Json(name = "contentText")
    val contentText: String?=null,
    @Json(name = "createdAt")
    val createdAt: Long,
    @Json(name = "endTime")
    val endTime: Long,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "imageSort")
    val imageSort: Int,
    @Json(name = "indexImage")
    val indexImage: String?=null,
    @Json(name = "multiple")
    val multiple: Int,
    @Json(name = "popImage")
    val popImage: String?=null,
    @Json(name = "reward")
    val reward: Double,
    @Json(name = "startTime")
    val startTime: Long,
    @Json(name = "subTitleText")
    val subTitleText: String?=null,
    @Json(name = "titleImage")
    val titleImage: String?=null,
    @Json(name = "titleText")
    val titleText: String?=null,
    @Json(name = "frontPageShow")
    val frontPageShow: Int,
    @Json(name = "imageLink")
    val imageLink: String?=null,
):Parcelable