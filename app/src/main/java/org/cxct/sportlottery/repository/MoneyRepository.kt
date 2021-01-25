package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.MoneyRechCfgResult
import retrofit2.Response

class MoneyRepository {

    suspend fun getRechCfg(): Response<MoneyRechCfgResult> {
        return OneBoSportApi.moneyService.getRechCfg()
    }

    suspend fun rechargeAdd(moneyAddRequest: MoneyAddRequest): Response<MoneyAddResult> {
        return OneBoSportApi.moneyService.rechargeAdd(moneyAddRequest)
    }

    suspend fun rechargeOnlinePay(moneyAddRequest: MoneyAddRequest): Response<MoneyAddResult> {
        return OneBoSportApi.moneyService.rechargeOnlinePay(moneyAddRequest)
    }

}