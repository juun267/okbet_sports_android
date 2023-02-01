package org.cxct.sportlottery.network.lottery

import org.cxct.sportlottery.network.Constants.LOTTERY_GET
import retrofit2.Response
import retrofit2.http.GET

interface LotteryService {

    @GET(LOTTERY_GET)
    suspend fun getLotteryResult(): Response<LotteryResult>

}