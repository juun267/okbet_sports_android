package org.cxct.sportlottery.network.quest

import org.cxct.sportlottery.network.Constants.CLAIM_ALL_REWARD
import org.cxct.sportlottery.network.Constants.CLAIM_REWARD
import org.cxct.sportlottery.network.Constants.QUEST_CHECK
import org.cxct.sportlottery.network.Constants.QUEST_GUEST_INFO
import org.cxct.sportlottery.network.Constants.QUEST_INFO
import org.cxct.sportlottery.network.Constants.TIME_LINE
import org.cxct.sportlottery.network.quest.check.CheckResult
import org.cxct.sportlottery.network.Constants.QUEST_JOIN
import org.cxct.sportlottery.network.quest.claimAllReward.ClaimAllRewardRequest
import org.cxct.sportlottery.network.quest.claimAllReward.ClaimAllRewardResult
import org.cxct.sportlottery.network.quest.claimReward.ClaimRewardResult
import org.cxct.sportlottery.network.quest.info.QuestInfoResult
import org.cxct.sportlottery.network.quest.timeLine.TimeLineResult
import org.cxct.sportlottery.network.quest.join.QuestJoinResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface QuestService {

    @GET(QUEST_GUEST_INFO)
    suspend fun getQuestGuestInfo(@Query("device") device: Int): Response<QuestInfoResult>

    @GET(QUEST_INFO)
    suspend fun getQuestInfo(@Query("device") device: Int): Response<QuestInfoResult>

    @POST(QUEST_JOIN)
    suspend fun postQuestJoin(@Path("questId") questId: Long,@Query("device") device: Int): Response<QuestJoinResult>

    @POST(CLAIM_REWARD)
    suspend fun postClaimReward(@Path("rewardId") rewardId: Long): Response<ClaimRewardResult>

    @POST(CLAIM_ALL_REWARD)
    suspend fun postClaimAllReward(@Body claimAllRewardRequest: ClaimAllRewardRequest): Response<ClaimAllRewardResult>

    @GET(TIME_LINE)
    suspend fun getTimeLine(): Response<TimeLineResult>

    @POST(QUEST_CHECK)
    suspend fun postQuestCheck(@Query("device") device: Int): Response<CheckResult>


}