package org.cxct.sportlottery.network.odds

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface OddsService {

    companion object {
        const val match_odds_list = "/api/front/match/odds/list"
    }

    @Headers("x-session-platform-code:plat1")
    @POST(match_odds_list)
    suspend fun getOddsList(
        @Header("x-session-token") token: String,
        @Body oddsListRequest: OddsListRequest
    ): Response<OddsListResult>
}