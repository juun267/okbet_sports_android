package org.cxct.sportlottery.network.odds

import org.cxct.sportlottery.network.Constants.MATCH_ODDS_DETAIL
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_LIST
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface OddsService {

    @POST(MATCH_ODDS_LIST)
    suspend fun getOddsList(
        @Body oddsListRequest: OddsListRequest
    ): Response<OddsListResult>

    @POST(MATCH_ODDS_DETAIL)
    suspend fun getOddsDetail(
        @Body oddsListRequest: OddsDetailRequest
    ): Response<OddsDetailResult>

}