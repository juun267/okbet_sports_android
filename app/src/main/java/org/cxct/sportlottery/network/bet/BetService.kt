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

interface BetService {

    companion object {
        const val match_bet_info = "/api/front/match/bet/info"
        const val match_bet_add = "/api/front/match/bet/add"
        const val match_bet_list = "/api/front/match/bet/list"
    }


    @Headers("x-session-platform-code:plat1")
    @POST(match_bet_info)
    suspend fun getBetInfo(
        @Header("x-session-token") token: String,
        @Body betInfoRequest: BetInfoRequest
    ): Response<BetInfoResult>

    @Headers("x-session-platform-code:plat1")
    @POST(match_bet_add)
    suspend fun addBet(
        @Header("x-session-token") token: String,
        @Body betAddRequest: BetAddRequest
    ): Response<BetAddResult>

    @Headers("x-session-platform-code:plat1")
    @POST(match_bet_list)
    suspend fun getBetList(
        @Header("x-session-token") token: String,
        @Body betListRequest: BetListRequest
    ): Response<BetListResult>
}