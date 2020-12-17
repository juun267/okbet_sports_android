package org.cxct.sportlottery.network.matchresult

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

const val MATCH_RESULT_LIST = "/api/front/match/result/list"

interface MatchResultService {

    @Headers("x-session-platform-code:plat1")
    @POST(MATCH_RESULT_LIST)
    suspend fun getMatchResultList(
        @Header("x-session-token") token: String,
        @Body matchResultListRequest: MatchResultListRequest
    ): Response<MatchResultListResult>
}