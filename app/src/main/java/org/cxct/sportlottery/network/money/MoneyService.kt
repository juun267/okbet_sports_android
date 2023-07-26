package org.cxct.sportlottery.network.money

import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.RED_ENVELOPE_PRIZE
import org.cxct.sportlottery.network.Constants.USER_BILL_LIST
import org.cxct.sportlottery.network.Constants.USER_RECHARGE_LIST
import org.cxct.sportlottery.network.money.config.MoneyRechCfgResult
import org.cxct.sportlottery.network.money.list.*
import retrofit2.Response
import retrofit2.http.*

interface MoneyService {

    @GET(Constants.RECHARGE_CONFIG_MAP)
    suspend fun getRechCfg(): Response<MoneyRechCfgResult>

    @POST(Constants.USER_RECHARGE_ADD)
    suspend fun rechargeAdd(
        @Body moneyAddRequest: MoneyAddRequest
    ): Response<MoneyAddResult>

    @POST(USER_RECHARGE_LIST)
    suspend fun getUserRechargeList(
        @Body rechargeListRequest: RechargeListRequest
    ): Response<RechargeListResult>

    @POST(USER_BILL_LIST)
    suspend fun getBillList(
        @Body request: SportBillListRequest
    ): Response<SportBillResult>

    @GET(Constants.RED_ENVELOPE_CHECK)
    suspend fun getRainInfo(): Response<RedEnvelopeResult>

    @GET(RED_ENVELOPE_PRIZE)
    suspend fun getRedEnvelopePrize(
        @Path("redEnpId") redEnpId: Int? ,
    ): Response<RedEnvelopePrizeResult>


    @POST(Constants.PACKET_LIST)
    suspend fun getRedEnvelopeHistoryList(
        @Body withdrawListRequest: RedEnvelopeListRequest
    ): Response<RedEnvelopeListResult>

}