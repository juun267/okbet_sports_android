package org.cxct.sportlottery.network.odds

import org.cxct.sportlottery.network.Constants.MATCH_ODDS_LIST
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

const val MATCH_ODDS_LIST = "/api/front/match/odds/list"
const val MATCH_ODDS_Detail = "/api/front/match/odds/detail"


interface OddsService {

    @Headers("x-session-platform-code:plat1")
    @POST(MATCH_ODDS_LIST)
    suspend fun getOddsList(
        @Header("x-session-token") token: String,
        @Body oddsListRequest: OddsListRequest
    ): Response<OddsListResult>

    @Headers("x-session-platform-code:plat1")
    @POST(MATCH_ODDS_Detail)
    suspend fun getOddsDetail(
        @Body oddsListRequest: OddsDetailRequest
    ): Response<OddsDetailResult>

}