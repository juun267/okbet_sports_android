package org.cxct.sportlottery.network.outright

import org.cxct.sportlottery.network.Constants.OUTRIGHT_BET_ADD
import org.cxct.sportlottery.network.Constants.OUTRIGHT_BET_INFO
import org.cxct.sportlottery.network.Constants.OUTRIGHT_ODDS_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_RESULT_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_SEASON_LIST
import org.cxct.sportlottery.network.bet.info.BetInfoRequest
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.outright.bet.add.OutrightBetAddRequest
import org.cxct.sportlottery.network.outright.bet.add.OutrightBetAddResult
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListRequest
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListResult
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

    @POST(OUTRIGHT_SEASON_LIST)
    suspend fun getOutrightSeasonList(
        @Body outrightSeasonListRequest: OutrightSeasonListRequest
    ): Response<OutrightSeasonListResult>

    @POST(OUTRIGHT_BET_ADD)
    suspend fun addOutrightBet(
        @Body outrightBetAddRequest: OutrightBetAddRequest
    ): Response<OutrightBetAddResult>

    @POST(OUTRIGHT_BET_INFO)
    suspend fun getOutrightBetInfo(
        @Body outrightBetInfoRequest: BetInfoRequest
    ): Response<BetInfoResult>
}