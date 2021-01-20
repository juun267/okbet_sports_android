package org.cxct.sportlottery.network.money

import org.cxct.sportlottery.network.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MoneyService {

    @GET(Constants.RECHARGE_CONFIG_MAP)
    suspend fun getRechCfg(): Response<MoneyRechCfgResult>

}