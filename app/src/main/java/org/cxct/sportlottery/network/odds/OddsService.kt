package org.cxct.sportlottery.network.odds

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface OddsService {
    @Headers("x-session-platform-code:plat1")
    @POST("/api/front/match/odds/list")
    suspend fun getOddsList(
        @Header("x-session-token") token: String,
        @Body oddsListRequest: OddsListRequest
    ): OddsListResponse
}