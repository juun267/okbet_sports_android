package org.cxct.sportlottery.network.feedback

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class FeedbackSaveRequest(
    val content: String,//反馈内容
    val status: Int ?= 0, //状态（0:待反馈，1:已反馈）
    val type: Int ?= 3// 类型（0-充值问题，1-提款问题，2-其他问题，3-提交建议，4-我要投诉, 5-客服反馈，6-玩家回复）
)