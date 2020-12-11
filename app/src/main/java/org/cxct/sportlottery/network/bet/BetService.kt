package org.cxct.sportlottery.network.bet

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface BetService {
    @Headers("x-session-platform-code:plat1")
    @POST("/api/front/match/bet/list")
    suspend fun getBetList(
        @Header("x-session-token") token: String,
        @Body betListRequest: BetListRequest
    ): BetListResponse
}