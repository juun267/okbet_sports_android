package org.cxct.sportlottery.network.odds

import org.cxct.sportlottery.network.Constants.MATCH_ODDS_LIST
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


interface OddsService {

    @Headers("x-session-platform-code:plat1")
    @POST(MATCH_ODDS_LIST)
    suspend fun getOddsList(
        @Header("x-session-token") token: String,
        @Body oddsListRequest: OddsListRequest
    ): Response<OddsListResult>
}