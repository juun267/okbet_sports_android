package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.MoneyRechCfgResult
import retrofit2.Response

class MoneyRepository {
    suspend fun getUserNoticeList(languageType: String): Response<MoneyRechCfgResult> {
        return OneBoSportApi.moneyService.getRechCfg(languageType)
    }
}