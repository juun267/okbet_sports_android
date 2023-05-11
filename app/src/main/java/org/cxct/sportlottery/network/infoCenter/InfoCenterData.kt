package org.cxct.sportlottery.network.infoCenter

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
@Parcelize
data class InfoCenterData(
    @Json(name = "id")
    val id: Int?,
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "addDate")
    val addDate: String?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "content")
    val content: String?,
    @Json(name = "isRead")//是否已读 0未读 1已读
    val isRead: Int?,
    @Json(name = "noticeType")//消息类型 0消息通知、1app推送
    val noticeType: Int?,
    @Json(name = "msgShowType")//消息类型 1发送至用户消息中心、2右下角弹出提示、3页面中央弹出提示、4app推送
    val msgShowType: Int?,
    @Json(name = "platformId")//平台id
    val platformId: Int?,
    @Json(name = "operatorName")//发件人
    val operatorName: String?
): Parcelable
