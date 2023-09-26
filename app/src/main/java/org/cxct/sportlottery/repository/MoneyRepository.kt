package org.cxct.sportlottery.repository


import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.config.MoneyRechCfgResult
import retrofit2.Response

object MoneyRepository {

    suspend fun getRechCfg(): Response<MoneyRechCfgResult> {
        return OneBoSportApi.moneyService.getRechCfg()
    }

    suspend fun rechargeAdd(moneyAddRequest: MoneyAddRequest): Response<MoneyAddResult> {
        return OneBoSportApi.moneyService.rechargeAdd(moneyAddRequest)
    }

    suspend fun rechCheckStauts(map: HashMap<String,String>): ApiResult<String> {
        return OneBoSportApi.moneyService.rechCheckStauts(map.toMap())
    }
}