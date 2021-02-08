package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.feedback.*
import retrofit2.Response

class FeedbackRepository {

    suspend fun getFbQueryList(feedbackListRequest: FeedbackListRequest): Response<FeedbackListResult> {
        return OneBoSportApi.feedbackService.getFbQueryList(feedbackListRequest)
    }

    suspend fun fbSave(feedbackSaveRequest: FeedbackSaveRequest): Response<FeedBackBaseResult> {
        return OneBoSportApi.feedbackService.fbSave(feedbackSaveRequest)
    }

    suspend fun fbReply(feedbackReplyRequest: FeedbackReplyRequest): Response<FeedBackBaseResult> {
        return OneBoSportApi.feedbackService.fbReply(feedbackReplyRequest)
    }

    suspend fun fbQueryDetail(id: String): Response<FeedBackBaseResult> {
        return OneBoSportApi.feedbackService.fbQueryDetail(id)
    }
}