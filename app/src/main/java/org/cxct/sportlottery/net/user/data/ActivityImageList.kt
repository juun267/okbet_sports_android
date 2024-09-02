package org.cxct.sportlottery.net.user.data


import android.os.Parcelable
import com.stx.xhb.androidx.entity.BaseBannerInfo
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
data class ActivityImageList(
    val activityId: String?,
    val activityType: Int,
    val amount: Double,
    val contentImage: String?=null,
    val contentText: String?=null,
    val createdAt: Long,
    val endTime: Long,
    val id: Int?,
    val imageSort: Int,
    val indexImage: String?=null,
    val multiple: Int,
    val popImage: String?=null,
    val reward: Double,
    val startTime: Long,
    val subTitleText: String?=null,
    val titleImage: String?=null,
    val titleText: String?=null,
    val frontPageShow: Int,
    val imageLink: String?=null,
):Parcelable, BaseBannerInfo {
    var typeName: String = ""
    init {
        typeName = activityType.toString()
    }
    override fun getXBannerUrl(): String {
        return indexImage?:""
    }

    override fun getXBannerTitle(): String {
        return titleText?:""
    }
}