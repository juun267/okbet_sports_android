package org.cxct.sportlottery.network.match

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface MatchService {
    @Headers("x-session-platform-code:plat1")
    @POST("/api/front/match/preload")
    suspend fun getMatchPreload(
        @Header("x-session-token") token: String,
        @Body matchPreloadRequest: MatchPreloadRequest
    ): MatchPreloadResponse
}