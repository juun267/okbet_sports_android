package org.cxct.sportlottery.network.outright

import org.cxct.sportlottery.network.Constants.OUTRIGHT_ODDS_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_RESULT_LIST
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OutrightService {

    @POST(OUTRIGHT_ODDS_LIST)
    suspend fun getOutrightOddsList(
        @Body outrightOddsListRequest: OutrightOddsListRequest
    ): Response<OutrightOddsListResult>

    @POST(OUTRIGHT_RESULT_LIST)
    suspend fun getOutrightResultList(
        @Body outrightResultListRequest: OutrightResultListRequest
    ): Response<OutrightResultListResult>

}