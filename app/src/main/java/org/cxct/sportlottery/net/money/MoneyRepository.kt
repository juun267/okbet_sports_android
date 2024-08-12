package org.cxct.sportlottery.net.money

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.OtherApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.money.api.MoneyApiService
import org.cxct.sportlottery.net.money.data.DailyConfig
import org.cxct.sportlottery.net.money.data.FirstDepositDetail
import org.cxct.sportlottery.net.money.data.UniPaid

object MoneyRepository {

    val moneyApi by lazy { RetrofitHolder.createApiService(MoneyApiService::class.java) }

    suspend fun rechCheckStauts(params: JsonObject): ApiResult<String> {
        return moneyApi.rechCheckStauts(params)
    }
    suspend fun rechDailyConfig(): OtherApiResult<List<DailyConfig>, UniPaid> {
        return moneyApi.rechDailyConfig()
    }
    suspend fun firstDepositDetail(): ApiResult<FirstDepositDetail> {
        return moneyApi.firstDepositDetail()
    }
}