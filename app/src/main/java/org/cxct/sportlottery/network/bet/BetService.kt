package org.cxct.sportlottery.network.bet

import org.cxct.sportlottery.network.Constants.MATCH_BET_ADD
import org.cxct.sportlottery.network.Constants.MATCH_BET_INFO
import org.cxct.sportlottery.network.Constants.MATCH_BET_LIST
import org.cxct.sportlottery.network.Constants.MATCH_BET_REMARK_BET
import org.cxct.sportlottery.network.Constants.MATCH_BET_SETTLED_DETAIL_LIST
import org.cxct.sportlottery.network.Constants.MATCH_BET_SETTLED_LIST
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.bet.settledDetailList.BetInfoRequest
import org.cxct.sportlottery.network.bet.settledDetailList.BetInfoResult
import org.cxct.sportlottery.network.bet.settledDetailList.BetSettledDetailListRequest
import org.cxct.sportlottery.network.bet.settledDetailList.BetSettledDetailListResult
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.network.bet.settledList.BetSettledListRequest
import org.cxct.sportlottery.network.bet.settledList.BetSettledListResult
import org.cxct.sportlottery.network.bet.settledList.RemarkBetResult
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

    @POST(MATCH_BET_SETTLED_LIST)
    suspend fun getBetSettledList(
        @Body betSettledListRequest: BetSettledListRequest
    ): Response<BetSettledListResult>

    @POST(MATCH_BET_SETTLED_DETAIL_LIST)
    suspend fun getBetSettledDetailList(
        @Body betSettledDetailListRequest: BetSettledDetailListRequest
    ): Response<BetSettledDetailListResult>

    @POST(MATCH_BET_INFO)
    suspend fun getBetInfo(
        @Body betInfoRequest: BetInfoRequest
    ): Response<BetInfoResult>

    @POST(MATCH_BET_REMARK_BET)
    suspend fun reMarkBet(
        @Body remarkBetRequest:RemarkBetRequest
    ): Response<RemarkBetResult>



}