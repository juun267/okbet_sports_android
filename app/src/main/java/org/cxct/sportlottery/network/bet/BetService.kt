package org.cxct.sportlottery.network.bet

import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.bet.info.BetInfoRequest
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.BetListResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


const val MATCH_BET_INFO = "/api/front/match/bet/info"
const val MATCH_BET_ADD = "/api/front/match/bet/add"
const val MATCH_BET_LIST = "/api/front/match/bet/list"

interface BetService {
    @Headers("x-session-platform-code:plat1")
    @POST(MATCH_BET_INFO)
    suspend fun getBetInfo(
        @Header("x-session-token") token: String,
        @Body betInfoRequest: BetInfoRequest
    ): Response<BetInfoResult>

    @Headers("x-session-platform-code:plat1")
    @POST(MATCH_BET_ADD)
    suspend fun addBet(
        @Header("x-session-token") token: String,
        @Body betAddRequest: BetAddRequest
    ): Response<BetAddResult>

    @Headers("x-session-platform-code:plat1")
    @POST(MATCH_BET_LIST)
    suspend fun getBetList(
        @Header("x-session-token") token: String,
        @Body betListRequest: BetListRequest
    ): Response<BetListResult>
}