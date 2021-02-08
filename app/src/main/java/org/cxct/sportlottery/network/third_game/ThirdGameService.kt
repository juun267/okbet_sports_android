package org.cxct.sportlottery.network.third_game

import org.cxct.sportlottery.network.Constants.GET_ALL_BALANCE
import org.cxct.sportlottery.network.Constants.THIRD_ALL_TRANSFER_OUT
import org.cxct.sportlottery.network.Constants.THIRD_GAMES
import org.cxct.sportlottery.network.Constants.TRANSFER
import org.cxct.sportlottery.network.third_game.money_transfer.GetAllBalanceResult
import org.cxct.sportlottery.network.third_game.third_games.ThirdGamesResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ThirdGameService {
    @GET(GET_ALL_BALANCE)
    suspend fun getAllBalance(
    ): Response<GetAllBalanceResult>

    @GET(THIRD_ALL_TRANSFER_OUT)
    suspend fun allTransferOut(
    ): Response<BlankResult>

    @GET(THIRD_GAMES)
    suspend fun getThirdGames(
    ): Response<ThirdGamesResult>

    @POST(TRANSFER)
    suspend fun transfer(
        @Path("outPlat") outPlat: String,
        @Path("inPlat") inPlat: String,
        @Path("amount") amount: Long
    ): Response<BlankResult>

}
