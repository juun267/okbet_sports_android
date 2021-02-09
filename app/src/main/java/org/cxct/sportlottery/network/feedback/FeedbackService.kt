package org.cxct.sportlottery.network.feedback

import org.cxct.sportlottery.network.Constants.FEEDBACK_QUERYDETAIL
import org.cxct.sportlottery.network.Constants.FEEDBACK_QUERYLIST
import org.cxct.sportlottery.network.Constants.FEEDBACK_REPLY
import org.cxct.sportlottery.network.Constants.FEEDBACK_SAVE
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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
    ): Response<FeedBackBaseResult>

    //回复反馈信息
    @POST(FEEDBACK_REPLY)
    suspend fun fbReply(
        @Body feedbackReplyRequest: FeedbackReplyRequest
    ): Response<FeedBackBaseResult>

    //回复反馈信息
    @GET(FEEDBACK_QUERYDETAIL)
    suspend fun fbQueryDetail(
        @Query("id") id: String
    ): Response<FeedbackListResult>

}
