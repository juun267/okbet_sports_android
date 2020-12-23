package org.cxct.sportlottery.network.odds

import org.cxct.sportlottery.network.Constants.MATCH_ODDS_DETAIL
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_LIST
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.odds.list.OddsListRequest
import org.cxct.sportlottery.network.odds.list.OddsListResult
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