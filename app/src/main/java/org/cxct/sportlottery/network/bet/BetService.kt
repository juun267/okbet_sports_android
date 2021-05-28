package org.cxct.sportlottery.network.bet

import org.cxct.sportlottery.network.Constants.MATCH_BET_ADD
import org.cxct.sportlottery.network.Constants.MATCH_BET_LIST
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.BetListResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface BetService {
    @POST(MATCH_BET_ADD)
    suspend fun addBet(
        @Body betAddRequest: BetAddRequest
    ): Response<BetAddResult>

    @POST(MATCH_BET_LIST)
    suspend fun getBetList(
        @Body betListRequest: BetListRequest
    ): Response<BetListResult>
}