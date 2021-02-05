package org.cxct.sportlottery.network.third_game

import org.cxct.sportlottery.network.Constants.GET_ALL_BALANCE
import org.cxct.sportlottery.network.Constants.THIRD_ALL_TRANSFER_OUT
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.third_game.money_transfer.GetAllBalanceResult
import retrofit2.Response
import retrofit2.http.GET

interface ThirdGameService {
    @GET(GET_ALL_BALANCE)
    suspend fun getAllBalance(
    ): Response<GetAllBalanceResult>

    @GET(THIRD_ALL_TRANSFER_OUT)
    suspend fun allTransferOut(
    ): Response<AllTransferOutResult>

}
