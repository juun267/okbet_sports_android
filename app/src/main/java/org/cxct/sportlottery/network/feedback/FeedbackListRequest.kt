package org.cxct.sportlottery.network.feedback

import org.cxct.sportlottery.network.common.PagingParams

data class FeedbackListRequest(
    override val page: Int? = null,//当前页
    override val pageSize: Int? = null,//每页条数
    val startTime: String? = null,
    val endTime: String? = null,
    val userId: Int? = null,
    val userName: String? = null, //用户名
    val platformId: Int? = null, //平台id
    val feedbackCode: String? = null,//反馈码
    val type: Int? = null,//类型（0-充值问题，1-提款问题，2-其他问题，3-提交建议，4-我要投诉, 5-客服反馈，6-玩家回复, 10-开奖网信息反馈）
    val status: Int? = null//状态（0:待反馈，1:已反馈）
) : PagingParams
