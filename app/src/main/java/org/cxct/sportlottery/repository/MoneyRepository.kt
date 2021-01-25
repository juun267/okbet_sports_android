package org.cxct.sportlottery.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.cxct.sportlottery.db.dao.UserInfoDao
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.MoneyRechCfgResult
import retrofit2.Response

class MoneyRepository(private val userInfoDao: UserInfoDao) {

    val userInfo: Flow<UserInfo?>
        get() = userInfoDao.getUserInfo().map {
            if (it.isNotEmpty()) {
                return@map it[0]
            }
            return@map null
        }

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