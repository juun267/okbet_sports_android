package org.cxct.sportlottery.network.matchresult

import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayListResult
import retrofit2.Response
import retrofit2.http.*

const val MATCH_RESULT_LIST = "/api/front/match/result/list"
const val MATCH_RESULT_PLAY_LIST = "/api/front/match/result/play/list"

interface MatchResultService {

    @Headers("x-session-platform-code:plat1")
    @POST(MATCH_RESULT_LIST)
    suspend fun getMatchResultList(
        @Header("x-session-token") token: String,
        @Body matchResultListRequest: MatchResultListRequest
    ): Response<MatchResultListResult>

    @Headers("x-session-platform-code:plat1")
    @GET(MATCH_RESULT_PLAY_LIST)
    suspend fun getMatchResultPlayList(
        @Header("x-session-token") token: String,
        @Query("matchId") matchId: String
    ): Response<MatchResultPlayListResult>
}