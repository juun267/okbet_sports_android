package org.cxct.sportlottery.net.user.data


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
data class ActivityImageList(
    val activityId: String?,
    val activityType: Int,
    val amount: Int,
    val contentImage: String?=null,
    val contentText: String?=null,
    val createdAt: Long,
    val endTime: Long,
    val id: Int?,
    val imageSort: Int,
    val indexImage: String?=null,
    val multiple: Int,
    val popImage: String?=null,
    val reward: Int,
    val startTime: Long,
    val subTitleText: String?=null,
    val titleImage: String?=null,
    val titleText: String?=null,
    val frontPageShow: Int,
    val imageLink: String?=null,
):Parcelable