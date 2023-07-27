package org.cxct.sportlottery.network.match

import org.cxct.sportlottery.network.Constants.MATCH_LIVE_ROUND
import retrofit2.Response
import retrofit2.http.*


interface MatchService {


    @GET("$MATCH_LIVE_ROUND/{roundNo}")
    suspend fun getMatchLiveRound(
        @Path("roundNo") roundNo: String,
    ): Response<MatchRoundResult>


}