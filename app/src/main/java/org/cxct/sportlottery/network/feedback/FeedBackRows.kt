package org.cxct.sportlottery.network.feedback

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class FeedBackRows(
    @Json(name = "id")
    val id: Int?,
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "platformId")
    val platformId: Int?,
    @Json(name = "type")
    val type: Int?, // 类型（0-充值问题，1-提款问题，2-其他问题，3-提交建议，4-我要投诉, 5-客服反馈，6-玩家回复, 10-开奖网信息反馈）
    @Json(name = "content")
    val content: String?,
    @Json(name = "addTime")
    val addTime: Long?,
    @Json(name = "status")
    val status: Int?,// 状态（0:待反馈，1:已反馈）
    @Json(name = "track")
    val track: Int?,
    @Json(name = "lastFeedbackTime")
    val lastFeedbackTime: Long?,
    @Json(name = "feedbackCode")
    val feedbackCode: String?,
): MultiItemEntity{
    override val itemType: Int = userId?:0
}