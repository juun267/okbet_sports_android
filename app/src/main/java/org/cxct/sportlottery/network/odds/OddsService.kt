package org.cxct.sportlottery.network.odds

import org.cxct.sportlottery.network.Constants.MATCH_INPLAY_ALL
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_DETAIL
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_EPS_LIST
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_LIST
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_QUICK_LIST
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.odds.eps.OddsEpsListRequest
import org.cxct.sportlottery.network.odds.eps.OddsEpsListResult
import org.cxct.sportlottery.network.odds.list.OddsAllListResult
import org.cxct.sportlottery.network.odds.list.OddsListRequest
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.odds.quick.QuickListResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface OddsService {

    @POST(MATCH_ODDS_LIST)
    suspend fun getOddsList(
        @Body oddsListRequest: OddsListRequest
    ): Response<OddsListResult>

    @POST(MATCH_ODDS_DETAIL)
    suspend fun getOddsDetail(@Body oddsListRequest: OddsDetailRequest): Response<OddsDetailResult>

    @POST(MATCH_ODDS_EPS_LIST)
    suspend fun getEpsList(
        @Body oddsEpsListRequest: OddsEpsListRequest,
    ): Response<OddsEpsListResult>

    @POST(MATCH_ODDS_QUICK_LIST)
    suspend fun getQuickList(
        @Body quickListRequest: QuickListRequest,
    ): Response<QuickListResult>

    @POST(MATCH_INPLAY_ALL)
    suspend fun getInPlayAllList(
        @Body oddsListRequest: OddsListRequest,
    ): Response<OddsAllListResult>
}