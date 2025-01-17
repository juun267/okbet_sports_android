package org.cxct.sportlottery.network.matchresult

import org.cxct.sportlottery.network.Constants.MATCH_RESULT_LIST
import org.cxct.sportlottery.network.Constants.MATCH_RESULT_PLAY_LIST
import org.cxct.sportlottery.network.matchresult.list.MatchResultListRequest
import org.cxct.sportlottery.network.matchresult.list.MatchResultListResult
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayListResult
import retrofit2.Response
import retrofit2.http.*


interface MatchResultService {

    @POST(MATCH_RESULT_LIST)
    suspend fun getMatchResultList(
        @Body matchResultListRequest: MatchResultListRequest
    ): Response<MatchResultListResult>

    @GET(MATCH_RESULT_PLAY_LIST)
    suspend fun getMatchResultPlayList(
        @Query("matchId") matchId: String
    ): Response<MatchResultPlayListResult>
}