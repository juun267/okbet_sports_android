package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.feedback.*
import retrofit2.Response

object FeedbackRepository {

    suspend fun getFbQueryList(feedbackListRequest: FeedbackListRequest): Response<FeedbackListResult> {
        return OneBoSportApi.feedbackService.getFbQueryList(feedbackListRequest)
    }

    suspend fun fbSave(feedbackSaveRequest: FeedbackSaveRequest): Response<NetResult> {
        return OneBoSportApi.feedbackService.fbSave(feedbackSaveRequest)
    }

    suspend fun fbReply(feedbackReplyRequest: FeedbackReplyRequest): Response<NetResult> {
        return OneBoSportApi.feedbackService.fbReply(feedbackReplyRequest)
    }

    suspend fun fbQueryDetail(id: String): Response<FeedbackListResult> {
        return OneBoSportApi.feedbackService.fbQueryDetail(id)
    }
}