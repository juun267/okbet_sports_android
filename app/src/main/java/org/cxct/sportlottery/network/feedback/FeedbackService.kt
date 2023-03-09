package org.cxct.sportlottery.network.feedback

import org.cxct.sportlottery.network.Constants.FEEDBACK_QUERYDETAIL
import org.cxct.sportlottery.network.Constants.FEEDBACK_QUERYLIST
import org.cxct.sportlottery.network.Constants.FEEDBACK_REPLY
import org.cxct.sportlottery.network.Constants.FEEDBACK_SAVE
import org.cxct.sportlottery.network.NetResult
import retrofit2.Response
import retrofit2.http.*

interface FeedbackService {

    //反馈列表
    @POST(FEEDBACK_QUERYLIST)
    suspend fun getFbQueryList(
        @Body feedbackListRequest: FeedbackListRequest
    ): Response<FeedbackListResult>

    //提交反馈信息
    @POST(FEEDBACK_SAVE)
    suspend fun fbSave(
        @Body feedbackSaveRequest: FeedbackSaveRequest
    ): Response<NetResult>

    //回复反馈信息
    @POST(FEEDBACK_REPLY)
    suspend fun fbReply(
        @Body feedbackReplyRequest: FeedbackReplyRequest
    ): Response<NetResult>

    //回复反馈信息
    @GET(FEEDBACK_QUERYDETAIL)
    suspend fun fbQueryDetail(
        @Path("id") id: String
    ): Response<FeedbackListResult>

}
