package org.cxct.sportlottery.network.money

import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.USER_RECHARGE_LIST
import org.cxct.sportlottery.network.money.list.RechargeListRequest
import org.cxct.sportlottery.network.money.list.RechargeListResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MoneyService {

    @GET(Constants.RECHARGE_CONFIG_MAP)
    suspend fun getRechCfg(): Response<MoneyRechCfgResult>

    @POST(Constants.USER_RECHARGE_ADD)
    suspend fun rechargeAdd(
        @Body moneyAddRequest: MoneyAddRequest
    ): Response<MoneyAddResult>

    @POST(Constants.USER_RECHARGE_ONLINE_PAY)
    suspend fun rechargeOnlinePay(
        @Body moneyAddRequest: MoneyAddRequest
    ): Response<MoneyAddResult>

    @POST(USER_RECHARGE_LIST)
    suspend fun getUserRechargeList(
        @Body rechargeListRequest: RechargeListRequest
    ): Response<RechargeListResult>

}