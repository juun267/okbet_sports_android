package org.cxct.sportlottery.network.third_game

import org.cxct.sportlottery.network.Constants.GET_ALL_BALANCE
import org.cxct.sportlottery.network.third_game.money_transfer.GetAllBalanceResponse
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddRequest
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ThirdGameService {
    @GET(GET_ALL_BALANCE)
    suspend fun getAllBalance(
    ): Response<GetAllBalanceResponse>

}
