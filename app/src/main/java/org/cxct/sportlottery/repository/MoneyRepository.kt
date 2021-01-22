package org.cxct.sportlottery.repository

import org.cxct.sportlottery.db.dao.UserInfoDao
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.MoneyRechCfgResult
import retrofit2.Response

class MoneyRepository(private val userInfoDao: UserInfoDao) {
    suspend fun getRechCfg(): Response<MoneyRechCfgResult> {
        return OneBoSportApi.moneyService.getRechCfg()
    }
}