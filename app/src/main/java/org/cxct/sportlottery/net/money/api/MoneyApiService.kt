package org.cxct.sportlottery.net.money.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.money.data.DailyConfig
import org.cxct.sportlottery.network.Constants
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

//用户账户相关Api
interface MoneyApiService {

    @POST(Constants.RECH_CHECK_STATUS)
    suspend fun rechCheckStauts(@Body params: JsonObject): ApiResult<String>

    @GET(Constants.RECH_DAILY_CONFIG)
    suspend fun rechDailyConfig(): ApiResult<List<DailyConfig>>
}