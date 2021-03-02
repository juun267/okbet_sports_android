package org.cxct.sportlottery.network.third_game

import org.cxct.sportlottery.network.Constants.QUERY_FIRST_ORDERS
import org.cxct.sportlottery.network.Constants.QUERY_SECOND_ORDERS
import org.cxct.sportlottery.network.Constants.THIRD_ALL_TRANSFER_OUT
import org.cxct.sportlottery.network.Constants.THIRD_AUTO_TRANSFER
import org.cxct.sportlottery.network.Constants.THIRD_GAMES
import org.cxct.sportlottery.network.Constants.THIRD_GET_ALL_BALANCE
import org.cxct.sportlottery.network.Constants.THIRD_LOGIN
import org.cxct.sportlottery.network.Constants.THIRD_QUERY_TRANSFERS
import org.cxct.sportlottery.network.Constants.THIRD_TRANSFER
import org.cxct.sportlottery.network.third_game.money_transfer.GetAllBalanceResult
import org.cxct.sportlottery.network.third_game.query_transfers.QueryTransfersRequest
import org.cxct.sportlottery.network.third_game.query_transfers.QueryTransfersResult
import org.cxct.sportlottery.network.third_game.third_games.ThirdGamesResult
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.OtherBetHistoryRequest
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.OtherBetHistoryResult
import retrofit2.Response
import retrofit2.http.*

interface ThirdGameService {
    @GET(THIRD_GET_ALL_BALANCE)
    suspend fun getAllBalance(
    ): Response<GetAllBalanceResult>

    @GET(THIRD_ALL_TRANSFER_OUT)
    suspend fun allTransferOut(
    ): Response<BlankResult>

    @GET(THIRD_GAMES)
    suspend fun getThirdGames(
    ): Response<ThirdGamesResult>

    @GET(THIRD_TRANSFER)
    suspend fun transfer(
        @Path("outPlat") outPlat: String,
        @Path("inPlat") inPlat: String,
        @Query("amount") amount: Long
    ): Response<BlankResult>

    @POST(THIRD_QUERY_TRANSFERS)
    suspend fun queryTransfers(
        @Body queryTransfersRequest: QueryTransfersRequest
    ): Response<QueryTransfersResult>

    @GET(THIRD_AUTO_TRANSFER)
    suspend fun autoTransfer(
        @Path("inPlat") inPlat: String?
    ): Response<AutoTransferResult>

    @GET(THIRD_LOGIN)
    suspend fun thirdLogin(
        @Path("firmType") firmType: String?,
        @Query("gameCode") gameCode: String?,
        @Query("loginSrc") loginSrc: Int = 2, //登录来源（0：WEB, 1：MOBILE_BROWSER, 2：ANDROID, 3：IOS）
    ): Response<ThirdLoginResult>

    @POST(QUERY_FIRST_ORDERS)
    suspend fun queryFirstOrders(
        @Body queryTransfersRequest: OtherBetHistoryRequest
    ): Response<OtherBetHistoryResult>

    @POST(QUERY_SECOND_ORDERS)
    suspend fun querySecondOrders(
        @Body queryTransfersRequest: OtherBetHistoryRequest
    ): Response<QueryTransfersResult>

}
