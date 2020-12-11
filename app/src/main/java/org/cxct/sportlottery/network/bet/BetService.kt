package org.cxct.sportlottery.network.bet

import org.cxct.sportlottery.network.bet.info.BetInfoRequest
import org.cxct.sportlottery.network.bet.info.BetInfoResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface BetService {
    @Headers("x-session-platform-code:plat1")
    @POST("https://sports.cxct.org/api/front/match/bet/info")
    suspend fun getBetInfo(
        @Header("x-session-token") token: String,
        @Body betInfoRequest: BetInfoRequest
    ): BetInfoResponse

    @Headers("x-session-platform-code:plat1")
    @POST("/api/front/match/bet/list")
    suspend fun getBetList(
        @Header("x-session-token") token: String,
        @Body betListRequest: BetListRequest
    ): BetListResponse
}