package org.cxct.sportlottery.repository

import kotlinx.coroutines.flow.Flow
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.config.MoneyRechCfgResult
import retrofit2.Response

class MoneyRepository() {

    val userInfo: Flow<UserInfo?>?
        get() = MultiLanguagesApplication.getInstance()?.userInfo
//        get() = userInfoDao.getUserInfo().map {
//            if (it.isNotEmpty()) {
//                return@map it[0]
//            }
//            return@map null
//        }

    suspend fun getRechCfg(): Response<MoneyRechCfgResult> {
        return OneBoSportApi.moneyService.getRechCfg()
    }

    suspend fun rechargeAdd(moneyAddRequest: MoneyAddRequest): Response<MoneyAddResult> {
        return OneBoSportApi.moneyService.rechargeAdd(moneyAddRequest)
    }

}